package com.huma.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.repository.TaskRepository
import com.huma.app.ui.screen.dashboard.DashboardScreen
import com.huma.app.ui.screen.LoginScreen
import com.huma.app.ui.screen.SplashScreen
import com.huma.app.ui.screen.task.AddTaskScreen
import com.huma.app.ui.screen.task.EditTaskScreen
import com.huma.app.ui.screen.task.TaskDetailScreen
import com.huma.app.ui.screen.task.TaskScreen
import com.huma.app.ui.screen.task.UpcomingTaskScreen
import com.huma.app.ui.viewmodel.TaskViewModel
import com.huma.app.ui.viewmodel.TaskViewModelFactory
import com.huma.app.ui.screen.focus.FocusScreen
import com.huma.app.ui.notification.createFocusNotificationChannel
import com.huma.app.ui.screen.analytics.AnalyticsScreen
import com.huma.app.ui.screen.lifearea.LifeAreaScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.huma.app.ui.screen.lifearea.AreaDetailScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.huma.app.ui.screen.note.NoteEditorScreen
import androidx.compose.runtime.LaunchedEffect
import com.huma.app.ui.screen.note.NoteScreen
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import com.huma.app.ui.screen.note.NoteData
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.huma.app.viewmodel.NoteViewModel
import com.huma.app.viewmodel.NoteViewModelFactory
import com.huma.app.viewmodel.StreakViewModel
import com.huma.app.viewmodel.StreakViewModelFactory
import com.huma.app.ui.screen.streak.StreakScreen
import com.huma.app.ui.notification.NotificationHelper
import com.huma.app.ui.notification.NotificationScheduler
import androidx.compose.ui.platform.LocalContext
import com.huma.app.ui.feature.MindGamesScreen
import com.huma.app.ui.feature.WheelScreen
import com.huma.app.ui.feature.TimeCapsuleScreen
import com.huma.app.ui.feature.CommitmentScreen
import com.huma.app.ui.feature.AddCommitmentScreen
import com.huma.app.viewmodel.CapsuleViewModel
import com.huma.app.viewmodel.CommitmentViewModel

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { splashProvider ->
            splashProvider.remove()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(
                "task_channel",
                "Task Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(taskChannel)

            createFocusNotificationChannel(this)
            NotificationHelper.init(this)
        }

        NotificationScheduler.scheduleAll(this)

        val database = AppDatabase.getInstance(this)
        val repository = TaskRepository(database.taskDao())

        val factory = TaskViewModelFactory(repository)
        val taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val noteFactory = NoteViewModelFactory(database.noteDao())
        val noteViewModel = ViewModelProvider(this, noteFactory)[NoteViewModel::class.java]
        
        val streakFactory = StreakViewModelFactory(database.streakDao())
        val streakViewModel = ViewModelProvider(this, streakFactory)[StreakViewModel::class.java]

        val capsuleViewModel = ViewModelProvider(this)[CapsuleViewModel::class.java]
        val commitmentViewModel = ViewModelProvider(this)[CommitmentViewModel::class.java]

        setContent {
            val context = LocalContext.current
            val navController: NavHostController = rememberNavController()
            val openNoteId = intent.getStringExtra("open_note_id")
            
            LaunchedEffect(openNoteId) {
                if (openNoteId != null) {
                    navController.navigate("notes_list")
                }
            }
            
            val noteEntities by noteViewModel.allNotes.collectAsState(initial = emptyList())
            val globalNotes = noteEntities.map { entity ->
                NoteData(entity.id, entity.title, entity.blocks, entity.date)
            }

            Surface(color = MaterialTheme.colorScheme.background) {
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                ) {
                    composable("splash") { SplashScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("dashboard") { DashboardScreen(navController, taskViewModel, commitmentViewModel) }
                    composable("tasks") { TaskScreen(taskViewModel, "all", navController) }
                    composable("tasks_today") { TaskScreen(taskViewModel, "today", navController) }
                    composable("tasks_upcoming") { UpcomingTaskScreen(taskViewModel, navController) }
                    composable("add_task/{type}") { backStackEntry ->
                        val type = backStackEntry.arguments?.getString("type")
                        AddTaskScreen(navController, taskViewModel, type == "upcoming")
                    }
                    composable("task_detail/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: return@composable
                        TaskDetailScreen(navController, taskId, taskViewModel)
                    }
                    composable("edit_task/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: return@composable
                        EditTaskScreen(taskId, navController, taskViewModel)
                    }
                    composable("focus") { FocusScreen(navController, taskViewModel) }
                    composable("streak") { StreakScreen(streakViewModel) }
                    composable("life_area") { LifeAreaScreen(navController, taskViewModel) }
                    composable("notes_list") {
                        NoteScreen(navController, globalNotes) { noteToDelete ->
                            noteViewModel.deleteNote(context, noteToDelete)
                        }
                    }
                    composable("wheel") { WheelScreen() }
                    composable("capsule") { TimeCapsuleScreen(capsuleViewModel) }
                    composable("mind_games") { MindGamesScreen(navController = navController) }
                    composable("commitments") { CommitmentScreen(navController = navController) }
                    composable(
                        route = "add_commitment?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: -1
                        AddCommitmentScreen(navController, if (id != -1) id else null)
                    }
                    composable(
                        route = "note_editor?noteId={noteId}",
                        arguments = listOf(navArgument("noteId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId")
                        NoteEditorScreen(navController, noteId, globalNotes) { newNote ->
                            noteViewModel.saveNote(context, newNote)
                        }
                    }
                    composable("analytics") { AnalyticsScreen(navController, taskViewModel) }
                    composable(
                        route = "area_detail/{areaName}",
                        arguments = listOf(navArgument("areaName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val areaName = backStackEntry.arguments?.getString("areaName") ?: ""
                        AreaDetailScreen(areaName, navController, taskViewModel)
                    }
                }
            }
        }
    }
}
