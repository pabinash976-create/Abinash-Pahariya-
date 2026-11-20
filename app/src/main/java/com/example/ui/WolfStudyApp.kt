package com.example.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.BackupRestoreScreen
import com.example.ui.screens.BookScannerScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DigitalLibraryScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesListScreen
import com.example.ui.screens.PdfReaderScreen
import com.example.ui.screens.PrivacyPinLockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyDiaryScreen
import com.example.ui.screens.SubjectDetailScreen
import com.example.ui.screens.SubjectsListScreen
import com.example.ui.screens.TaskManagerScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.screens.WolfSplashScreen
import com.example.ui.theme.WolfStudyTheme

object WolfRoutes {
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val NOTES = "notes"
    const val LIBRARY = "library"
    const val AI = "ai"
    const val SUBJECT_DETAIL = "subject_detail/{subjectId}"
    const val NOTE_EDITOR = "note_editor/{noteId}?subjectId={subjectId}"
    const val PDF_READER = "pdf_reader/{bookId}?page={page}"
    const val DIARY = "diary"
    const val TIMETABLE = "timetable"
    const val TASKS = "tasks"
    const val BOOKMARKS = "bookmarks"
    const val SCANNER = "scanner"
    const val BACKUP = "backup"
    const val SETTINGS = "settings"

    fun subjectDetail(subjectId: Int) = "subject_detail/$subjectId"
    fun noteEditor(noteId: Long, subjectId: Int = 1) = "note_editor/$noteId?subjectId=$subjectId"
    fun pdfReader(bookId: Long, page: Int = -1) = "pdf_reader/$bookId?page=$page"
}

internal data class TopNavTab(
    val route: String,
    val hashPath: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

internal val TOP_NAV_TABS = listOf(
    TopNavTab(WolfRoutes.HOME, "#/home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TopNavTab(WolfRoutes.SUBJECTS, "#/subjects", "Subjects", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    TopNavTab(WolfRoutes.NOTES, "#/notes", "Notes", Icons.Filled.EditNote, Icons.Outlined.EditNote),
    TopNavTab(WolfRoutes.LIBRARY, "#/library", "Library", Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks),
    TopNavTab(WolfRoutes.AI, "#/ai", "AI Tutor", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
)

@Composable
fun WolfStudyApp() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val mainViewModel: WolfStudyViewModel = viewModel(factory = WolfStudyViewModel.Factory(app))
    val aiViewModel: AiAssistantViewModel = viewModel(factory = AiAssistantViewModel.Factory(mainViewModel.repository))

    val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
    val splashFinished by mainViewModel.splashFinished.collectAsStateWithLifecycle()
    val isUnlocked by mainViewModel.isUnlocked.collectAsStateWithLifecycle()

    WolfStudyTheme(
        darkTheme = preferences.darkThemeEnabled,
        preset = preferences.themePreset
    ) {
        when {
            !splashFinished -> {
                WolfSplashScreen(onFinished = { mainViewModel.markSplashFinished() })
            }
            !isUnlocked -> {
                PrivacyPinLockScreen(onVerifyPin = { pin -> mainViewModel.tryUnlockWithPin(pin) })
            }
            else -> {
                WolfStudyMainShell(mainViewModel = mainViewModel, aiViewModel = aiViewModel)
            }
        }
    }
}

@Composable
private fun WolfStudyMainShell(
    mainViewModel: WolfStudyViewModel,
    aiViewModel: AiAssistantViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val toastMessage by mainViewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            mainViewModel.clearToastMessage()
        }
    }

    val showTopLevelNav = currentRoute in TOP_NAV_TABS.map { it.route }

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(WolfRoutes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopLevelNav && !useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        TOP_NAV_TABS.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelNav && useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        TOP_NAV_TABS.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_rail_${tab.route}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = WolfRoutes.HOME
                    ) {
                        wolfStudyNavGraph(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            aiViewModel = aiViewModel,
                            navigateTopTab = ::navigateTopTab
                        )
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.wolfStudyNavGraph(
    navController: NavHostController,
    mainViewModel: WolfStudyViewModel,
    aiViewModel: AiAssistantViewModel,
    navigateTopTab: (String) -> Unit
) {
    composable(WolfRoutes.HOME) {
        val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val notes by mainViewModel.notes.collectAsStateWithLifecycle()
        val pdfBooks by mainViewModel.pdfBooks.collectAsStateWithLifecycle()
        val tasks by mainViewModel.tasks.collectAsStateWithLifecycle()
        val assignments by mainViewModel.assignments.collectAsStateWithLifecycle()
        HomeDashboardScreen(
            preferences = preferences,
            subjects = subjects,
            notes = notes,
            pdfBooks = pdfBooks,
            tasks = tasks,
            assignments = assignments,
            onOpenSubjects = { navigateTopTab(WolfRoutes.SUBJECTS) },
            onOpenSubjectDetail = { id -> navController.navigate(WolfRoutes.subjectDetail(id)) },
            onOpenLibrary = { navigateTopTab(WolfRoutes.LIBRARY) },
            onOpenNotes = { navigateTopTab(WolfRoutes.NOTES) },
            onOpenNoteEditor = { noteId -> navController.navigate(WolfRoutes.noteEditor(noteId)) },
            onOpenDiary = { navController.navigate(WolfRoutes.DIARY) },
            onOpenTimetable = { navController.navigate(WolfRoutes.TIMETABLE) },
            onOpenTasks = { navController.navigate(WolfRoutes.TASKS) },
            onOpenBookmarks = { navController.navigate(WolfRoutes.BOOKMARKS) },
            onOpenAiAssistant = { navigateTopTab(WolfRoutes.AI) },
            onOpenAiSearch = { query ->
                aiViewModel.openAiSearch(query)
                navigateTopTab(WolfRoutes.AI)
            },
            onOpenBookScanner = { navController.navigate(WolfRoutes.SCANNER) },
            onOpenBackup = { navController.navigate(WolfRoutes.BACKUP) },
            onOpenSettings = { navController.navigate(WolfRoutes.SETTINGS) }
        )
    }

    composable(WolfRoutes.SUBJECTS) {
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val notes by mainViewModel.notes.collectAsStateWithLifecycle()
        val pdfBooks by mainViewModel.pdfBooks.collectAsStateWithLifecycle()
        val importantQuestions by mainViewModel.importantQuestions.collectAsStateWithLifecycle()
        val quizQuestions by mainViewModel.quizQuestions.collectAsStateWithLifecycle()
        val assignments by mainViewModel.assignments.collectAsStateWithLifecycle()
        SubjectsListScreen(
            subjects = subjects,
            notes = notes,
            pdfBooks = pdfBooks,
            importantQuestions = importantQuestions,
            quizQuestions = quizQuestions,
            assignments = assignments,
            onSelectSubject = { id -> navController.navigate(WolfRoutes.subjectDetail(id)) }
        )
    }

    composable(
        route = WolfRoutes.SUBJECT_DETAIL,
        arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
    ) { entry ->
        val subjId = entry.arguments?.getInt("subjectId") ?: 1
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val notes by mainViewModel.notes.collectAsStateWithLifecycle()
        val pdfBooks by mainViewModel.pdfBooks.collectAsStateWithLifecycle()
        val importantQuestions by mainViewModel.importantQuestions.collectAsStateWithLifecycle()
        val quizQuestions by mainViewModel.quizQuestions.collectAsStateWithLifecycle()
        val assignments by mainViewModel.assignments.collectAsStateWithLifecycle()
        val subject = subjects.firstOrNull { it.id == subjId } ?: subjects.first()
        SubjectDetailScreen(
            subject = subject,
            notes = notes.filter { it.subjectId == subject.id },
            pdfBooks = pdfBooks.filter { it.subjectId == subject.id },
            importantQuestions = importantQuestions.filter { it.subjectId == subject.id },
            quizQuestions = quizQuestions.filter { it.subjectId == subject.id },
            assignments = assignments.filter { it.subjectId == subject.id },
            onBack = { navController.popBackStack() },
            onOpenNoteEditor = { noteId, sId ->
                navController.navigate(WolfRoutes.noteEditor(noteId, sId))
            },
            onDeleteNote = mainViewModel::deleteNote,
            onToggleNoteBookmark = mainViewModel::toggleNoteBookmark,
            onImportPdf = { uri, sId, title ->
                mainViewModel.importPdf(uri, sId, title)
            },
            onOpenPdfReader = { bookId ->
                navController.navigate(WolfRoutes.pdfReader(bookId))
            },
            onDeletePdfBook = mainViewModel::deletePdfBook,
            onTogglePdfBookmark = mainViewModel::togglePdfBookBookmark,
            onSaveImportantQuestion = mainViewModel::saveImportantQuestion,
            onDeleteImportantQuestion = mainViewModel::deleteImportantQuestion,
            onToggleImportantQuestionBookmark = mainViewModel::toggleImportantQuestionBookmark,
            onSaveQuizQuestion = mainViewModel::saveQuizQuestion,
            onDeleteQuizQuestion = mainViewModel::deleteQuizQuestion,
            onRecordQuizAttempt = mainViewModel::recordQuizAttempt,
            onSaveAssignment = mainViewModel::saveAssignment,
            onDeleteAssignment = mainViewModel::deleteAssignment,
            onToggleAssignmentBookmark = mainViewModel::toggleAssignmentBookmark
        )
    }

    composable(WolfRoutes.NOTES) {
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val notes by mainViewModel.notes.collectAsStateWithLifecycle()
        NotesListScreen(
            subjects = subjects,
            notes = notes,
            onOpenNoteEditor = { noteId, sId ->
                navController.navigate(WolfRoutes.noteEditor(noteId, sId ?: 1))
            },
            onDeleteNote = mainViewModel::deleteNote,
            onToggleNoteBookmark = mainViewModel::toggleNoteBookmark,
            onOpenAiSearch = { query ->
                aiViewModel.openAiSearch(query)
                navigateTopTab(WolfRoutes.AI)
            }
        )
    }

    composable(
        route = WolfRoutes.NOTE_EDITOR,
        arguments = listOf(
            navArgument("noteId") { type = NavType.LongType },
            navArgument("subjectId") {
                type = NavType.IntType
                defaultValue = 1
            }
        )
    ) { entry ->
        val noteId = entry.arguments?.getLong("noteId") ?: 0L
        val subjId = entry.arguments?.getInt("subjectId") ?: 1
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val notes by mainViewModel.notes.collectAsStateWithLifecycle()
        val autoSaveStatus by mainViewModel.autoSaveStatus.collectAsStateWithLifecycle()
        val existing = notes.firstOrNull { it.id == noteId }
        NoteEditorScreen(
            existingNote = existing,
            initialSubjectId = subjId,
            subjects = subjects,
            autoSaveStatus = autoSaveStatus,
            onAutoSave = mainViewModel::autoSaveNote,
            onSaveImmediate = mainViewModel::saveNoteImmediate,
            onDelete = mainViewModel::deleteNote,
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.LIBRARY) {
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val pdfBooks by mainViewModel.pdfBooks.collectAsStateWithLifecycle()
        DigitalLibraryScreen(
            subjects = subjects,
            pdfBooks = pdfBooks,
            onImportPdf = { uri, sId, title ->
                mainViewModel.importPdf(uri, sId, title)
            },
            onOpenPdfReader = { bookId ->
                navController.navigate(WolfRoutes.pdfReader(bookId))
            },
            onUpdatePdfMetadata = mainViewModel::updatePdfMetadata,
            onDeletePdfBook = mainViewModel::deletePdfBook,
            onTogglePdfBookmark = mainViewModel::togglePdfBookBookmark,
            onOpenAiSearch = { query ->
                aiViewModel.openAiSearch(query)
                navigateTopTab(WolfRoutes.AI)
            }
        )
    }

    composable(
        route = WolfRoutes.PDF_READER,
        arguments = listOf(
            navArgument("bookId") { type = NavType.LongType },
            navArgument("page") {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { entry ->
        val bookId = entry.arguments?.getLong("bookId") ?: 0L
        val explicitPage = entry.arguments?.getInt("page") ?: -1
        val pdfBooks by mainViewModel.pdfBooks.collectAsStateWithLifecycle()
        val pageBookmarks by mainViewModel.pdfPageBookmarks.collectAsStateWithLifecycle()
        val book = pdfBooks.firstOrNull { it.id == bookId }
        val startPage = if (explicitPage >= 0) explicitPage else (book?.lastReadPage ?: 0)
        PdfReaderScreen(
            book = book,
            initialPage = startPage,
            pageBookmarks = pageBookmarks,
            renderPage = mainViewModel::renderPdfPage,
            onPageChanged = mainViewModel::updatePdfLastReadPage,
            onTogglePageBookmark = mainViewModel::togglePdfPageBookmark,
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.AI) {
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
        AiAssistantScreen(
            aiViewModel = aiViewModel,
            subjects = subjects,
            preferredLanguage = preferences.preferredLanguage,
            onOpenNote = { noteId -> navController.navigate(WolfRoutes.noteEditor(noteId)) },
            onOpenPdf = { bookId -> navController.navigate(WolfRoutes.pdfReader(bookId)) },
            onOpenSubject = { subjId -> navController.navigate(WolfRoutes.subjectDetail(subjId)) }
        )
    }

    composable(WolfRoutes.DIARY) {
        val diaryEntries by mainViewModel.diaryEntries.collectAsStateWithLifecycle()
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        StudyDiaryScreen(
            entries = diaryEntries,
            subjects = subjects,
            onSaveEntry = mainViewModel::saveDiaryEntry,
            onDeleteEntry = mainViewModel::deleteDiaryEntry,
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.TIMETABLE) {
        val timetableSlots by mainViewModel.timetableSlots.collectAsStateWithLifecycle()
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        TimetableScreen(
            slots = timetableSlots,
            subjects = subjects,
            onSaveSlot = mainViewModel::saveTimetableSlot,
            onDeleteSlot = mainViewModel::deleteTimetableSlot,
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.TASKS) {
        val tasks by mainViewModel.tasks.collectAsStateWithLifecycle()
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        TaskManagerScreen(
            tasks = tasks,
            subjects = subjects,
            onSaveTask = mainViewModel::saveTask,
            onToggleTask = mainViewModel::toggleTaskCompleted,
            onDeleteTask = mainViewModel::deleteTask,
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.BOOKMARKS) {
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        val notes by mainViewModel.notes.collectAsStateWithLifecycle()
        val pdfBooks by mainViewModel.pdfBooks.collectAsStateWithLifecycle()
        val pageBookmarks by mainViewModel.pdfPageBookmarks.collectAsStateWithLifecycle()
        val importantQuestions by mainViewModel.importantQuestions.collectAsStateWithLifecycle()
        val assignments by mainViewModel.assignments.collectAsStateWithLifecycle()
        BookmarksScreen(
            subjects = subjects,
            notes = notes,
            pdfBooks = pdfBooks,
            pageBookmarks = pageBookmarks,
            importantQuestions = importantQuestions,
            assignments = assignments,
            onOpenNote = { noteId -> navController.navigate(WolfRoutes.noteEditor(noteId)) },
            onOpenPdfAtPage = { bookId, page -> navController.navigate(WolfRoutes.pdfReader(bookId, page)) },
            onOpenSubject = { subjId -> navController.navigate(WolfRoutes.subjectDetail(subjId)) },
            onToggleNoteBookmark = mainViewModel::toggleNoteBookmark,
            onTogglePdfBookmark = mainViewModel::togglePdfBookBookmark,
            onRemovePageBookmark = mainViewModel::deletePdfPageBookmarkById,
            onToggleQuestionBookmark = mainViewModel::toggleImportantQuestionBookmark,
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.SCANNER) {
        val subjects by mainViewModel.subjects.collectAsStateWithLifecycle()
        BookScannerScreen(
            subjects = subjects,
            ocrProcessor = mainViewModel.ocrProcessor,
            onSaveScannedNote = { note ->
                mainViewModel.saveNoteImmediate(note) {
                    mainViewModel.showToast("Scanned text saved to Notes")
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.BACKUP) {
        val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
        val backupState by mainViewModel.backupState.collectAsStateWithLifecycle()
        BackupRestoreScreen(
            preferences = preferences,
            backupState = backupState,
            onExportBackup = mainViewModel::exportBackup,
            onImportBackup = mainViewModel::importBackup,
            onToggleHideProfile = mainViewModel::setHideProfileInBackup,
            onResetBackupState = mainViewModel::resetBackupState,
            onBack = { navController.popBackStack() }
        )
    }

    composable(WolfRoutes.SETTINGS) {
        val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
        SettingsScreen(
            preferences = preferences,
            onUpdateProfile = mainViewModel::updateProfile,
            onSetLanguage = mainViewModel::setLanguage,
            onSetDarkTheme = mainViewModel::setDarkTheme,
            onSetThemePreset = mainViewModel::setThemePreset,
            onSetPrivacyPin = mainViewModel::setPrivacyPin,
            onDisableAppLock = mainViewModel::disableAppLock,
            onOpenBackup = { navController.navigate(WolfRoutes.BACKUP) },
            onClearAllData = mainViewModel::clearAllStudyData,
            onBack = { navController.popBackStack() }
        )
    }
}
