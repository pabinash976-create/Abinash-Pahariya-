package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai.configureAppCheck
import com.example.ui.AiAssistantViewModel
import com.example.ui.TOP_NAV_TABS
import com.example.ui.WolfRoutes
import com.example.ui.WolfStudyViewModel
import com.example.ui.screens.PrivacyPinLockScreen
import com.example.ui.screens.WolfSplashScreen
import com.example.ui.theme.WolfStudyTheme
import com.example.ui.wolfStudyNavGraph

/**
 * Main entry point for WOLF STUDY.
 * Configures App Check, edge-to-edge layout, Dark Navy theme, HashRouter-like route state,
 * root Material 3 Scaffold, and Jetpack Navigation NavHost across all study modules
 * including the 7 fixed academic subjects (#/subject/1..#/subject/7).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before Compose UI or ViewModels.
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        val initialHash = intent?.data?.fragment
        setContent {
            WolfStudyEntry(initialHashFragment = initialHash)
        }
    }
}

/**
 * HashRouter-like state holder synchronizing `#/...` hash paths with [NavHostController].
 * Supports top-level routes (#/home, #/subjects, #/notes, #/library, #/ai),
 * all 7 study subject modules (#/subject/1..#/subject/7), and secondary study tools
 * (#/diary, #/timetable, #/tasks, #/bookmarks, #/scanner, #/backup, #/settings).
 */
@Stable
class WolfHashRouterState(
    val navController: NavHostController,
    initialHash: String
) {
    var currentHash by mutableStateOf(normalizeHash(initialHash))
        internal set

    fun navigateHash(hashPath: String, topLevelTab: Boolean = false) {
        val normalized = normalizeHash(hashPath)
        val targetRoute = hashToRoute(normalized)
        currentHash = normalized
        if (topLevelTab) {
            navController.navigate(targetRoute) {
                popUpTo(WolfRoutes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(targetRoute) {
                launchSingleTop = true
            }
        }
    }

    fun syncFromBackStack(entry: NavBackStackEntry?) {
        val pattern = entry?.destination?.route ?: return
        val resolvedHash = when (pattern) {
            WolfRoutes.HOME -> "#/home"
            WolfRoutes.SUBJECTS -> "#/subjects"
            WolfRoutes.SUBJECT_DETAIL -> {
                val id = entry.arguments?.getInt("subjectId") ?: 1
                "#/subject/$id"
            }
            WolfRoutes.NOTES -> "#/notes"
            WolfRoutes.NOTE_EDITOR -> {
                val noteId = entry.arguments?.getLong("noteId") ?: 0L
                "#/note/$noteId"
            }
            WolfRoutes.LIBRARY -> "#/library"
            WolfRoutes.PDF_READER -> {
                val bookId = entry.arguments?.getLong("bookId") ?: 0L
                "#/pdf/$bookId"
            }
            WolfRoutes.AI -> "#/ai"
            WolfRoutes.DIARY -> "#/diary"
            WolfRoutes.TIMETABLE -> "#/timetable"
            WolfRoutes.TASKS -> "#/tasks"
            WolfRoutes.BOOKMARKS -> "#/bookmarks"
            WolfRoutes.SCANNER -> "#/scanner"
            WolfRoutes.BACKUP -> "#/backup"
            WolfRoutes.SETTINGS -> "#/settings"
            else -> "#/home"
        }
        if (currentHash != resolvedHash) {
            currentHash = resolvedHash
        }
    }

    companion object {
        fun normalizeHash(raw: String?): String {
            val cleaned = raw?.trim().orEmpty()
            if (cleaned.isEmpty()) return "#/home"
            return when {
                cleaned.startsWith("#/") -> cleaned
                cleaned.startsWith("#") -> "#/${cleaned.removePrefix("#")}"
                cleaned.startsWith("/") -> "#$cleaned"
                else -> "#/$cleaned"
            }
        }

        fun hashToRoute(hashPath: String): String {
            val path = normalizeHash(hashPath).removePrefix("#/")
            val segments = path.split("/").filter { it.isNotBlank() }
            return when (segments.firstOrNull()) {
                null, "home" -> WolfRoutes.HOME
                "subjects" -> WolfRoutes.SUBJECTS
                "subject" -> {
                    val subjectId = segments.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 7) ?: 1
                    WolfRoutes.subjectDetail(subjectId)
                }
                "notes" -> WolfRoutes.NOTES
                "note" -> {
                    val noteId = segments.getOrNull(1)?.toLongOrNull() ?: 0L
                    WolfRoutes.noteEditor(noteId)
                }
                "library" -> WolfRoutes.LIBRARY
                "pdf" -> {
                    val bookId = segments.getOrNull(1)?.toLongOrNull() ?: 0L
                    WolfRoutes.pdfReader(bookId)
                }
                "ai", "ai-search" -> WolfRoutes.AI
                "diary" -> WolfRoutes.DIARY
                "timetable" -> WolfRoutes.TIMETABLE
                "tasks" -> WolfRoutes.TASKS
                "bookmarks" -> WolfRoutes.BOOKMARKS
                "scanner" -> WolfRoutes.SCANNER
                "backup" -> WolfRoutes.BACKUP
                "settings" -> WolfRoutes.SETTINGS
                else -> WolfRoutes.HOME
            }
        }
    }
}

@Composable
fun rememberWolfHashRouter(
    navController: NavHostController = rememberNavController(),
    initialHashFragment: String? = null
): WolfHashRouterState {
    var savedHash by rememberSaveable {
        mutableStateOf(WolfHashRouterState.normalizeHash(initialHashFragment))
    }
    val router = remember(navController) {
        WolfHashRouterState(navController = navController, initialHash = savedHash)
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry) {
        router.syncFromBackStack(backStackEntry)
        savedHash = router.currentHash
    }
    return router
}

@Composable
fun WolfStudyEntry(initialHashFragment: String? = null) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val mainViewModel: WolfStudyViewModel = viewModel(factory = WolfStudyViewModel.Factory(app))
    val aiViewModel: AiAssistantViewModel = viewModel(factory = AiAssistantViewModel.Factory(mainViewModel.repository))

    val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
    val splashFinished by mainViewModel.splashFinished.collectAsStateWithLifecycle()
    val isUnlocked by mainViewModel.isUnlocked.collectAsStateWithLifecycle()

    // Dark Navy background theme configuration (Midnight Wolf default: #061324, Silver, White & Gold accents)
    WolfStudyTheme(
        darkTheme = preferences.darkThemeEnabled,
        preset = preferences.themePreset
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                !splashFinished -> {
                    WolfSplashScreen(onFinished = { mainViewModel.markSplashFinished() })
                }
                !isUnlocked -> {
                    PrivacyPinLockScreen(onVerifyPin = { pin -> mainViewModel.tryUnlockWithPin(pin) })
                }
                else -> {
                    WolfStudyScaffoldShell(
                        mainViewModel = mainViewModel,
                        aiViewModel = aiViewModel,
                        initialHashFragment = initialHashFragment
                    )
                }
            }
        }
    }
}

@Composable
private fun WolfStudyScaffoldShell(
    mainViewModel: WolfStudyViewModel,
    aiViewModel: AiAssistantViewModel,
    initialHashFragment: String?
) {
    val navController = rememberNavController()
    val hashRouter = rememberWolfHashRouter(
        navController = navController,
        initialHashFragment = initialHashFragment
    )
    val toastMessage by mainViewModel.toastMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (hashRouter.currentHash != "#/home") {
            hashRouter.navigateHash(hashRouter.currentHash)
        }
    }

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            mainViewModel.clearToastMessage()
        }
    }

    val currentHash = hashRouter.currentHash
    val showTopLevelNav = TOP_NAV_TABS.any { it.hashPath == currentHash }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopLevelNav && !useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        TOP_NAV_TABS.forEach { tab ->
                            val selected = currentHash == tab.hashPath
                            NavigationBarItem(
                                selected = selected,
                                onClick = { hashRouter.navigateHash(tab.hashPath, topLevelTab = true) },
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
                            val selected = currentHash == tab.hashPath
                            NavigationRailItem(
                                selected = selected,
                                onClick = { hashRouter.navigateHash(tab.hashPath, topLevelTab = true) },
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = WolfRoutes.HOME
                    ) {
                        wolfStudyNavGraph(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            aiViewModel = aiViewModel,
                            navigateTopTab = { route ->
                                hashRouter.navigateHash("#/$route", topLevelTab = true)
                            }
                        )
                    }
                }
            }
        }
    }
}
