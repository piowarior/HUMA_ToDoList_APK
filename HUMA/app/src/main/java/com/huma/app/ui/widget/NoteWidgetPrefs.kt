package com.huma.app.widget

import android.content.Context

object NoteWidgetPrefs {

    private const val PREF = "note_widget"
    private const val KEY_NOTE_ID = "note_id"

    fun setWidgetNote(context: Context, noteId: String) {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_NOTE_ID, noteId).apply()
    }

    fun getWidgetNote(context: Context): String? {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return pref.getString(KEY_NOTE_ID, null)
    }
}