package com.huma.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TaskEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    TaskConverters::class,
    DateTimeConverter::class   // kalau file ini memang ada
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "huma_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance // <--- KAMU LUPA BARIS INI TADI, makanya crash (null)
                instance
            }
        }
    }
}