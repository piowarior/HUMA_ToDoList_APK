package com.huma.app.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.huma.app.MainActivity
import com.huma.app.R
import com.huma.app.data.local.AppDatabase
import com.huma.app.ui.screen.note.NoteBlock
import kotlinx.coroutines.runBlocking

class NoteWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NoteWidgetFactory(applicationContext, intent)
    }
}

class NoteWidgetFactory(val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private val noteId = intent.getStringExtra("note_id") ?: ""
    private var blocks: List<NoteBlock> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val db = AppDatabase.getInstance(context)
        blocks = runBlocking { db.noteDao().getNoteById(noteId)?.blocks ?: emptyList() }
    }

    override fun getCount(): Int = blocks.size

    override fun getViewAt(position: Int): RemoteViews {
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

        // Klik item juga buka note
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_note_id", noteId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (noteId + position).hashCode(), // penting: requestCode unik
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