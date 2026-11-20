package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SubjectEntity::class,
        NoteEntity::class,
        PdfBookEntity::class,
        PdfPageBookmarkEntity::class,
        ImportantQuestionEntity::class,
        QuizQuestionEntity::class,
        AssignmentEntity::class,
        DiaryEntryEntity::class,
        TimetableSlotEntity::class,
        StudyTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WolfStudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: WolfStudyDatabase? = null

        fun getInstance(context: Context): WolfStudyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WolfStudyDatabase::class.java,
                    "wolf_study.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
