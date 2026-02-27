package com.huma.app.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

fun daysBetween(startMillis: Long, endMillis: Long): Int {
    if (startMillis == 0L) return 0

    val startCal = Calendar.getInstance().apply {
        timeInMillis = startMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCal = Calendar.getInstance().apply {
        timeInMillis = endMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffMillis = endCal.timeInMillis - startCal.timeInMillis
    return (diffMillis / (24 * 60 * 60 * 1000)).toInt()
}

fun getTodayDayId(): Long {
    return System.currentTimeMillis() / (1000L * 60 * 60 * 24)
}

fun addDays(baseMillis: Long, days: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = baseMillis
        add(Calendar.DAY_OF_YEAR, days)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}