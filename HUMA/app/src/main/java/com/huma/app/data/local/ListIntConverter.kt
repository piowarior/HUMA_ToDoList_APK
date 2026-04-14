package com.huma.app.data.local

import androidx.room.TypeConverter

class ListIntConverter {

    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return list?.joinToString(";") ?: ""
    }

    @TypeConverter
    fun toList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            value.split(";").filter { it.isNotBlank() }.map { it.toInt() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
