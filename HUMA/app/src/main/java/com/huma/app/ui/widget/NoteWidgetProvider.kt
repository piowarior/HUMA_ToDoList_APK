package com.huma.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.huma.app.MainActivity
import com.huma.app.R
import com.huma.app.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import com.huma.app.ui.screen.note.NoteBlock

class NoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_note)
        val noteId = NoteWidgetPrefs.getWidgetNote(context)

        if (noteId != null) {
            val db = AppDatabase.getInstance(context)
            val note = runBlocking { db.noteDao().getNoteById(noteId) }


            if (note != null) {
                views.setTextViewText(R.id.widgetTitle, note.title)

                // RemoteViewsService
                val intentService = Intent(context, NoteWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("note_id", noteId)
                    data = android.net.Uri.parse("widget://$appWidgetId") // wajib unik
                }
                views.setRemoteAdapter(R.id.widgetList, intentService)

                // Klik judul buka note
                val intentMain = Intent(context, MainActivity::class.java).apply {
                    putExtra("open_note_id", noteId)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    noteId.hashCode(), 
                    intentMain,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent)
                
                // 🔥 Penting: Beritahu manager bahwa data list (RemoteViewsService) harus di-refresh
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList)
                
            } else {
                views.setTextViewText(R.id.widgetTitle, "Catatan tidak ditemukan")
            }
        } else {
            views.setTextViewText(R.id.widgetTitle, "Belum ada catatan")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // 🔥 Realtime update saat ada broadcast update
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, NoteWidgetProvider::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            
            for (id in ids) {
                updateWidget(context, mgr, id)
            }
        }
    }
}
