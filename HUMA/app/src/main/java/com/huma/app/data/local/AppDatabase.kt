package com.huma.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.huma.app.data.local.streak.StreakDao
import com.huma.app.data.local.streak.StreakEntity

@Database(
    entities = [
        TaskEntity::class,
        NoteEntity::class,
        StreakEntity::class,
        CapsuleEntity::class,
        CommitmentEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(
    TaskConverters::class,
    DateTimeConverter::class,
    NoteConverters::class,
    ListIntConverter::class,
    ListStringConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun streakDao(): StreakDao
    abstract fun capsuleDao(): CapsuleDao
    abstract fun commitmentDao(): CommitmentDao

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

                INSTANCE = instance
                instance
            }
        }
    }
}
