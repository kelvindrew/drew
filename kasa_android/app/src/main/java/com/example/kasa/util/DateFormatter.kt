package com.example.kasa.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val locale = Locale.FRENCH

    fun formatDate(date: Date): String {
        return SimpleDateFormat("dd MMM yyyy", locale).format(date)
    }

    fun formatShort(date: Date): String {
        return SimpleDateFormat("dd MMM", locale).format(date)
    }

    fun formatTime(date: Date): String {
        return SimpleDateFormat("HH:mm", locale).format(date)
    }

    fun currentMonthName(): String {
        return SimpleDateFormat("MMMM yyyy", locale).format(Date())
    }

    fun currentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }

    fun formatRelative(date: Date): String {
        val diff = Date().time - date.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "À l'instant"
            minutes < 60 -> "Il y a ${minutes}m"
            hours < 24 -> "Il y a ${hours}h"
            days < 7 -> "Il y a ${days}j"
            else -> formatDate(date)
        }
    }
}
