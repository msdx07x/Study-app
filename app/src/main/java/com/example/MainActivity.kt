package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import com.example.data.entity.ChapterEntity
import com.example.data.entity.CourseEntity
import com.example.ui.courses.CourseDetailScreen
import com.example.ui.courses.ImportCourseSheet
import com.example.ui.courses.MyCoursesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.ProgressTrackerScreen
import com.example.ui.player.LessonScreen
import com.example.ui.reminders.RemindersScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.StudyOSTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel

sealed class Screen {
    data object Home : Screen()
    data object Courses : Screen()
    data object Reminders : Screen()
    data object Settings : Screen()
    data object ProgressTracker : Screen()
    data class CourseDetail(val courseId: Long) : Screen()
    data class Lesson(val courseId: Long, val chapterId: Long) : Screen()
}

enum class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, Screen.Home, "nav_home"),
    COURSES("My Courses", Icons.Filled.School, Icons.Outlined.School, Screen.Courses, "nav_courses"),
    REMINDERS("Reminders", Icons.Filled.Alarm, Icons.Outlined.Alarm, Screen.Reminders, "nav_reminders"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, Screen.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            StudyOSTheme(darkTheme = isDark) {
                StudyOSApp(viewModel = viewModel, intent = intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun StudyOSApp(viewModel: MainViewModel, intent: Intent?) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var screenBackstack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    var showImportSheet by remember { mutableStateOf(false) }

    // Request notification permission on Android 13+
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle incoming deep link or notification click
    LaunchedEffect(intent) {
        val navTarget = intent?.getStringExtra("EXTRA_NAVIGATE_TO")
        val courseId = intent?.getLongExtra("EXTRA_COURSE_ID", -1L) ?: -1L
        val chapterId = intent?.getLongExtra("EXTRA_CHAPTER_ID", -1L) ?: -1L

        if (navTarget == "lesson" && courseId != -1L && chapterId != -1L) {
            currentScreen = Screen.Lesson(courseId, chapterId)
            screenBackstack = listOf(Screen.Home, Screen.CourseDetail(courseId), Screen.Lesson(courseId, chapterId))
        }
    }

    // Data observation from ViewModel
    val totalCourses by viewModel.totalCoursesCount.collectAsState()
    val totalChapters by viewModel.totalChaptersCount.collectAsState()
    val completedChapters by viewModel.completedChaptersCount.collectAsState()
    val pendingChapters by viewModel.pendingChaptersCount.collectAsState()
    val mostRecentIncompleteChapter by viewModel.mostRecentIncompleteChapter.collectAsState()
    val recentChapters by viewModel.recentlyWatchedChapters.collectAsState()
    val courses by viewModel.allCourses.collectAsState()
    val reminders by viewModel.allReminders.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val youtubeApiKey by viewModel.youtubeApiKey.collectAsState()
    val defaultRepeatInterval by viewModel.defaultRepeatInterval.collectAsState()
    val autoResumeEnabled by viewModel.autoResumeEnabled.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importError by viewModel.importError.collectAsState()

    // Map lookups for fast rendering
    val coursesMap = remember(courses) { courses.associateBy { it.id } }
    val continueCourse = remember(mostRecentIncompleteChapter, coursesMap) {
        mostRecentIncompleteChapter?.let { coursesMap[it.courseId] }
    }

    fun navigateTo(screen: Screen) {
        if (screen is Screen.Home || screen is Screen.Courses || screen is Screen.Reminders || screen is Screen.Settings) {
            screenBackstack = listOf(screen)
        } else {
            screenBackstack = screenBackstack + screen
        }
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenBackstack.size > 1) {
            val newStack = screenBackstack.dropLast(1)
            screenBackstack = newStack
            currentScreen = newStack.last()
        } else {
            currentScreen = Screen.Home
            screenBackstack = listOf(Screen.Home)
        }
    }

    // BackHandler for sub-screens
    BackHandler(enabled = currentScreen !is Screen.Home && currentScreen !is Screen.Courses && currentScreen !is Screen.Reminders && currentScreen !is Screen.Settings) {
        navigateBack()
    }

    val isTopLevel = currentScreen is Screen.Home ||
            currentScreen is Screen.Courses ||
            currentScreen is Screen.Reminders ||
            currentScreen is Screen.Settings

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavItem.entries.forEach { item ->
                        val isSelected = when (item.screen) {
                            Screen.Home -> currentScreen is Screen.Home
                            Screen.Courses -> currentScreen is Screen.Courses
                            Screen.Reminders -> currentScreen is Screen.Reminders
                            Screen.Settings -> currentScreen is Screen.Settings
                            else -> false
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { navigateTo(item.screen) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = ElectricBlue,
                                indicatorColor = ElectricBlue,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Home -> {
                        val todayReminders = remember(reminders) {
                            reminders.filter { it.enabled }
                        }
                        HomeScreen(
                            totalCourses = totalCourses,
                            completedChapters = completedChapters,
                            pendingChapters = pendingChapters,
                            continueChapter = mostRecentIncompleteChapter,
                            continueCourse = continueCourse,
                            recentChapters = recentChapters,
                            todayReminders = todayReminders,
                            coursesMap = coursesMap,
                            onOpenLesson = { cId, chId ->
                                navigateTo(Screen.Lesson(cId, chId))
                            },
                            onNavigateToCourses = { navigateTo(Screen.Courses) },
                            onNavigateToReminders = { navigateTo(Screen.Reminders) },
                            onImportClick = {
                                viewModel.clearImportError()
                                showImportSheet = true
                            },
                            onLoadStarterCourse = {
                                viewModel.loadStarterCourse(0) { courseId ->
                                    navigateTo(Screen.CourseDetail(courseId))
                                }
                            },
                            onOpenProgressTracker = {
                                navigateTo(Screen.ProgressTracker)
                            }
                        )
                    }

                    is Screen.ProgressTracker -> {
                        ProgressTrackerScreen(
                            totalCourses = totalCourses,
                            completedChapters = completedChapters,
                            totalChapters = totalChapters,
                            courses = courses,
                            completedList = recentChapters,
                            onNavigateBack = { navigateBack() },
                            onOpenCourse = { cId -> navigateTo(Screen.CourseDetail(cId)) },
                            onOpenLesson = { cId, chId -> navigateTo(Screen.Lesson(cId, chId)) }
                        )
                    }

                    is Screen.Courses -> {
                        MyCoursesScreen(
                            courses = courses,
                            courseStats = emptyMap(),
                            onOpenCourse = { cId ->
                                navigateTo(Screen.CourseDetail(cId))
                            },
                            onContinueCourse = { cId ->
                                navigateTo(Screen.CourseDetail(cId))
                            },
                            onDeleteCourse = { cId ->
                                viewModel.deleteCourse(cId)
                            },
                            onRenameCourse = { cId, newTitle ->
                                viewModel.renameCourse(cId, newTitle)
                            },
                            onImportClick = {
                                viewModel.clearImportError()
                                showImportSheet = true
                            }
                        )
                    }

                    is Screen.Reminders -> {
                        val allChaptersMap = remember(courses) { mutableMapOf<Long, ChapterEntity>() }
                        RemindersScreen(
                            reminders = reminders,
                            chaptersMap = allChaptersMap,
                            coursesMap = coursesMap,
                            onToggleReminder = { rId, enabled ->
                                viewModel.toggleReminder(rId, enabled)
                            },
                            onDeleteReminder = { rId ->
                                viewModel.deleteReminder(rId)
                            },
                            onOpenLesson = { cId, chId ->
                                navigateTo(Screen.Lesson(cId, chId))
                            }
                        )
                    }

                    is Screen.Settings -> {
                        SettingsScreen(
                            currentTheme = currentTheme,
                            youtubeApiKey = youtubeApiKey,
                            defaultRepeatInterval = defaultRepeatInterval,
                            autoResumeEnabled = autoResumeEnabled,
                            onSetTheme = { viewModel.setThemeMode(it) },
                            onSaveApiKey = { viewModel.saveApiKey(it) },
                            onSetDefaultRepeatInterval = { viewModel.setDefaultRepeatInterval(it) },
                            onSetAutoResume = { viewModel.setAutoResumeEnabled(it) },
                            onClearAllData = { viewModel.clearAllData() }
                        )
                    }

                    is Screen.CourseDetail -> {
                        val activeCourse by viewModel.getCourseFlow(screen.courseId).collectAsState(initial = null)
                        val chapters by viewModel.getChaptersFlow(screen.courseId).collectAsState(initial = emptyList())

                        CourseDetailScreen(
                            course = activeCourse,
                            chapters = chapters,
                            onNavigateBack = { navigateBack() },
                            onOpenLesson = { cId, chId ->
                                navigateTo(Screen.Lesson(cId, chId))
                            },
                            onToggleCompletion = { chId, completed ->
                                viewModel.toggleChapterCompletion(chId, completed)
                            },
                            onDeleteCourse = { cId ->
                                viewModel.deleteCourse(cId)
                                navigateBack()
                            },
                            onRenameCourse = { cId, newTitle ->
                                viewModel.renameCourse(cId, newTitle)
                            },
                            onSaveReminder = { reminder ->
                                viewModel.scheduleReminder(reminder)
                            }
                        )
                    }

                    is Screen.Lesson -> {
                        val activeCourse by viewModel.getCourseFlow(screen.courseId).collectAsState(initial = null)
                        val activeChapter by viewModel.getChapterFlow(screen.chapterId).collectAsState(initial = null)
                        val chapters by viewModel.getChaptersFlow(screen.courseId).collectAsState(initial = emptyList())

                        LessonScreen(
                            course = activeCourse,
                            chapter = activeChapter,
                            allChapters = chapters,
                            onNavigateBack = { navigateBack() },
                            onSelectChapter = { ch ->
                                navigateTo(Screen.Lesson(screen.courseId, ch.id))
                            },
                            onUpdatePosition = { posSeconds ->
                                activeChapter?.let { ch ->
                                    viewModel.updatePlaybackPosition(screen.courseId, ch.id, ch.videoId, posSeconds)
                                }
                            },
                            onToggleCompletion = { chId, completed ->
                                viewModel.toggleChapterCompletion(chId, completed)
                            },
                            onSaveNotes = { chId, notes ->
                                viewModel.saveChapterNotes(chId, notes)
                            },
                            onSaveReminder = { reminder ->
                                viewModel.scheduleReminder(reminder)
                            }
                        )
                    }
                }
            }
        }
    }

    // Import Course Sheet
    if (showImportSheet) {
        ImportCourseSheet(
            apiKey = youtubeApiKey,
            isLoading = isImporting,
            errorMessage = importError,
            onDismiss = {
                showImportSheet = false
                viewModel.clearImportError()
            },
            onImportUrl = { url ->
                viewModel.importPlaylistUrl(url) { courseId ->
                    showImportSheet = false
                    navigateTo(Screen.CourseDetail(courseId))
                }
            },
            onLoadStarterCourse = { index ->
                viewModel.loadStarterCourse(index) { courseId ->
                    showImportSheet = false
                    navigateTo(Screen.CourseDetail(courseId))
                }
            },
            onNavigateToSettings = {
                showImportSheet = false
                navigateTo(Screen.Settings)
            }
        )
    }
}
