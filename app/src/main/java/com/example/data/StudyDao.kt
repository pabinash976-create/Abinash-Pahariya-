package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // Subjects (exact 7 immutable subjects)
    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun observeSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY id ASC")
    suspend fun getSubjectsOnce(): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubjects(subjects: List<SubjectEntity>)

    // Notes
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAllNotesOnce(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE notes SET isBookmarked = :bookmarked, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setNoteBookmarked(id: Long, bookmarked: Boolean, updatedAt: Long = System.currentTimeMillis())

    // PDF Books & Page Bookmarks
    @Query("SELECT * FROM pdf_books ORDER BY addedAt DESC")
    fun observeAllPdfBooks(): Flow<List<PdfBookEntity>>

    @Query("SELECT * FROM pdf_books ORDER BY addedAt DESC")
    suspend fun getAllPdfBooksOnce(): List<PdfBookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPdfBook(book: PdfBookEntity): Long

    @Query("DELETE FROM pdf_books WHERE id = :id")
    suspend fun deletePdfBookById(id: Long)

    @Query("UPDATE pdf_books SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setPdfBookBookmarked(id: Long, bookmarked: Boolean)

    @Query("UPDATE pdf_books SET lastReadPage = :page WHERE id = :id")
    suspend fun updatePdfLastReadPage(id: Long, page: Int)

    @Query("SELECT * FROM pdf_page_bookmarks ORDER BY createdAt DESC")
    fun observeAllPdfPageBookmarks(): Flow<List<PdfPageBookmarkEntity>>

    @Query("SELECT * FROM pdf_page_bookmarks ORDER BY createdAt DESC")
    suspend fun getAllPdfPageBookmarksOnce(): List<PdfPageBookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPdfPageBookmark(bookmark: PdfPageBookmarkEntity): Long

    @Query("DELETE FROM pdf_page_bookmarks WHERE bookId = :bookId AND pageNumber = :pageNumber")
    suspend fun deletePdfPageBookmark(bookId: Long, pageNumber: Int)

    @Query("DELETE FROM pdf_page_bookmarks WHERE id = :id")
    suspend fun deletePdfPageBookmarkById(id: Long)

    @Query("DELETE FROM pdf_page_bookmarks WHERE bookId = :bookId")
    suspend fun deletePageBookmarksForBook(bookId: Long)

    // Important Questions
    @Query("SELECT * FROM important_questions ORDER BY updatedAt DESC")
    fun observeAllImportantQuestions(): Flow<List<ImportantQuestionEntity>>

    @Query("SELECT * FROM important_questions ORDER BY updatedAt DESC")
    suspend fun getAllImportantQuestionsOnce(): List<ImportantQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImportantQuestion(question: ImportantQuestionEntity): Long

    @Query("DELETE FROM important_questions WHERE id = :id")
    suspend fun deleteImportantQuestionById(id: Long)

    @Query("UPDATE important_questions SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setImportantQuestionBookmarked(id: Long, bookmarked: Boolean)

    // Quiz Questions
    @Query("SELECT * FROM quiz_questions ORDER BY updatedAt DESC")
    fun observeAllQuizQuestions(): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions ORDER BY updatedAt DESC")
    suspend fun getAllQuizQuestionsOnce(): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuizQuestion(question: QuizQuestionEntity): Long

    @Query("DELETE FROM quiz_questions WHERE id = :id")
    suspend fun deleteQuizQuestionById(id: Long)

    @Query("UPDATE quiz_questions SET lastResultCorrect = :correct, updatedAt = :updatedAt WHERE id = :id")
    suspend fun recordQuizAttempt(id: Long, correct: Boolean, updatedAt: Long = System.currentTimeMillis())

    // Assignments
    @Query("SELECT * FROM assignments ORDER BY dueDateMillis ASC")
    fun observeAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments ORDER BY dueDateMillis ASC")
    suspend fun getAllAssignmentsOnce(): List<AssignmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssignment(assignment: AssignmentEntity): Long

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Long)

    @Query("UPDATE assignments SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setAssignmentBookmarked(id: Long, bookmarked: Boolean)

    // Diary
    @Query("SELECT * FROM diary_entries ORDER BY dateKey DESC, createdAt DESC")
    fun observeAllDiaryEntries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries ORDER BY dateKey DESC, createdAt DESC")
    suspend fun getAllDiaryEntriesOnce(): List<DiaryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiaryEntry(entry: DiaryEntryEntity): Long

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteDiaryEntryById(id: Long)

    // Timetable
    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek ASC, startMinutes ASC")
    fun observeAllTimetableSlots(): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek ASC, startMinutes ASC")
    suspend fun getAllTimetableSlotsOnce(): List<TimetableSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTimetableSlot(slot: TimetableSlotEntity): Long

    @Query("DELETE FROM timetable_slots WHERE id = :id")
    suspend fun deleteTimetableSlotById(id: Long)

    // Tasks
    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, deadlineMillis ASC")
    fun observeAllTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, deadlineMillis ASC")
    suspend fun getAllTasksOnce(): List<StudyTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: StudyTaskEntity): Long

    @Update
    suspend fun updateTask(task: StudyTaskEntity)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // Clear all user-authored tables (preserving the 7 fixed subjects)
    @Query("DELETE FROM notes")
    suspend fun clearNotes()

    @Query("DELETE FROM pdf_books")
    suspend fun clearPdfBooks()

    @Query("DELETE FROM pdf_page_bookmarks")
    suspend fun clearPdfPageBookmarks()

    @Query("DELETE FROM important_questions")
    suspend fun clearImportantQuestions()

    @Query("DELETE FROM quiz_questions")
    suspend fun clearQuizQuestions()

    @Query("DELETE FROM assignments")
    suspend fun clearAssignments()

    @Query("DELETE FROM diary_entries")
    suspend fun clearDiaryEntries()

    @Query("DELETE FROM timetable_slots")
    suspend fun clearTimetableSlots()

    @Query("DELETE FROM study_tasks")
    suspend fun clearTasks()

    @Transaction
    suspend fun clearAllStudyData() {
        clearNotes()
        clearPdfPageBookmarks()
        clearPdfBooks()
        clearImportantQuestions()
        clearQuizQuestions()
        clearAssignments()
        clearDiaryEntries()
        clearTimetableSlots()
        clearTasks()
    }
}
