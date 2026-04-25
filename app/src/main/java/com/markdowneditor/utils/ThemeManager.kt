package com.markdowneditor.utils

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class ThemeManager {
    enum class ThemeType {
        LIGHT,
        DARK,
        SYSTEM
    }

    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFFF8F9FA),
        surface = Color(0xFFFFFFFF),
        onPrimary = Color(0xFFFFFFFF),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFF000000),
        onSurface = Color(0xFF000000)
    )

    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFBB86FC),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFFFFFFFF)
    )
}
