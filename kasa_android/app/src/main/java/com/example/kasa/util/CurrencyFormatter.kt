package com.example.kasa.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currency: String = "USD"): String {
        return when (currency.uppercase()) {
            "USD" -> "$%.2f".format(Locale.US, amount)
            "EUR" -> "%.2f €".format(Locale.FRANCE, amount)
            "CDF" -> "%,.0f FC".format(Locale.FRENCH, amount)
            "XOF", "FCFA" -> "%,.0f FCFA".format(Locale.FRENCH, amount)
            else -> "$%.2f %s".format(Locale.US, amount, currency)
        }
    }

    fun formatCompact(amount: Double, currency: String = "USD"): String {
        return when {
            amount >= 1_000_000 -> "%.1fM %s".format(Locale.US, amount / 1_000_000, currency)
            amount >= 1_000 -> "%.1fK %s".format(Locale.US, amount / 1_000, currency)
            else -> format(amount, currency)
        }
    }
}
