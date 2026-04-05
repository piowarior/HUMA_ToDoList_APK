package com.huma.app.data.local

import androidx.room.TypeConverter

class ListStringConverter {

    @TypeConverter
    fun fromList(list: List<String>): String =
        list.joinToString(",")

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isBlank()) emptyList()
        else value.split(",")
}
