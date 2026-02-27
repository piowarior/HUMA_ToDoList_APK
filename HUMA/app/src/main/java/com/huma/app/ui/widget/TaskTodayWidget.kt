package com.huma.app.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.huma.app.R

class TaskTodayWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_task_today)

            // sementara dummy text dulu (nanti kita ambil dari DB)
            views.setTextViewText(R.id.task1, "• Loading...")
            views.setTextViewText(R.id.task2, "")
            views.setTextViewText(R.id.task3, "")

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}