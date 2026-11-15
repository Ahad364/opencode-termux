package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.themes.KeyboardTheme
import com.dualmode.keyboard.themes.Themes
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.SectionTitle

@Composable
fun ThemeScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    val resolved = Themes.resolve(settings)

    ScreenScaffold(stringResource(R.string.theme_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard {
                Text(
                    "The keyboard preview updates live as you pick a theme or color. " +
                        "All choices are saved on this device only.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SectionTitle("Themes")
            Themes.ALL.chunked(2).forEach { rowThemes ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowThemes.forEach { theme ->
                        ThemeCard(
                            theme = theme,
                            selected = settings.themeId == theme.id,
                            onClick = { viewModel.setTheme(theme.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowThemes.size == 1) Box(Modifier.weight(1f))
                }
            }

            SectionTitle("Accent color")
            ColorSwatchRow(
                selected = settings.accentOverride,
                default = resolved.accent,
                onSelect = { viewModel.setAccent(it) }
            )
            SectionTitle("Key background")
            ColorSwatchRow(
                selected = settings.keyBackgroundOverride,
                default = resolved.keyBackground,
                onSelect = { viewModel.setKeyBackground(it) }
            )
            SectionTitle("Key text color")
            ColorSwatchRow(
                selected = settings.keyTextOverride,
                default = resolved.keyText,
                onSelect = { viewModel.setKeyText(it) }
            )
            SectionTitle("Keyboard background")
            ColorSwatchRow(
                selected = settings.keyboardBackgroundOverride,
                default = resolved.keyboardBackground,
                onSelect = { viewModel.setKeyboardBackground(it) }
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: KeyboardTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        // Mini preview strip made of the theme's own colors.
        Row(
            Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(
                    Color(theme.keyboardBackground),
                    RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(6) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(24.dp)
                        .background(
                            if (it == 3) Color(theme.accent) else Color(theme.keyBackground),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        Text(
            theme.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun ColorSwatchRow(selected: Int?, default: Int, onSelect: (Int?) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Swatch(null, selected == null) { onSelect(null) }
        Themes.CUSTOM_COLORS.forEach { color ->
            Swatch(color, selected == color) { onSelect(color) }
        }
    }
    Text(
        "First swatch = theme default",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Swatch(color: Int?, selected: Boolean, onClick: () -> Unit) {
    val display = if (color != null) Color(color) else MaterialTheme.colorScheme.surfaceVariant
    Box(
        Modifier
            .size(34.dp)
            .background(display, CircleShape)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (color == null) {
            Text("A", style = MaterialTheme.typography.labelMedium)
        }
    }
}
