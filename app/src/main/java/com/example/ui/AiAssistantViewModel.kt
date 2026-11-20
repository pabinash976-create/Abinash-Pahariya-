package com.example.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiSearchResult
import com.example.ai.AiSearchScope
import com.example.ai.AiStreamEvent
import com.example.ai.AiStudyMode
import com.example.ai.AiStudyRepository
import com.example.data.FixedSubjects
import com.example.data.LocalVaultMatch
import com.example.data.NoteEntity
import com.example.data.StudyLanguage
import com.example.data.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiChatMessage(
    val id: Long = System.nanoTime(),
    val isUser: Boolean,
    val text: String,
    val modeLabel: String = "",
    val languageLabel: String = "",
    val subjectId: Int = 1,
    val hasImage: Boolean = false
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val partialText: String) : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

class AiAssistantViewModel(
    private val aiRepository: AiStudyRepository,
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(StudyLanguage.ENGLISH)
    val selectedLanguage: StateFlow<StudyLanguage> = _selectedLanguage.asStateFlow()

    private val _selectedMode = MutableStateFlow(AiStudyMode.OPEN_CHAT)
    val selectedMode: StateFlow<AiStudyMode> = _selectedMode.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow(1)
    val selectedSubjectId: StateFlow<Int> = _selectedSubjectId.asStateFlow()

    private val _attachedBitmap = MutableStateFlow<Bitmap?>(null)
    val attachedBitmap: StateFlow<Bitmap?> = _attachedBitmap.asStateFlow()

    private val _actionBanner = MutableStateFlow<String?>(null)
    val actionBanner: StateFlow<String?> = _actionBanner.asStateFlow()

    // AI Search Mode State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchScope = MutableStateFlow(AiSearchScope.ALL_KNOWLEDGE)
    val searchScope: StateFlow<AiSearchScope> = _searchScope.asStateFlow()

    private val _searchAllSubjects = MutableStateFlow(true)
    val searchAllSubjects: StateFlow<Boolean> = _searchAllSubjects.asStateFlow()

    private val _vaultMatches = MutableStateFlow<List<LocalVaultMatch>>(emptyList())
    val vaultMatches: StateFlow<List<LocalVaultMatch>> = _vaultMatches.asStateFlow()

    private val _searchResult = MutableStateFlow<AiSearchResult?>(null)
    val searchResult: StateFlow<AiSearchResult?> = _searchResult.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private var activeSearchJob: Job? = null

    fun setLanguage(language: StudyLanguage) {
        _selectedLanguage.value = language
    }

    fun setMode(mode: AiStudyMode) {
        _selectedMode.value = mode
    }

    fun setSubjectId(subjectId: Int) {
        _selectedSubjectId.value = subjectId.coerceIn(1, 7)
    }

    fun setAttachedBitmap(bitmap: Bitmap?) {
        _attachedBitmap.value = bitmap
    }

    fun setSearchScope(scope: AiSearchScope) {
        _searchScope.value = scope
    }

    fun setSearchAllSubjects(allSubjects: Boolean) {
        _searchAllSubjects.value = allSubjects
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openAiSearch(initialQuery: String? = null) {
        _selectedMode.value = AiStudyMode.AI_SEARCH
        val q = initialQuery?.trim().orEmpty()
        if (q.isNotEmpty()) {
            _searchQuery.value = q
            runAiSearch(q)
        }
    }

    fun clearSearch() {
        activeSearchJob?.cancel()
        _searchQuery.value = ""
        _vaultMatches.value = emptyList()
        _searchResult.value = null
        _searchError.value = null
        _isSearching.value = false
    }

    fun runAiSearch(queryInput: String = _searchQuery.value) {
        val trimmed = queryInput.trim()
        val image = _attachedBitmap.value
        if (trimmed.isEmpty() && image == null) return

        val effectiveQuery = if (trimmed.isEmpty()) {
            "Identify and summarize the academic topic in this attached page image."
        } else trimmed

        _searchQuery.value = effectiveQuery
        _selectedMode.value = AiStudyMode.AI_SEARCH

        val allSubjects = _searchAllSubjects.value
        val subjectId = _selectedSubjectId.value
        val subjectName = if (allSubjects) {
            "All 7 Study Subjects"
        } else {
            FixedSubjects.ALL.firstOrNull { it.id == subjectId }?.name ?: "Education (Major)"
        }
        val language = _selectedLanguage.value
        val scope = _searchScope.value
        _attachedBitmap.value = null

        activeSearchJob?.cancel()
        activeSearchJob = viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            val matches = studyRepository.findLocalStudyMatches(
                query = effectiveQuery,
                subjectIdFilter = if (allSubjects) null else subjectId
            )
            _vaultMatches.value = matches

            aiRepository.performAiSearch(
                query = effectiveQuery,
                language = language,
                subjectName = subjectName,
                scope = scope,
                localMatches = matches,
                attachedImage = image
            ).onSuccess { result ->
                _searchResult.value = result
            }.onFailure { err ->
                _searchError.value = err.localizedMessage ?: "AI Search could not complete."
            }
            _isSearching.value = false
        }
    }

    fun clearBanner() {
        _actionBanner.value = null
    }

    fun clearChat() {
        _messages.value = emptyList()
        _uiState.value = AiUiState.Idle
        _attachedBitmap.value = null
    }

    fun sendPrompt(promptInput: String) {
        val trimmed = promptInput.trim()
        val image = _attachedBitmap.value
        if (trimmed.isEmpty() && image == null) return

        val effectivePrompt = if (trimmed.isEmpty()) {
            "Read and explain the academic content in this attached study page image."
        } else trimmed

        val mode = _selectedMode.value
        val language = _selectedLanguage.value
        val subjectId = _selectedSubjectId.value
        val subjectName = FixedSubjects.ALL.firstOrNull { it.id == subjectId }?.name ?: "Education (Major)"

        val userMessage = AiChatMessage(
            isUser = true,
            text = effectivePrompt,
            modeLabel = mode.label,
            languageLabel = language.displayName,
            subjectId = subjectId,
            hasImage = image != null
        )
        _messages.value = _messages.value + userMessage
        _attachedBitmap.value = null

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            aiRepository.streamStudyHelp(
                prompt = effectivePrompt,
                mode = mode,
                language = language,
                subjectName = subjectName,
                attachedImage = image
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Chunk -> {
                        _uiState.value = AiUiState.Streaming(event.accumulatedText)
                    }
                    is AiStreamEvent.Completed -> {
                        _messages.value = _messages.value + AiChatMessage(
                            isUser = false,
                            text = event.finalText,
                            modeLabel = mode.label,
                            languageLabel = language.displayName,
                            subjectId = subjectId
                        )
                        _uiState.value = AiUiState.Success(event.finalText)
                    }
                    is AiStreamEvent.Error -> {
                        _uiState.value = AiUiState.Error(event.message)
                    }
                }
            }
        }
    }

    fun generateAndSaveImportantQuestions(topic: String) {
        val trimmed = topic.trim()
        if (trimmed.isEmpty()) return
        val subjectId = _selectedSubjectId.value
        val language = _selectedLanguage.value
        val subjectName = FixedSubjects.ALL.firstOrNull { it.id == subjectId }?.name ?: "Education (Major)"

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            aiRepository.generateStructuredImportantQuestions(trimmed, subjectId, subjectName, language)
                .onSuccess { items ->
                    items.forEach { studyRepository.saveImportantQuestion(it) }
                    val summaryText = items.mapIndexed { idx, q ->
                        "${idx + 1}. [${q.priority}] ${q.question}\nAnswer: ${q.answer}"
                    }.joinToString("\n\n")
                    _messages.value = _messages.value + AiChatMessage(
                        isUser = true,
                        text = "Generate Important Questions: $trimmed",
                        modeLabel = "Import Qs",
                        languageLabel = language.displayName,
                        subjectId = subjectId
                    ) + AiChatMessage(
                        isUser = false,
                        text = "Saved ${items.size} important questions to $subjectName:\n\n$summaryText",
                        modeLabel = "Saved to $subjectName",
                        languageLabel = language.displayName,
                        subjectId = subjectId
                    )
                    _uiState.value = AiUiState.Success(summaryText)
                    _actionBanner.value = "Saved ${items.size} Important Questions to $subjectName"
                }
                .onFailure { err ->
                    _uiState.value = AiUiState.Error(err.localizedMessage ?: "Failed to generate questions.")
                }
        }
    }

    fun generateAndSaveQuiz(topic: String) {
        val trimmed = topic.trim()
        if (trimmed.isEmpty()) return
        val subjectId = _selectedSubjectId.value
        val language = _selectedLanguage.value
        val subjectName = FixedSubjects.ALL.firstOrNull { it.id == subjectId }?.name ?: "Education (Major)"

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            aiRepository.generateStructuredQuiz(trimmed, subjectId, subjectName, language)
                .onSuccess { items ->
                    items.forEach { studyRepository.saveQuizQuestion(it) }
                    val preview = items.mapIndexed { idx, q ->
                        "${idx + 1}. ${q.question}\nA) ${q.optionA}  B) ${q.optionB}  C) ${q.optionC}  D) ${q.optionD}"
                    }.joinToString("\n\n")
                    _messages.value = _messages.value + AiChatMessage(
                        isUser = true,
                        text = "Build Quiz: $trimmed",
                        modeLabel = "Quiz Builder",
                        languageLabel = language.displayName,
                        subjectId = subjectId
                    ) + AiChatMessage(
                        isUser = false,
                        text = "Added ${items.size} interactive MCQ questions to $subjectName Quiz tab:\n\n$preview",
                        modeLabel = "Quiz Ready",
                        languageLabel = language.displayName,
                        subjectId = subjectId
                    )
                    _uiState.value = AiUiState.Success(preview)
                    _actionBanner.value = "Added ${items.size} Quiz MCQs to $subjectName"
                }
                .onFailure { err ->
                    _uiState.value = AiUiState.Error(err.localizedMessage ?: "Failed to generate quiz.")
                }
        }
    }

    fun saveAiResponseAsNote(title: String, content: String, subjectId: Int) {
        viewModelScope.launch {
            studyRepository.saveNote(
                NoteEntity(
                    subjectId = subjectId.coerceIn(1, 7),
                    title = title.trim().ifEmpty { "AI Study Note" },
                    content = content,
                    sourceTag = "AI"
                )
            )
            val subjectName = FixedSubjects.ALL.firstOrNull { it.id == subjectId }?.name ?: "Subject"
            _actionBanner.value = "Saved AI note to $subjectName"
        }
    }

    class Factory(private val studyRepository: StudyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AiAssistantViewModel(AiStudyRepository(), studyRepository) as T
        }
    }
}
