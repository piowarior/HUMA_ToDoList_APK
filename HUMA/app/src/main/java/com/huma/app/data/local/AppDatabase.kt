package com.huma.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.huma.app.data.local.streak.StreakDao     // 🔥 Import Dao Baru
import com.huma.app.data.local.streak.StreakEntity  // 🔥 Import Entity Baru

@Database(
    entities = [
        TaskEntity::class,
        NoteEntity::class,
        StreakEntity::class // 🔥 Daftarkan StreakEntity di sini
    ],
    version = 4, // 🔥 Naikkan ke versi 4 karena ada tabel baru (Streak)
    exportSchema = false
)
@TypeConverters(
    TaskConverters::class,
    DateTimeConverter::class,
    NoteConverters::class,
    ListIntConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun streakDao(): StreakDao // 🔥 Tambahkan fungsi akses untuk StreakDao

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
                    /* ⚠️ PENTING: fallbackToDestructiveMigration() akan menghapus data lama
                       setiap kali versi naik (dari 3 ke 4). Cocok untuk tahap development.
                    */
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}