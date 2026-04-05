package com.huma.app.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.huma.app.R
import com.huma.app.widget.NoteWidgetProvider

object WidgetUpdater {

    fun update(context: Context) {

        val manager = AppWidgetManager.getInstance(context)

        val component = ComponentName(
            context,
            TaskTodayWidget::class.java
        )

        val ids = manager.getAppWidgetIds(component)

        // 🔥 refresh list data
        manager.notifyAppWidgetViewDataChanged(ids, R.id.taskList)

        // 🔥 update widget
        val intent = Intent(
            context,
            TaskTodayWidget::class.java
        )

        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        context.sendBroadcast(intent)
    }

    fun updateNoteWidget(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, NoteWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widgetList)  // refresh ListView
        val intent = Intent(context, NoteWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}