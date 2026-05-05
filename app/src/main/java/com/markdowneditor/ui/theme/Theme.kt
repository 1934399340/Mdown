package com.markdowneditor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

data class AppElevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 1.dp,
    val level2: Dp = 3.dp,
    val level3: Dp = 6.dp,
    val level4: Dp = 8.dp,
    val level5: Dp = 12.dp,
)

data class AppTypography(
    val displayLarge: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    val displayMedium: TextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    val headlineLarge: TextStyle = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    val headlineMedium: TextStyle = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    val titleLarge: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    val titleMedium: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    val bodyLarge: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    val bodyMedium: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    val bodySmall: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    val labelLarge: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    val labelMedium: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    val labelSmall: TextStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp),
    val editorText: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp, letterSpacing = 0.3.sp),
    val editorCode: TextStyle = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp),
)

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
val LocalAppElevation = staticCompositionLocalOf { AppElevation() }
val LocalAppTypography = staticCompositionLocalOf { AppTypography() }

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenOnPrimary = Color(0xFFFFFFFF)
private val GreenPrimaryContainer = Color(0xFFB9F6CA)
private val GreenOnPrimaryContainer = Color(0xFF002105)

private val TealSecondary = Color(0xFF006C4C)
private val TealOnSecondary = Color(0xFFFFFFFF)
private val TealSecondaryContainer = Color(0xFF89F8C7)
private val TealOnSecondaryContainer = Color(0xFF002114)

private val WarmTertiary = Color(0xFF7B5800)
private val WarmOnTertiary = Color(0xFFFFFFFF)
private val WarmTertiaryContainer = Color(0xFFFFDEA6)
private val WarmOnTertiaryContainer = Color(0xFF271900)

private val DarkGreenPrimary = Color(0xFF6BDB73)
private val DarkGreenOnPrimary = Color(0xFF003909)
private val DarkGreenPrimaryContainer = Color(0xFF005313)
private val DarkGreenOnPrimaryContainer = Color(0xFFB9F6CA)

private val DarkTealSecondary = Color(0xFF6DDBA8)
private val DarkTealOnSecondary = Color(0xFF003824)
private val DarkTealSecondaryContainer = Color(0xFF005237)
private val DarkTealOnSecondaryContainer = Color(0xFF89F8C7)

private val DarkWarmTertiary = Color(0xFFF0BF48)
private val DarkWarmOnTertiary = Color(0xFF402D00)
private val DarkWarmTertiaryContainer = Color(0xFF5C4300)
private val DarkWarmOnTertiaryContainer = Color(0xFFFFDEA6)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = TealOnSecondaryContainer,
    tertiary = WarmTertiary,
    onTertiary = WarmOnTertiary,
    tertiaryContainer = WarmTertiaryContainer,
    onTertiaryContainer = WarmOnTertiaryContainer,
    background = Color(0xFFFBFDF7),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFBFDF7),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDDE5D9),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717971),
    outlineVariant = Color(0xFFC1C9BD),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = DarkGreenOnPrimary,
    primaryContainer = DarkGreenPrimaryContainer,
    onPrimaryContainer = DarkGreenOnPrimaryContainer,
    secondary = DarkTealSecondary,
    onSecondary = DarkTealOnSecondary,
    secondaryContainer = DarkTealSecondaryContainer,
    onSecondaryContainer = DarkTealOnSecondaryContainer,
    tertiary = DarkWarmTertiary,
    onTertiary = DarkWarmOnTertiary,
    tertiaryContainer = DarkWarmTertiaryContainer,
    onTertiaryContainer = DarkWarmOnTertiaryContainer,
    background = Color(0xFF1A1C19),
    onBackground = Color(0xFFE2E3DD),
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9BD),
    outline = Color(0xFF8B938C),
    outlineVariant = Color(0xFF414942),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun MarkdownEditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAppSpacing provides AppSpacing(),
        LocalAppElevation provides AppElevation(),
        LocalAppTypography provides AppTypography()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

object AppTheme {
    val spacing: AppSpacing @Composable get() = LocalAppSpacing.current
    val elevation: AppElevation @Composable get() = LocalAppElevation.current
    val typography: AppTypography @Composable get() = LocalAppTypography.current
}
