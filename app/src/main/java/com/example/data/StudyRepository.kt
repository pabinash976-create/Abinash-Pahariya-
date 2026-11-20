package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.media.PdfLibraryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class BackupSummary(
    val notesCount: Int,
    val booksCount: Int,
    val questionsCount: Int,
    val quizzesCount: Int,
    val assignmentsCount: Int,
    val diaryCount: Int,
    val timetableCount: Int,
    val tasksCount: Int
)

data class LocalVaultMatch(
    val kind: String,
    val itemId: Long,
    val subjectId: Int,
    val title: String,
    val snippet: String,
    val score: Int
)

class StudyRepository(
    private val context: Context,
    private val dao: StudyDao,
    val pdfLibraryManager: PdfLibraryManager
) {
    val subjects: Flow<List<SubjectEntity>> = dao.observeSubjects()
    val notes: Flow<List<NoteEntity>> = dao.observeAllNotes()
    val pdfBooks: Flow<List<PdfBookEntity>> = dao.observeAllPdfBooks()
    val pdfPageBookmarks: Flow<List<PdfPageBookmarkEntity>> = dao.observeAllPdfPageBookmarks()
    val importantQuestions: Flow<List<ImportantQuestionEntity>> = dao.observeAllImportantQuestions()
    val quizQuestions: Flow<List<QuizQuestionEntity>> = dao.observeAllQuizQuestions()
    val assignments: Flow<List<AssignmentEntity>> = dao.observeAllAssignments()
    val diaryEntries: Flow<List<DiaryEntryEntity>> = dao.observeAllDiaryEntries()
    val timetableSlots: Flow<List<TimetableSlotEntity>> = dao.observeAllTimetableSlots()
    val tasks: Flow<List<StudyTaskEntity>> = dao.observeAllTasks()

    suspend fun ensureSevenFixedSubjectsSeeded() {
        val current = dao.getSubjectsOnce()
        if (current.size != FixedSubjects.ALL.size || current.map { it.id } != FixedSubjects.ALL.map { it.id }) {
            dao.upsertSubjects(FixedSubjects.ALL)
        }
    }

    // Notes
    suspend fun saveNote(note: NoteEntity): Long =
        dao.upsertNote(note.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteNote(id: Long) = dao.deleteNoteById(id)

    suspend fun toggleNoteBookmark(note: NoteEntity) =
        dao.setNoteBookmarked(note.id, !note.isBookmarked)

    // PDFs & Bookmarks
    suspend fun importAndSavePdf(uri: Uri, subjectId: Int, customTitle: String?): Result<PdfBookEntity> {
        val imported = pdfLibraryManager.importPdf(uri)
        return imported.map { info ->
            val entity = PdfBookEntity(
                subjectId = subjectId.coerceIn(1, 7),
                title = customTitle?.trim()?.takeIf { it.isNotEmpty() } ?: info.suggestedTitle,
                localPath = info.localPath,
                fileName = info.fileName,
                pageCount = info.pageCount
            )
            val insertedId = dao.upsertPdfBook(entity)
            entity.copy(id = insertedId)
        }
    }

    suspend fun updatePdfBookTitleAndSubject(book: PdfBookEntity, newTitle: String, newSubjectId: Int) {
        dao.upsertPdfBook(book.copy(title = newTitle.trim().ifEmpty { book.title }, subjectId = newSubjectId.coerceIn(1, 7)))
    }

    suspend fun deletePdfBook(book: PdfBookEntity) {
        dao.deletePageBookmarksForBook(book.id)
        dao.deletePdfBookById(book.id)
        pdfLibraryManager.deleteLocalPdf(book.localPath)
    }

    suspend fun togglePdfBookBookmark(book: PdfBookEntity) =
        dao.setPdfBookBookmarked(book.id, !book.isBookmarked)

    suspend fun updatePdfLastReadPage(bookId: Long, page: Int) =
        dao.updatePdfLastReadPage(bookId, page)

    suspend fun addPdfPageBookmark(bookId: Long, pageNumber: Int, label: String) {
        dao.deletePdfPageBookmark(bookId, pageNumber)
        dao.upsertPdfPageBookmark(
            PdfPageBookmarkEntity(
                bookId = bookId,
                pageNumber = pageNumber,
                noteLabel = label.trim().ifEmpty { "Page ${pageNumber + 1}" }
            )
        )
    }

    suspend fun removePdfPageBookmark(bookId: Long, pageNumber: Int) =
        dao.deletePdfPageBookmark(bookId, pageNumber)

    suspend fun removePdfPageBookmarkById(id: Long) =
        dao.deletePdfPageBookmarkById(id)

    // Important Questions
    suspend fun saveImportantQuestion(item: ImportantQuestionEntity): Long =
        dao.upsertImportantQuestion(item.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteImportantQuestion(id: Long) = dao.deleteImportantQuestionById(id)

    suspend fun toggleImportantQuestionBookmark(item: ImportantQuestionEntity) =
        dao.setImportantQuestionBookmarked(item.id, !item.isBookmarked)

    // Quiz
    suspend fun saveQuizQuestion(item: QuizQuestionEntity): Long =
        dao.upsertQuizQuestion(item.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteQuizQuestion(id: Long) = dao.deleteQuizQuestionById(id)

    suspend fun recordQuizAttempt(id: Long, correct: Boolean) = dao.recordQuizAttempt(id, correct)

    // Assignments
    suspend fun saveAssignment(item: AssignmentEntity): Long =
        dao.upsertAssignment(item.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteAssignment(id: Long) = dao.deleteAssignmentById(id)

    suspend fun toggleAssignmentBookmark(item: AssignmentEntity) =
        dao.setAssignmentBookmarked(item.id, !item.isBookmarked)

    // Diary
    suspend fun saveDiaryEntry(item: DiaryEntryEntity): Long = dao.upsertDiaryEntry(item)

    suspend fun deleteDiaryEntry(id: Long) = dao.deleteDiaryEntryById(id)

    // Timetable
    suspend fun saveTimetableSlot(item: TimetableSlotEntity): Long = dao.upsertTimetableSlot(item)

    suspend fun deleteTimetableSlot(id: Long) = dao.deleteTimetableSlotById(id)

    // Tasks
    suspend fun saveTask(item: StudyTaskEntity): Long = dao.upsertTask(item)

    suspend fun toggleTaskCompleted(item: StudyTaskEntity) =
        dao.updateTask(item.copy(isCompleted = !item.isCompleted))

    suspend fun deleteTask(id: Long) = dao.deleteTaskById(id)

    suspend fun clearAllStudyData() {
        val books = dao.getAllPdfBooksOnce()
        books.forEach { pdfLibraryManager.deleteLocalPdf(it.localPath) }
        dao.clearAllStudyData()
    }

    suspend fun findLocalStudyMatches(
        query: String,
        subjectIdFilter: Int? = null,
        maxResults: Int = 8
    ): List<LocalVaultMatch> = withContext(Dispatchers.IO) {
        val tokens = query.trim().lowercase()
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length >= 2 }
        if (tokens.isEmpty()) return@withContext emptyList()

        fun scoreText(vararg fields: String): Int {
            val combined = fields.joinToString(" ").lowercase()
            val fullQuery = query.trim().lowercase()
            var score = if (combined.contains(fullQuery)) 5 else 0
            tokens.forEach { tok ->
                if (combined.contains(tok)) score += 2
            }
            return score
        }

        val matches = mutableListOf<LocalVaultMatch>()

        dao.getAllNotesOnce()
            .filter { subjectIdFilter == null || it.subjectId == subjectIdFilter }
            .forEach { note ->
                val s = scoreText(note.title, note.content)
                if (s > 0) {
                    matches += LocalVaultMatch(
                        kind = "NOTE",
                        itemId = note.id,
                        subjectId = note.subjectId,
                        title = note.title,
                        snippet = note.content.replace('\n', ' ').take(160),
                        score = s
                    )
                }
            }

        dao.getAllPdfBooksOnce()
            .filter { subjectIdFilter == null || it.subjectId == subjectIdFilter }
            .forEach { book ->
                val s = scoreText(book.title, book.fileName)
                if (s > 0) {
                    matches += LocalVaultMatch(
                        kind = "PDF",
                        itemId = book.id,
                        subjectId = book.subjectId,
                        title = book.title,
                        snippet = "${book.fileName} • ${book.pageCount} pages",
                        score = s
                    )
                }
            }

        dao.getAllImportantQuestionsOnce()
            .filter { subjectIdFilter == null || it.subjectId == subjectIdFilter }
            .forEach { q ->
                val s = scoreText(q.question, q.answer)
                if (s > 0) {
                    matches += LocalVaultMatch(
                        kind = "QUESTION",
                        itemId = q.id,
                        subjectId = q.subjectId,
                        title = q.question.take(90),
                        snippet = q.answer.replace('\n', ' ').take(160),
                        score = s
                    )
                }
            }

        dao.getAllAssignmentsOnce()
            .filter { subjectIdFilter == null || it.subjectId == subjectIdFilter }
            .forEach { a ->
                val s = scoreText(a.title, a.description)
                if (s > 0) {
                    matches += LocalVaultMatch(
                        kind = "ASSIGNMENT",
                        itemId = a.id,
                        subjectId = a.subjectId,
                        title = a.title,
                        snippet = a.description.replace('\n', ' ').ifBlank { "Status: ${a.status}" }.take(160),
                        score = s
                    )
                }
            }

        dao.getAllTasksOnce()
            .filter { subjectIdFilter == null || it.subjectId == subjectIdFilter }
            .forEach { t ->
                val s = scoreText(t.title)
                if (s > 0) {
                    matches += LocalVaultMatch(
                        kind = "TASK",
                        itemId = t.id,
                        subjectId = t.subjectId,
                        title = t.title,
                        snippet = if (t.isCompleted) "Completed task" else "Pending task (${t.priority})",
                        score = s
                    )
                }
            }

        matches.sortedByDescending { it.score }.take(maxResults)
    }

    // Full JSON Backup & Restore (with optional AES-GCM passphrase protection)
    suspend fun exportBackupToUri(
        uri: Uri,
        passphrase: String,
        hideProfile: Boolean,
        studentName: String,
        academicTrack: String
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        try {
            val notesList = dao.getAllNotesOnce()
            val booksList = dao.getAllPdfBooksOnce()
            val pageBookmarks = dao.getAllPdfPageBookmarksOnce()
            val impQuestions = dao.getAllImportantQuestionsOnce()
            val quizQuestions = dao.getAllQuizQuestionsOnce()
            val assignmentsList = dao.getAllAssignmentsOnce()
            val diaryList = dao.getAllDiaryEntriesOnce()
            val timetableList = dao.getAllTimetableSlotsOnce()
            val tasksList = dao.getAllTasksOnce()

            val dataJson = JSONObject().apply {
                if (!hideProfile) {
                    put("profile", JSONObject().apply {
                        put("studentName", studentName)
                        put("academicTrack", academicTrack)
                    })
                }
                put("notes", JSONArray().apply {
                    notesList.forEach { n ->
                        put(JSONObject().apply {
                            put("subjectId", n.subjectId)
                            put("title", n.title)
                            put("content", n.content)
                            put("isBookmarked", n.isBookmarked)
                            put("sourceTag", n.sourceTag)
                            put("createdAt", n.createdAt)
                            put("updatedAt", n.updatedAt)
                        })
                    }
                })
                put("pdfBooks", JSONArray().apply {
                    booksList.forEach { b ->
                        put(JSONObject().apply {
                            put("legacyId", b.id)
                            put("subjectId", b.subjectId)
                            put("title", b.title)
                            put("localPath", b.localPath)
                            put("fileName", b.fileName)
                            put("pageCount", b.pageCount)
                            put("lastReadPage", b.lastReadPage)
                            put("isBookmarked", b.isBookmarked)
                            put("addedAt", b.addedAt)
                        })
                    }
                })
                put("pdfPageBookmarks", JSONArray().apply {
                    pageBookmarks.forEach { bm ->
                        put(JSONObject().apply {
                            put("legacyBookId", bm.bookId)
                            put("pageNumber", bm.pageNumber)
                            put("noteLabel", bm.noteLabel)
                            put("createdAt", bm.createdAt)
                        })
                    }
                })
                put("importantQuestions", JSONArray().apply {
                    impQuestions.forEach { q ->
                        put(JSONObject().apply {
                            put("subjectId", q.subjectId)
                            put("question", q.question)
                            put("answer", q.answer)
                            put("priority", q.priority)
                            put("isBookmarked", q.isBookmarked)
                            put("updatedAt", q.updatedAt)
                        })
                    }
                })
                put("quizQuestions", JSONArray().apply {
                    quizQuestions.forEach { q ->
                        put(JSONObject().apply {
                            put("subjectId", q.subjectId)
                            put("question", q.question)
                            put("optionA", q.optionA)
                            put("optionB", q.optionB)
                            put("optionC", q.optionC)
                            put("optionD", q.optionD)
                            put("correctIndex", q.correctIndex)
                            put("explanation", q.explanation)
                            put("updatedAt", q.updatedAt)
                        })
                    }
                })
                put("assignments", JSONArray().apply {
                    assignmentsList.forEach { a ->
                        put(JSONObject().apply {
                            put("subjectId", a.subjectId)
                            put("title", a.title)
                            put("description", a.description)
                            put("dueDateMillis", a.dueDateMillis)
                            put("status", a.status)
                            put("isBookmarked", a.isBookmarked)
                            put("updatedAt", a.updatedAt)
                        })
                    }
                })
                put("diaryEntries", JSONArray().apply {
                    diaryList.forEach { d ->
                        put(JSONObject().apply {
                            put("dateKey", d.dateKey)
                            put("title", d.title)
                            put("learningRecord", d.learningRecord)
                            put("studyMinutes", d.studyMinutes)
                            put("focusRating", d.focusRating)
                            if (d.subjectId != null) put("subjectId", d.subjectId)
                            put("createdAt", d.createdAt)
                        })
                    }
                })
                put("timetableSlots", JSONArray().apply {
                    timetableList.forEach { t ->
                        put(JSONObject().apply {
                            put("subjectId", t.subjectId)
                            put("dayOfWeek", t.dayOfWeek)
                            put("startMinutes", t.startMinutes)
                            put("endMinutes", t.endMinutes)
                            put("topicNote", t.topicNote)
                        })
                    }
                })
                put("studyTasks", JSONArray().apply {
                    tasksList.forEach { task ->
                        put(JSONObject().apply {
                            put("subjectId", task.subjectId)
                            put("title", task.title)
                            put("deadlineMillis", task.deadlineMillis)
                            put("isCompleted", task.isCompleted)
                            put("priority", task.priority)
                            put("createdAt", task.createdAt)
                        })
                    }
                })
            }

            val envelope = if (passphrase.isNotBlank()) {
                val (ivBase64, cipherBase64) = encryptAesGcm(dataJson.toString(), passphrase)
                JSONObject().apply {
                    put("format", "wolf_study_backup_v1")
                    put("exportedAt", System.currentTimeMillis())
                    put("encrypted", true)
                    put("iv", ivBase64)
                    put("payload", cipherBase64)
                }
            } else {
                JSONObject().apply {
                    put("format", "wolf_study_backup_v1")
                    put("exportedAt", System.currentTimeMillis())
                    put("encrypted", false)
                    put("data", dataJson)
                }
            }

            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(envelope.toString(2).toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open backup destination."))

            Result.success(
                BackupSummary(
                    notesCount = notesList.size,
                    booksCount = booksList.size,
                    questionsCount = impQuestions.size,
                    quizzesCount = quizQuestions.size,
                    assignmentsCount = assignmentsList.size,
                    diaryCount = diaryList.size,
                    timetableCount = timetableList.size,
                    tasksCount = tasksList.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackupFromUri(
        uri: Uri,
        passphrase: String,
        replaceExisting: Boolean
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        try {
            val rawText = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext Result.failure(IllegalStateException("Unable to read backup file."))

            val envelope = JSONObject(rawText)
            if (envelope.optString("format") != "wolf_study_backup_v1") {
                return@withContext Result.failure(IllegalArgumentException("Unsupported backup file format."))
            }

            val dataJson = if (envelope.optBoolean("encrypted", false)) {
                if (passphrase.isBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("This backup is encrypted. Enter its passphrase to restore."))
                }
                val decrypted = decryptAesGcm(
                    ivBase64 = envelope.getString("iv"),
                    cipherBase64 = envelope.getString("payload"),
                    passphrase = passphrase
                )
                JSONObject(decrypted)
            } else {
                envelope.getJSONObject("data")
            }

            ensureSevenFixedSubjectsSeeded()
            if (replaceExisting) {
                dao.clearAllStudyData()
            }

            val notesArr = dataJson.optJSONArray("notes") ?: JSONArray()
            for (i in 0 until notesArr.length()) {
                val obj = notesArr.getJSONObject(i)
                dao.upsertNote(
                    NoteEntity(
                        subjectId = obj.optInt("subjectId", 1).coerceIn(1, 7),
                        title = obj.optString("title", "Restored Note"),
                        content = obj.optString("content", ""),
                        isBookmarked = obj.optBoolean("isBookmarked", false),
                        sourceTag = obj.optString("sourceTag", "MANUAL"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val bookIdMap = mutableMapOf<Long, Long>()
            val booksArr = dataJson.optJSONArray("pdfBooks") ?: JSONArray()
            var restoredBooks = 0
            for (i in 0 until booksArr.length()) {
                val obj = booksArr.getJSONObject(i)
                val path = obj.optString("localPath", "")
                if (path.isNotBlank() && java.io.File(path).exists()) {
                    val legacyId = obj.optLong("legacyId", -1L)
                    val newId = dao.upsertPdfBook(
                        PdfBookEntity(
                            subjectId = obj.optInt("subjectId", 1).coerceIn(1, 7),
                            title = obj.optString("title", "PDF Book"),
                            localPath = path,
                            fileName = obj.optString("fileName", "document.pdf"),
                            pageCount = obj.optInt("pageCount", 1),
                            lastReadPage = obj.optInt("lastReadPage", 0),
                            isBookmarked = obj.optBoolean("isBookmarked", false),
                            addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                        )
                    )
                    if (legacyId > 0) bookIdMap[legacyId] = newId
                    restoredBooks++
                }
            }

            val pageBookmarksArr = dataJson.optJSONArray("pdfPageBookmarks") ?: JSONArray()
            for (i in 0 until pageBookmarksArr.length()) {
                val obj = pageBookmarksArr.getJSONObject(i)
                val mappedBookId = bookIdMap[obj.optLong("legacyBookId", -1L)]
                if (mappedBookId != null) {
                    dao.upsertPdfPageBookmark(
                        PdfPageBookmarkEntity(
                            bookId = mappedBookId,
                            pageNumber = obj.optInt("pageNumber", 0),
                            noteLabel = obj.optString("noteLabel", "Bookmark"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val impArr = dataJson.optJSONArray("importantQuestions") ?: JSONArray()
            for (i in 0 until impArr.length()) {
                val obj = impArr.getJSONObject(i)
                dao.upsertImportantQuestion(
                    ImportantQuestionEntity(
                        subjectId = obj.optInt("subjectId", 1).coerceIn(1, 7),
                        question = obj.optString("question", ""),
                        answer = obj.optString("answer", ""),
                        priority = obj.optString("priority", "HIGH"),
                        isBookmarked = obj.optBoolean("isBookmarked", false),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val quizArr = dataJson.optJSONArray("quizQuestions") ?: JSONArray()
            for (i in 0 until quizArr.length()) {
                val obj = quizArr.getJSONObject(i)
                dao.upsertQuizQuestion(
                    QuizQuestionEntity(
                        subjectId = obj.optInt("subjectId", 1).coerceIn(1, 7),
                        question = obj.optString("question", ""),
                        optionA = obj.optString("optionA", ""),
                        optionB = obj.optString("optionB", ""),
                        optionC = obj.optString("optionC", ""),
                        optionD = obj.optString("optionD", ""),
                        correctIndex = obj.optInt("correctIndex", 0).coerceIn(0, 3),
                        explanation = obj.optString("explanation", ""),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val assignArr = dataJson.optJSONArray("assignments") ?: JSONArray()
            for (i in 0 until assignArr.length()) {
                val obj = assignArr.getJSONObject(i)
                dao.upsertAssignment(
                    AssignmentEntity(
                        subjectId = obj.optInt("subjectId", 1).coerceIn(1, 7),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        dueDateMillis = obj.optLong("dueDateMillis", System.currentTimeMillis()),
                        status = obj.optString("status", "PENDING"),
                        isBookmarked = obj.optBoolean("isBookmarked", false),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val diaryArr = dataJson.optJSONArray("diaryEntries") ?: JSONArray()
            for (i in 0 until diaryArr.length()) {
                val obj = diaryArr.getJSONObject(i)
                dao.upsertDiaryEntry(
                    DiaryEntryEntity(
                        dateKey = obj.optString("dateKey", ""),
                        title = obj.optString("title", ""),
                        learningRecord = obj.optString("learningRecord", ""),
                        studyMinutes = obj.optInt("studyMinutes", 60),
                        focusRating = obj.optInt("focusRating", 4).coerceIn(1, 5),
                        subjectId = if (obj.has("subjectId")) obj.optInt("subjectId", 1).coerceIn(1, 7) else null,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val timetableArr = dataJson.optJSONArray("timetableSlots") ?: JSONArray()
            for (i in 0 until timetableArr.length()) {
                val obj = timetableArr.getJSONObject(i)
                dao.upsertTimetableSlot(
                    TimetableSlotEntity(
                        subjectId = obj.optInt("subjectId", 1).coerceIn(1, 7),
                        dayOfWeek = obj.optInt("dayOfWeek", 1).coerceIn(1, 7),
                        startMinutes = obj.optInt("startMinutes", 480),
                        endMinutes = obj.optInt("endMinutes", 540),
                        topicNote = obj.optString("topicNote", "")
                    )
                )
            }

            val tasksArr = dataJson.optJSONArray("studyTasks") ?: JSONArray()
            for (i in 0 until tasksArr.length()) {
                val obj = tasksArr.getJSONObject(i)
                dao.upsertTask(
                    StudyTaskEntity(
                        subjectId = obj.optInt("subjectId", 1).coerceIn(1, 7),
                        title = obj.optString("title", ""),
                        deadlineMillis = obj.optLong("deadlineMillis", System.currentTimeMillis()),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        priority = obj.optString("priority", "MEDIUM"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            Result.success(
                BackupSummary(
                    notesCount = notesArr.length(),
                    booksCount = restoredBooks,
                    questionsCount = impArr.length(),
                    quizzesCount = quizArr.length(),
                    assignmentsCount = assignArr.length(),
                    diaryCount = diaryArr.length(),
                    timetableCount = timetableArr.length(),
                    tasksCount = tasksArr.length()
                )
            )
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.localizedMessage ?: "Failed to restore backup. Check passphrase or file format.", e))
        }
    }

    private fun deriveAesKey(passphrase: String): SecretKeySpec {
        val hash = MessageDigest.getInstance("SHA-256").digest(passphrase.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(hash, "AES")
    }

    private fun encryptAesGcm(plainText: String, passphrase: String): Pair<String, String> {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveAesKey(passphrase), GCMParameterSpec(128, iv))
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv, Base64.NO_WRAP) to Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    private fun decryptAesGcm(ivBase64: String, cipherBase64: String, passphrase: String): String {
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val encrypted = Base64.decode(cipherBase64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, deriveAesKey(passphrase), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}
