package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val shortCode: String,
    val category: String,
    val description: String,
    val accentColorHex: Long
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Int,
    val title: String,
    val content: String,
    val isBookmarked: Boolean = false,
    val sourceTag: String = "MANUAL", // MANUAL, OCR, AI
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pdf_books")
data class PdfBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Int,
    val title: String,
    val localPath: String,
    val fileName: String,
    val pageCount: Int = 1,
    val lastReadPage: Int = 0,
    val isBookmarked: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pdf_page_bookmarks")
data class PdfPageBookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int,
    val noteLabel: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "important_questions")
data class ImportantQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Int,
    val question: String,
    val answer: String,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val isBookmarked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Int,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int, // 0..3
    val explanation: String = "",
    val lastResultCorrect: Boolean? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Int,
    val title: String,
    val description: String,
    val dueDateMillis: Long,
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val isBookmarked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String, // yyyy-MM-dd
    val title: String,
    val learningRecord: String,
    val studyMinutes: Int = 60,
    val focusRating: Int = 4, // 1..5
    val subjectId: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Int,
    val dayOfWeek: Int, // 1=Mon..7=Sun
    val startMinutes: Int, // minutes from midnight
    val endMinutes: Int,
    val topicNote: String = ""
)

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Int,
    val title: String,
    val deadlineMillis: Long,
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val createdAt: Long = System.currentTimeMillis()
)

object FixedSubjects {
    val ALL: List<SubjectEntity> = listOf(
        SubjectEntity(
            id = 1,
            name = "Education (Major)",
            shortCode = "EDU",
            category = "Major Core",
            description = "Core pedagogical theory, educational psychology, philosophy & research.",
            accentColorHex = 0xFFF5C451L
        ),
        SubjectEntity(
            id = 2,
            name = "Assamese (Minor)",
            shortCode = "ASM",
            category = "Minor Core",
            description = "Assamese literature, grammar, Sahitya Sabha heritage & composition.",
            accentColorHex = 0xFF60A5FAL
        ),
        SubjectEntity(
            id = 3,
            name = "AEC",
            shortCode = "AEC",
            category = "Ability Enhancement",
            description = "Ability Enhancement Course — communication, language & academic skills.",
            accentColorHex = 0xFF34D399L
        ),
        SubjectEntity(
            id = 4,
            name = "GEC",
            shortCode = "GEC",
            category = "Generic Elective",
            description = "Generic Elective Course — interdisciplinary academic breadth.",
            accentColorHex = 0xFFA78BFAL
        ),
        SubjectEntity(
            id = 5,
            name = "VAC",
            shortCode = "VAC",
            category = "Value Added",
            description = "Value Added Course — ethics, civic studies, wellness & constitutional values.",
            accentColorHex = 0xFFFB923CL
        ),
        SubjectEntity(
            id = 6,
            name = "SEC",
            shortCode = "SEC",
            category = "Skill Enhancement",
            description = "Skill Enhancement Course — practical competencies & applied training.",
            accentColorHex = 0xFF2DD4BFL
        ),
        SubjectEntity(
            id = 7,
            name = "Government New Updates",
            shortCode = "GOV",
            category = "Current Affairs",
            description = "Government policies, schemes, notifications, competitive exam & syllabus updates.",
            accentColorHex = 0xFFF87171L
        )
    )
}
