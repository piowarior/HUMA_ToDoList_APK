package com.huma.app.data.local

import androidx.room.TypeConverter

class ListStringConverter {

    @TypeConverter
    fun fromList(list: List<String>): String {
        return if (list.isEmpty()) "" else list.joinToString(";")
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return if (value.isBlank()) emptyList()
        else value.split(";")
    }
}
