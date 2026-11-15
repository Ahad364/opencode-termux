package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.data.KeyboardMode
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.themes.Themes
import com.dualmode.keyboard.ui.Routes
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.SectionTitle
import com.dualmode.keyboard.ui.components.StatusRow
import com.dualmode.keyboard.ui.components.ToggleRow
import com.dualmode.keyboard.utils.ImeStatus

@Composable
fun HomeScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val enabled = ImeStatus.isEnabled(context)
    val isDefault = ImeStatus.isDefault(context)

    ScreenScaffold(stringResource(R.string.app_name), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard {
                SectionTitle("Keyboard status")
                StatusRow("Enabled in system settings", enabled, "Enabled", "Not enabled")
                StatusRow(
                    "Default keyboard",
                    isDefault,
                    "This is the default",
                    "Another keyboard is active"
                )
                StatusRow(
                    "Current mode",
                    true,
                    if (settings.lastMode == KeyboardMode.SHORTCUT) "Ctrl shortcuts"
                    else "Normal typing",
                    ""
                )
                StatusRow("Active theme", true, Themes.resolve(settings).name, "")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { ImeStatus.openImeSettings(context) }) {
                        Text(stringResource(R.string.enable_keyboard))
                    }
                    OutlinedButton(onClick = { ImeStatus.showImePicker(context) }) {
                        Text(stringResource(R.string.choose_keyboard))
                    }
                }
            }

            InfoCard {
                SectionTitle("How to enable")
                Text(
                    "1. Tap “Enable keyboard” — Android Settings opens.\n" +
                        "2. Turn on “Dual Mode Keyboard”.\n" +
                        "3. Go back and tap “Choose keyboard”.\n" +
                        "4. Select “Dual Mode Keyboard”.\n\n" +
                        "The exact menu names can differ between phone brands. " +
                        "This app cannot and will not change your system keyboard silently.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            InfoCard {
                SectionTitle("Quick settings")
                ToggleRow(
                    "Key press sound",
                    checked = settings.soundEnabled,
                    onChanged = { viewModel.setSound(it) }
                )
                ToggleRow(
                    "Vibration",
                    checked = settings.hapticsEnabled,
                    onChanged = { viewModel.setHaptics(it) }
                )
                ToggleRow(
                    "Animations",
                    checked = settings.animationsEnabled,
                    onChanged = { viewModel.setAnimations(it) }
                )
                SectionTitle("Default mode when keyboard opens")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.setDefaultMode(KeyboardMode.NORMAL) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (settings.defaultMode == KeyboardMode.NORMAL) "✓ Normal"
                            else "Normal"
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.setDefaultMode(KeyboardMode.SHORTCUT) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (settings.defaultMode == KeyboardMode.SHORTCUT) "✓ Ctrl mode"
                            else "Ctrl mode"
                        )
                    }
                }
            }

            InfoCard {
                SectionTitle("Customize")
                NavButton("Keyboard preview", Routes.PREVIEW, navController)
                NavButton("Themes & colors", Routes.THEME, navController)
                NavButton("Ctrl shortcut manager", Routes.SHORTCUTS, navController)
                NavButton("Languages", Routes.LANGUAGE, navController)
                NavButton("Keyboard size & spacing", Routes.SIZE, navController)
                NavButton("Sound", Routes.SOUND, navController)
                NavButton("Vibration", Routes.VIBRATION, navController)
                NavButton("Animations", Routes.ANIMATION, navController)
                NavButton("Privacy", Routes.PRIVACY, navController)
                NavButton("About", Routes.ABOUT, navController)
            }
        }
    }
}

@Composable
private fun NavButton(label: String, route: String, navController: NavHostController) {
    OutlinedButton(
        onClick = { navController.navigate(route) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(label)
    }
}
