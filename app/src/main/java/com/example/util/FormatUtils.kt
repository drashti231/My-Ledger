package com.example.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    fun formatCurrency(amount: Double, symbol: String = "$"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val formatted = formatter.format(amount)
        return if (symbol.length == 1) {
            "$symbol$formatted"
        } else {
            "$symbol $formatted"
        }
    }

    fun formatNumber(number: Number): String {
        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }

    fun formatPercent(value: Double): String {
        return String.format(Locale.US, "%.1f%%", value)
    }
}
