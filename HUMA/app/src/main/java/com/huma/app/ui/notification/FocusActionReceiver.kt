package com.huma.app.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FocusActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        // 1. Update Service (Notifikasi)
        val serviceIntent = Intent(context, FocusService::class.java).apply {
            this.action = action
        }
        context.startService(serviceIntent)

        // 2. Update UI (Layar Timer di APK)
        val uiIntent = Intent("FOCUS_UPDATE").apply {
            putExtra("action", action)
            setPackage(context.packageName)
        }
        context.sendBroadcast(uiIntent)
    }
}