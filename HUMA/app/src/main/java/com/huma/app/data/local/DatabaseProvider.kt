package com.huma.app.data.local

import android.content.Context
import androidx.room.Room


object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = AppDatabase.getInstance(context)
            INSTANCE = instance
            instance
        }
    }
}