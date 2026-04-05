package com.huma.app.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.huma.app.MainActivity
import com.huma.app.R
import com.huma.app.data.local.AppDatabase
import com.huma.app.ui.screen.note.NoteBlock
import kotlinx.coroutines.runBlocking
import android.app.PendingIntent

class NoteWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NoteWidgetFactory(applicationContext)
    }
}

class NoteWidgetFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var blocks: List<NoteBlock> = emptyList()
    private var currentNoteId: String? = null

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // 🔥 KUNCI PERBAIKAN: Ambil Note ID terbaru dari Prefs setiap kali data berubah
        val noteId = NoteWidgetPrefs.getWidgetNote(context)
        currentNoteId = noteId

        if (noteId != null) {
            val db = AppDatabase.getInstance(context)
            blocks = runBlocking {
                db.noteDao().getNoteById(noteId)?.blocks ?: emptyList()
            }
        } else {
            blocks = emptyList()
        }
    }

    override fun getCount(): Int = blocks.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= blocks.size) return RemoteViews(context.packageName, R.layout.widget_note_item)

        val block = blocks[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_note_item)

        val text = when (block) {
            is NoteBlock.Heading -> "📌 ${block.content.uppercase()}"
            is NoteBlock.Text -> block.content
            is NoteBlock.BulletList -> block.items.joinToString("\n") { "• $it" }
            is NoteBlock.CheckboxGroup -> block.items.joinToString("\n") {
                if (it.isChecked) "☑ ${it.text}" else "☐ ${it.text}"
            }
            else -> ""
        }

        rv.setTextViewText(R.id.itemContent, text)

        // Klik item buka note yang sedang aktif
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_note_id", currentNoteId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (currentNoteId.hashCode() + position),
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(R.id.itemContent, pendingIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
    override fun onDestroy() {}
}