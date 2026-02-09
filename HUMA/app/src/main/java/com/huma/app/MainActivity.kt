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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // 🔥 TAMBAHKAN INI
import com.huma.app.ui.screen.note.NoteEditorScreen
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



class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // kalau false, notif memang tidak boleh muncul
        }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 🔥 Kuncinya di sini: Langsung hapus splash sistem tanpa animasi tambahan
        splashScreen.setOnExitAnimationListener { splashProvider ->
            splashProvider.remove()
        }

        // 🔔 REQUEST NOTIFICATION PERMISSION (ANDROID 13+)
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

        // 🔔 NOTIFICATION CHANNEL (WAJIB ANDROID 8+)
        // 🔔 NOTIFICATION CHANNELS (ANDROID 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Task Reminder (SUDAH ADA)
            val taskChannel = NotificationChannel(
                "task_channel",
                "Task Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(taskChannel)

            // 🔒 Focus Mode (BARU)
            createFocusNotificationChannel(this)
        }



        // 1. Inisialisasi Database yang benar (AppDatabase)
        val database = AppDatabase.getInstance(this)
        val repository = TaskRepository(database.taskDao())

        // 2. Siapkan ViewModel menggunakan Factory
        val factory = TaskViewModelFactory(repository)
        val taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        // Setup Note ViewModel (Tambahkan ini di bawah taskViewModel)
        val noteFactory = NoteViewModelFactory(database.noteDao())
        val noteViewModel = ViewModelProvider(this, noteFactory)[NoteViewModel::class.java]
        // Inisialisasi Streak ViewModel
        val streakFactory = StreakViewModelFactory(database.streakDao())
        val streakViewModel = ViewModelProvider(this, streakFactory)[StreakViewModel::class.java]

        setContent {
            val navController: NavHostController = rememberNavController()
            val noteEntities by noteViewModel.allNotes.collectAsState(initial = emptyList())
            val globalNotes = noteEntities.map { entity ->
                NoteData(entity.id, entity.title, entity.blocks, entity.date)
            }


            Surface(color = MaterialTheme.colorScheme.background) {
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                ) {
                    // --- Auth & Splash ---
                    composable("splash") {
                        SplashScreen(navController)
                    }
                    composable("login") {
                        LoginScreen(navController)
                    }

                    // --- Dashboard ---
                    composable("dashboard") {
                        DashboardScreen(navController, taskViewModel)
                    }

                    composable("tasks") {
                        TaskScreen(
                            viewModel = taskViewModel,
                            mode = "all",
                            navController = navController
                        )
                    }

                    composable("tasks_today") {
                        TaskScreen(
                            viewModel = taskViewModel,
                            mode = "today",
                            navController = navController
                        )
                    }

                    composable("tasks_upcoming") {
                        UpcomingTaskScreen(
                            taskViewModel = taskViewModel,
                            navController = navController
                        )
                    }



                    composable("add_task/{type}") { backStackEntry ->
                        val type = backStackEntry.arguments?.getString("type")

                        AddTaskScreen(
                            navController = navController,
                            viewModel = taskViewModel,
                            isUpcoming = type == "upcoming"
                        )
                    }

                    composable("task_detail/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments
                            ?.getString("taskId")
                            ?.toIntOrNull() ?: return@composable

                        TaskDetailScreen(
                            navController = navController,
                            taskId = taskId,
                            viewModel = taskViewModel
                        )
                    }

                    composable("edit_task/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments
                            ?.getString("taskId")
                            ?.toIntOrNull() ?: return@composable

                        EditTaskScreen(
                            taskId = taskId,
                            navController = navController,
                            viewModel = taskViewModel
                        )
                    }




                    // Rute tambahan untuk Quick Menu (agar tidak crash saat diklik)
                    composable("focus") {
                        FocusScreen(
                            navController = navController,
                            taskViewModel = taskViewModel
                        )
                    }

                    composable("streak") {
                        // Pastikan StreakScreen sudah di-import:
                        // import com.huma.app.ui.screen.streak.StreakScreen
                        StreakScreen(viewModel = streakViewModel)
                    }

                    composable("life_area") {
                        LifeAreaScreen(
                            navController = navController,
                            taskViewModel = taskViewModel
                        )
                    }
                    // SESUDAHNYA (Fix)
                    composable("notes_list") {
                        NoteScreen(
                            navController = navController,
                            globalNotes = globalNotes,
                            onDeleteNote = { noteToDelete ->
                                // Kita panggil fungsi delete dari noteViewModel
                                noteViewModel.deleteNote(noteToDelete)
                            }
                        )
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

                        NoteEditorScreen(
                            navController = navController,
                            noteId = noteId, // Kirim ID ke editor
                            globalNotes = globalNotes // Kirim list besar ke editor buat dicari datanya
                        ) { newNote ->
                            noteViewModel.saveNote(newNote)
                        }
                    }

                    composable("analytics") {
                        // Pastikan AnalyticsScreen sudah di-import dari package:
                        // com.huma.app.ui.screen.analytics.AnalyticsScreen
                        AnalyticsScreen(
                            navController = navController,
                            viewModel = taskViewModel
                        )
                    }
                    composable(
                        route = "area_detail/{areaName}",
                        arguments = listOf(navArgument("areaName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val areaName = backStackEntry.arguments?.getString("areaName") ?: ""
                        AreaDetailScreen(
                            areaName = areaName,
                            navController = navController,
                            taskViewModel = taskViewModel
                        )
                    }
                }
            }
        }
    }
}