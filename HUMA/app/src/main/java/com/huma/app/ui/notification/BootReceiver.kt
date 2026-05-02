package com.huma.app.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.CommitmentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == "android.intent.action.QUICKBOOT_POWERON" || 
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            NotificationScheduler.scheduleAll(context)
            
            val db = AppDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Explicitly define type to help the compiler and avoid inference errors
                    val commitments: List<CommitmentEntity> = db.commitmentDao().getAllCommitmentsSync()
                    commitments.forEach { commitment: CommitmentEntity ->
                        if (commitment.isNotificationEnabled) {
                            CommitmentNotification.scheduleNotifications(context, commitment)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
