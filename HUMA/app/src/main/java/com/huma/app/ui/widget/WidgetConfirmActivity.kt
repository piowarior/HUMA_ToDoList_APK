package com.huma.app.ui.widget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huma.app.data.local.AppDatabase
import kotlinx.coroutines.*

class WidgetConfirmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val taskId = intent.getIntExtra("taskId", -1)

        MaterialAlertDialogBuilder(this)
            .setTitle("✔ Selesaikan Task")
            .setMessage("Apakah kamu yakin task ini sudah selesai?")
            .setPositiveButton("Selesai") { _, _ ->

                CoroutineScope(Dispatchers.IO).launch {

                    val db = AppDatabase.getInstance(this@WidgetConfirmActivity)

                    db.taskDao().markTaskDone(taskId)

                    WidgetUpdater.update(this@WidgetConfirmActivity)

                    finish()
                }
            }
            .setNegativeButton("Batal") { _, _ ->
                finish()
            }
            .setCancelable(true)
            .show()
    }
}