package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.SliderRow
import com.dualmode.keyboard.ui.components.ToggleRow

@Composable
fun SoundScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    ScreenScaffold(stringResource(R.string.sound_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard {
                ToggleRow(
                    "Key press sound",
                    "Plays the system keypress click effect (respects system sound settings).",
                    checked = settings.soundEnabled,
                    onChanged = { viewModel.setSound(it) }
                )
                SliderRow(
                    "Volume",
                    value = settings.soundVolume.toFloat(),
                    valueText = "${settings.soundVolume}%",
                    valueRange = 0f..100f,
                    onValueChangeFinished = { viewModel.setSoundVolume(it.toInt()) }
                )
            }
        }
    }
}

@Composable
fun VibrationScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    ScreenScaffold(stringResource(R.string.vibration_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard {
                ToggleRow(
                    "Haptic feedback",
                    "Short vibration on every key press.",
                    checked = settings.hapticsEnabled,
                    onChanged = { viewModel.setHaptics(it) }
                )
                SliderRow(
                    "Strength",
                    value = settings.hapticStrength.toFloat(),
                    valueText = "${settings.hapticStrength}%",
                    valueRange = 0f..100f,
                    onValueChangeFinished = { viewModel.setHapticStrength(it.toInt()) }
                )
                Text(
                    "Note: very low strength may be imperceptible on some devices; " +
                        "Android also allows the user to disable vibration globally.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AnimationScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    ScreenScaffold(stringResource(R.string.animation_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoCard {
                ToggleRow(
                    "Keyboard animations",
                    "Press highlights and mode-switch transitions.",
                    checked = settings.animationsEnabled,
                    onChanged = { viewModel.setAnimations(it) }
                )
                Text(
                    "Typing is always instant — the press highlight is visual only and " +
                        "characters commit the moment you touch a key. Disabling " +
                        "animations never affects typing speed.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
