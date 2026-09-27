package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val flag: String) {
    ENGLISH("en", "English", "English", "🇬🇧"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", "🇮🇳");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "gu" -> GUJARATI
                else -> ENGLISH
            }
        }
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }
val LocalStrings = compositionLocalOf { AppStrings.get(AppLanguage.ENGLISH) }

@Composable
fun currentStrings(): AppStrings = LocalStrings.current
