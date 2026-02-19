package com.huma.app.data.local

import androidx.room.TypeConverter

class ListIntConverter {

    @TypeConverter
    fun fromList(list: List<Int>): String =
        list.joinToString(",")

    @TypeConverter
    fun toList(value: String): List<Int> =
        if (value.isBlank()) emptyList()
        else value.split(",").map { it.toInt() }
}
