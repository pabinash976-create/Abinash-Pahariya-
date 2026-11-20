package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AssignmentEntity
import com.example.data.BackupSummary
import com.example.data.DiaryEntryEntity
import com.example.data.FixedSubjects
import com.example.data.ImportantQuestionEntity
import com.example.data.NoteEntity
import com.example.data.PdfBookEntity
import com.example.data.PdfPageBookmarkEntity
import com.example.data.QuizQuestionEntity
import com.example.data.StudyLanguage
import com.example.data.StudyRepository
import com.example.data.StudyTaskEntity
import com.example.data.SubjectEntity
import com.example.data.TimetableSlotEntity
import com.example.data.UserPreferences
import com.example.data.UserPreferencesRepository
import com.example.data.WolfStudyDatabase
import com.example.media.BookOcrProcessor
import com.example.media.PdfLibraryManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AutoSaveStatus { IDLE, SAVING, SAVED }

sealed interface BackupOperationState {
    data object Idle : BackupOperationState
    data object InProgress : BackupOperationState
    data class Success(val message: String, val summary: BackupSummary) : BackupOperationState
    data class Error(val message: String) : BackupOperationState
}

class WolfStudyViewModel(
    application: Application,
    val repository: StudyRepository,
    val prefsRepository: UserPreferencesRepository,
    val ocrProcessor: BookOcrProcessor
) : AndroidViewModel(application) {

    val subjects: StateFlow<List<SubjectEntity>> = repository.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FixedSubjects.ALL)

    val notes: StateFlow<List<NoteEntity>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pdfBooks: StateFlow<List<PdfBookEntity>> = repository.pdfBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pdfPageBookmarks: StateFlow<List<PdfPageBookmarkEntity>> = repository.pdfPageBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importantQuestions: StateFlow<List<ImportantQuestionEntity>> = repository.importantQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizQuestions: StateFlow<List<QuizQuestionEntity>> = repository.quizQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<AssignmentEntity>> = repository.assignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diaryEntries: StateFlow<List<DiaryEntryEntity>> = repository.diaryEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableSlots: StateFlow<List<TimetableSlotEntity>> = repository.timetableSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<StudyTaskEntity>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = prefsRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _autoSaveStatus = MutableStateFlow(AutoSaveStatus.IDLE)
    val autoSaveStatus: StateFlow<AutoSaveStatus> = _autoSaveStatus.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _backupState = MutableStateFlow<BackupOperationState>(BackupOperationState.Idle)
    val backupState: StateFlow<BackupOperationState> = _backupState.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSevenFixedSubjectsSeeded()
            val prefs = prefsRepository.preferencesFlow.first()
            if (!prefs.appLockEnabled || prefs.privacyPinHash.isBlank()) {
                _isUnlocked.value = true
            }
        }
    }

    fun markSplashFinished() {
        _splashFinished.value = true
    }

    fun tryUnlockWithPin(pin: String): Boolean {
        val prefs = userPreferences.value
        val valid = prefsRepository.verifyPin(pin, prefs.privacyPinHash)
        if (valid) {
            _isUnlocked.value = true
        }
        return valid
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    // Notes with automatic debounced save
    fun autoSaveNote(
        existingId: Long,
        subjectId: Int,
        title: String,
        content: String,
        isBookmarked: Boolean,
        sourceTag: String,
        createdAt: Long,
        onAssignedId: (Long) -> Unit
    ) {
        if (title.isBlank() && content.isBlank()) return
        autoSaveJob?.cancel()
        _autoSaveStatus.value = AutoSaveStatus.SAVING
        autoSaveJob = viewModelScope.launch {
            delay(450L)
            val savedId = repository.saveNote(
                NoteEntity(
                    id = existingId,
                    subjectId = subjectId.coerceIn(1, 7),
                    title = title.trim().ifEmpty { "Untitled Study Note" },
                    content = content,
                    isBookmarked = isBookmarked,
                    sourceTag = sourceTag,
                    createdAt = createdAt
                )
            )
            onAssignedId(savedId)
            _autoSaveStatus.value = AutoSaveStatus.SAVED
        }
    }

    fun saveNoteImmediate(note: NoteEntity, onDone: (Long) -> Unit = {}) {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            val savedId = repository.saveNote(
                note.copy(title = note.title.trim().ifEmpty { "Untitled Study Note" })
            )
            _autoSaveStatus.value = AutoSaveStatus.SAVED
            onDone(savedId)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            showToast("Note deleted")
        }
    }

    fun toggleNoteBookmark(note: NoteEntity) {
        viewModelScope.launch { repository.toggleNoteBookmark(note) }
    }

    // PDF Digital Library
    fun importPdf(uri: Uri, subjectId: Int, customTitle: String?, onComplete: (PdfBookEntity?) -> Unit = {}) {
        viewModelScope.launch {
            repository.importAndSavePdf(uri, subjectId, customTitle)
                .onSuccess { book ->
                    showToast("Imported \"${book.title}\" (${book.pageCount} pages)")
                    onComplete(book)
                }
                .onFailure { err ->
                    showToast(err.localizedMessage ?: "Failed to import PDF")
                    onComplete(null)
                }
        }
    }

    fun updatePdfMetadata(book: PdfBookEntity, title: String, subjectId: Int) {
        viewModelScope.launch {
            repository.updatePdfBookTitleAndSubject(book, title, subjectId)
            showToast("Book updated")
        }
    }

    fun deletePdfBook(book: PdfBookEntity) {
        viewModelScope.launch {
            repository.deletePdfBook(book)
            showToast("PDF removed from library")
        }
    }

    fun togglePdfBookBookmark(book: PdfBookEntity) {
        viewModelScope.launch { repository.togglePdfBookBookmark(book) }
    }

    fun updatePdfLastReadPage(bookId: Long, page: Int) {
        viewModelScope.launch { repository.updatePdfLastReadPage(bookId, page) }
    }

    suspend fun renderPdfPage(localPath: String, pageIndex: Int, targetWidthPx: Int): Result<Bitmap> =
        repository.pdfLibraryManager.renderPage(localPath, pageIndex, targetWidthPx)

    fun togglePdfPageBookmark(bookId: Long, pageNumber: Int, isCurrentlyBookmarked: Boolean, label: String) {
        viewModelScope.launch {
            if (isCurrentlyBookmarked) {
                repository.removePdfPageBookmark(bookId, pageNumber)
                showToast("Removed page ${pageNumber + 1} bookmark")
            } else {
                repository.addPdfPageBookmark(bookId, pageNumber, label)
                showToast("Bookmarked page ${pageNumber + 1}")
            }
        }
    }

    fun deletePdfPageBookmarkById(id: Long) {
        viewModelScope.launch { repository.removePdfPageBookmarkById(id) }
    }

    // Important Questions
    fun saveImportantQuestion(item: ImportantQuestionEntity) {
        viewModelScope.launch {
            repository.saveImportantQuestion(item)
            showToast("Question saved")
        }
    }

    fun deleteImportantQuestion(id: Long) {
        viewModelScope.launch {
            repository.deleteImportantQuestion(id)
            showToast("Question removed")
        }
    }

    fun toggleImportantQuestionBookmark(item: ImportantQuestionEntity) {
        viewModelScope.launch { repository.toggleImportantQuestionBookmark(item) }
    }

    // Quiz
    fun saveQuizQuestion(item: QuizQuestionEntity) {
        viewModelScope.launch {
            repository.saveQuizQuestion(item)
            showToast("Quiz question saved")
        }
    }

    fun deleteQuizQuestion(id: Long) {
        viewModelScope.launch {
            repository.deleteQuizQuestion(id)
            showToast("Quiz question removed")
        }
    }

    fun recordQuizAttempt(id: Long, correct: Boolean) {
        viewModelScope.launch { repository.recordQuizAttempt(id, correct) }
    }

    // Assignments
    fun saveAssignment(item: AssignmentEntity) {
        viewModelScope.launch {
            repository.saveAssignment(item)
            showToast("Assignment saved")
        }
    }

    fun deleteAssignment(id: Long) {
        viewModelScope.launch {
            repository.deleteAssignment(id)
            showToast("Assignment removed")
        }
    }

    fun toggleAssignmentBookmark(item: AssignmentEntity) {
        viewModelScope.launch { repository.toggleAssignmentBookmark(item) }
    }

    // Diary
    fun saveDiaryEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.saveDiaryEntry(entry)
            showToast("Study diary entry saved")
        }
    }

    fun deleteDiaryEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(id)
            showToast("Diary entry deleted")
        }
    }

    // Timetable
    fun saveTimetableSlot(slot: TimetableSlotEntity) {
        viewModelScope.launch {
            repository.saveTimetableSlot(slot)
            showToast("Schedule updated")
        }
    }

    fun deleteTimetableSlot(id: Long) {
        viewModelScope.launch {
            repository.deleteTimetableSlot(id)
            showToast("Schedule slot removed")
        }
    }

    // Tasks
    fun saveTask(task: StudyTaskEntity) {
        viewModelScope.launch {
            repository.saveTask(task)
            showToast("Task saved")
        }
    }

    fun toggleTaskCompleted(task: StudyTaskEntity) {
        viewModelScope.launch { repository.toggleTaskCompleted(task) }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            showToast("Task deleted")
        }
    }

    // Settings & Preferences
    fun updateProfile(name: String, track: String) {
        viewModelScope.launch {
            prefsRepository.updateProfile(name, track)
            showToast("Profile updated")
        }
    }

    fun setLanguage(language: StudyLanguage) {
        viewModelScope.launch {
            prefsRepository.setLanguage(language)
            showToast("Language set to ${language.displayName}")
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setDarkTheme(enabled) }
    }

    fun setThemePreset(preset: com.example.ui.theme.WolfThemePreset) {
        viewModelScope.launch {
            prefsRepository.setThemePreset(preset)
            showToast("Theme applied: ${preset.displayName}")
        }
    }

    fun setPrivacyPin(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setPrivacyPin(pin, enabled)
            showToast(if (enabled) "Privacy PIN protection enabled" else "Privacy PIN disabled")
        }
    }

    fun disableAppLock() {
        viewModelScope.launch {
            prefsRepository.disableAppLock()
            showToast("App lock disabled")
        }
    }

    fun setHideProfileInBackup(hide: Boolean) {
        viewModelScope.launch { prefsRepository.setHideProfileInBackup(hide) }
    }

    fun clearAllStudyData() {
        viewModelScope.launch {
            repository.clearAllStudyData()
            showToast("All personal study materials cleared")
        }
    }

    // Backup & Restore
    fun exportBackup(uri: Uri, passphrase: String) {
        viewModelScope.launch {
            _backupState.value = BackupOperationState.InProgress
            val prefs = userPreferences.value
            repository.exportBackupToUri(
                uri = uri,
                passphrase = passphrase,
                hideProfile = prefs.hideProfileInBackup,
                studentName = prefs.studentName,
                academicTrack = prefs.academicTrack
            ).onSuccess { summary ->
                prefsRepository.updateLastBackupTimestamp(System.currentTimeMillis())
                val encNote = if (passphrase.isNotBlank()) " (AES-encrypted)" else ""
                _backupState.value = BackupOperationState.Success(
                    "Backup exported successfully$encNote",
                    summary
                )
            }.onFailure { err ->
                _backupState.value = BackupOperationState.Error(
                    err.localizedMessage ?: "Export failed."
                )
            }
        }
    }

    fun importBackup(uri: Uri, passphrase: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            _backupState.value = BackupOperationState.InProgress
            repository.importBackupFromUri(uri, passphrase, replaceExisting)
                .onSuccess { summary ->
                    _backupState.value = BackupOperationState.Success(
                        "Backup restored successfully",
                        summary
                    )
                }
                .onFailure { err ->
                    _backupState.value = BackupOperationState.Error(
                        err.localizedMessage ?: "Restore failed."
                    )
                }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupOperationState.Idle
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = WolfStudyDatabase.getInstance(application)
            val pdfManager = PdfLibraryManager(application)
            val repo = StudyRepository(application, db.studyDao(), pdfManager)
            val prefsRepo = UserPreferencesRepository(application)
            val ocr = BookOcrProcessor(application)
            return WolfStudyViewModel(application, repo, prefsRepo, ocr) as T
        }
    }
}
