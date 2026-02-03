package com.huma.app.data.local

import androidx.room.TypeConverter
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority

class TaskConverters {

    @TypeConverter
    fun fromPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): TaskPriority {
        return TaskPriority.valueOf(value)
    }

    @TypeConverter
    fun fromMood(mood: TaskMood): String {
        return mood.name
    }

    @TypeConverter
    fun toMood(value: String): TaskMood {
        return TaskMood.valueOf(value)
    }
}
