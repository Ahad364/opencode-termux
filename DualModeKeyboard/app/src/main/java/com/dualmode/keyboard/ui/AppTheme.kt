package com.dualmode.keyboard.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.themes.KeyboardTheme
import com.dualmode.keyboard.themes.Themes

/** Bridges keyboard themes into a Material 3 color scheme for the app UI. */
fun themeColorScheme(theme: KeyboardTheme): androidx.compose.material3.ColorScheme {
    val accent = Color(theme.accent)
    val bg = Color(theme.keyboardBackground)
    return if (theme.isDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color(theme.accentText),
            background = bg,
            onBackground = Color(theme.keyText),
            surface = bg,
            onSurface = Color(theme.keyText),
            surfaceVariant = Color(theme.functionKeyBackground),
            onSurfaceVariant = Color(theme.functionKeyText),
            secondary = accent,
            onSecondary = Color(theme.accentText)
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = Color(theme.accentText),
            background = bg,
            onBackground = Color(theme.keyText),
            surface = bg,
            onSurface = Color(theme.keyText),
            surfaceVariant = Color(theme.functionKeyBackground),
            onSurfaceVariant = Color(theme.functionKeyText),
            secondary = accent,
            onSecondary = Color(theme.accentText)
        )
    }
}

/** Root theming composable for the settings dashboard. */
@Composable
fun AppTheme(
    settings: KeyboardSettings,
    content: @Composable () -> Unit
) {
    val theme = Themes.resolve(settings)
    MaterialTheme(
        colorScheme = themeColorScheme(theme),
        content = content
    )
}
