package com.huma.app.ui.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.huma.app.R

const val ACTION_PAUSE = "com.huma.app.ACTION_PAUSE"
const val ACTION_RESUME = "com.huma.app.ACTION_RESUME"
const val ACTION_STOP = "com.huma.app.ACTION_STOP"

class FocusService : Service() {

    private var paused = false
    private var taskTitle = ""
    private var method = ""
    private var phase = ""
    private var timeLeft = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_PAUSE -> paused = true
                ACTION_RESUME -> paused = false
                ACTION_STOP -> {
                    stopForeground(true)
                    stopSelf()
                    return START_NOT_STICKY
                }
                else -> {
                    taskTitle = it.getStringExtra("task") ?: "Fokus"
                    method = it.getStringExtra("method") ?: "Pomodoro"
                    phase = it.getStringExtra("phase") ?: "Focus"
                    timeLeft = it.getStringExtra("time") ?: "25:00"

                    if (it.hasExtra("is_paused")) {
                        paused = it.getBooleanExtra("is_paused", false)
                    } else {
                        notifyFocusStarted()
                    }
                }
            }

            val timeUpdate = it.getStringExtra("time")
            if (timeUpdate != null) timeLeft = timeUpdate

            val phaseUpdate = it.getStringExtra("phase")
            if (phaseUpdate != null) phase = phaseUpdate

            startForeground(FOCUS_NOTIFICATION_ID, buildNotification())
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_focus)

        remoteViews.setTextViewText(R.id.tv_task_title, taskTitle)
        remoteViews.setTextViewText(R.id.tv_timer, timeLeft)
        remoteViews.setTextViewText(R.id.tv_phase, "$method • $phase")

        if (paused) {
            remoteViews.setTextViewText(R.id.tv_status, "PAUSED")
            remoteViews.setTextColor(R.id.tv_status, android.graphics.Color.YELLOW)
        } else {
            remoteViews.setTextViewText(R.id.tv_status, "FOCUSING")
            remoteViews.setTextColor(R.id.tv_status, android.graphics.Color.parseColor("#6C63FF"))
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FOCUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.logohumaicon) // Menggunakan logo huma
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setStyle(androidx.core.app.NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                if (paused) "Resume" else "Pause",
                focusActionPending(if (paused) ACTION_RESUME else ACTION_PAUSE)
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Stop",
                focusActionPending(ACTION_STOP)
            )
            .build()
    }

    private fun notifyFocusStarted() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alertNotification = NotificationCompat.Builder(this, "task_channel")
            .setSmallIcon(R.drawable.logohumaicon) // Menggunakan logo huma
            .setContentTitle("HUMA Focus Started")
            .setContentText("Target: $taskTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(2002, alertNotification)
    }

    private fun focusActionPending(action: String) =
        PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            Intent(this, FocusActionReceiver::class.java).apply {
                this.action = action
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    override fun onBind(intent: Intent?): IBinder? = null
}