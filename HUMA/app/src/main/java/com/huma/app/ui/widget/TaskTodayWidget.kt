package com.huma.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.huma.app.MainActivity
import com.huma.app.R

class TaskTodayWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for (widgetId in appWidgetIds) {

            // 🔥 SERVICE UNTUK LIST DATA
            val serviceIntent = Intent(context, TaskWidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            serviceIntent.data = android.net.Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

            val views = RemoteViews(
                context.packageName,
                R.layout.widget_task_today
            )

            views.setRemoteAdapter(R.id.taskList, serviceIntent)
            views.setEmptyView(R.id.taskList, R.id.emptyView)

            val openAppIntent = Intent(context, MainActivity::class.java)

            val pendingOpen = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.emptyView, pendingOpen)

            // 🔥 TEMPLATE CLICK
            val clickIntent = Intent(context, TaskCheckboxReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            views.setPendingIntentTemplate(
                R.id.taskList,
                pendingIntent
            )

            appWidgetManager.notifyAppWidgetViewDataChanged(
                intArrayOf(widgetId),
                R.id.taskList
            )

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}