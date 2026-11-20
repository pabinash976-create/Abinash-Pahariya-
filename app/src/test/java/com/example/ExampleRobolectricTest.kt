package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AssignmentEntity
import com.example.data.DiaryEntryEntity
import com.example.data.FixedSubjects
import com.example.data.ImportantQuestionEntity
import com.example.data.NoteEntity
import com.example.data.QuizQuestionEntity
import com.example.data.StudyTaskEntity
import com.example.data.TimetableSlotEntity
import com.example.data.UserPreferencesRepository
import com.example.data.WolfStudyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: WolfStudyDatabase

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, WolfStudyDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `app name matches Wolf Study`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("Wolf Study", appName)
  }

  @Test
  fun `exact seven fixed subjects are configured`() {
    assertEquals(7, FixedSubjects.ALL.size)
    assertEquals(
      listOf(
        "Education (Major)",
        "Assamese (Minor)",
        "AEC",
        "GEC",
        "VAC",
        "SEC",
        "Government New Updates"
      ),
      FixedSubjects.ALL.map { it.name }
    )
  }

  @Test
  fun `room persists subjects notes tasks diary timetable questions quizzes and assignments`() = runTest {
    val dao = db.studyDao()
    dao.upsertSubjects(FixedSubjects.ALL)
    assertEquals(7, dao.getSubjectsOnce().size)

    val noteId = dao.upsertNote(
      NoteEntity(subjectId = 1, title = "Educational Psychology", content = "Constructivism notes", isBookmarked = true)
    )
    assertTrue(noteId > 0)
    assertEquals(1, dao.observeAllNotes().first().size)

    dao.upsertImportantQuestion(
      ImportantQuestionEntity(subjectId = 2, question = "Discuss Assamese literary heritage", answer = "Key points...", isBookmarked = true)
    )
    assertEquals(1, dao.getAllImportantQuestionsOnce().size)

    dao.upsertQuizQuestion(
      QuizQuestionEntity(
        subjectId = 3,
        question = "Effective communication requires?",
        optionA = "Clarity",
        optionB = "Noise",
        optionC = "Ambiguity",
        optionD = "None",
        correctIndex = 0
      )
    )
    assertEquals(1, dao.getAllQuizQuestionsOnce().size)

    dao.upsertAssignment(
      AssignmentEntity(subjectId = 4, title = "GEC Essay", description = "1500 words", dueDateMillis = 2_000_000L)
    )
    assertEquals(1, dao.getAllAssignmentsOnce().size)

    dao.upsertDiaryEntry(
      DiaryEntryEntity(dateKey = "2026-11-20", title = "Morning Revision", learningRecord = "Completed unit 1", studyMinutes = 90)
    )
    assertEquals(1, dao.getAllDiaryEntriesOnce().size)

    dao.upsertTimetableSlot(
      TimetableSlotEntity(subjectId = 5, dayOfWeek = 1, startMinutes = 540, endMinutes = 600, topicNote = "Ethics")
    )
    assertEquals(1, dao.getAllTimetableSlotsOnce().size)

    val taskId = dao.upsertTask(
      StudyTaskEntity(subjectId = 7, title = "Review Govt Schemes", deadlineMillis = 3_000_000L)
    )
    val savedTask = dao.getAllTasksOnce().first { it.id == taskId }
    dao.updateTask(savedTask.copy(isCompleted = true))
    assertTrue(dao.getAllTasksOnce().first { it.id == taskId }.isCompleted)
  }

  @Test
  fun `privacy pin hashing verifies matching pin only`() = runTest {
    val prefsRepo = UserPreferencesRepository(context)
    prefsRepo.setPrivacyPin("2468", true)
    val prefs = prefsRepo.preferencesFlow.first()
    assertTrue(prefs.appLockEnabled)
    assertTrue(prefsRepo.verifyPin("2468", prefs.privacyPinHash))
    assertFalse(prefsRepo.verifyPin("1234", prefs.privacyPinHash))
  }
}
