package com.huma.app.ui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huma.app.MainActivity

class TaskCheckboxReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val taskId = intent.getIntExtra("taskId", -1)

        when (intent.action) {

            "CHECK_TASK" -> {

                val confirmIntent = Intent(context, WidgetConfirmActivity::class.java)
                confirmIntent.putExtra("taskId", taskId)
                confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(confirmIntent)
            }

            "OPEN_APP" -> {

                val openApp = Intent(context, MainActivity::class.java)
                openApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                context.startActivity(openApp)
            }
        }
    }
}