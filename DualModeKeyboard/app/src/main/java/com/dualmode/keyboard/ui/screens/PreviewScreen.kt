package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.data.KeyboardMode
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.KeyboardPreview

@Composable
fun PreviewScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()
    var mode by remember { mutableStateOf(settings.lastMode) }

    ScreenScaffold(stringResource(R.string.preview_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == KeyboardMode.NORMAL,
                    onClick = { mode = KeyboardMode.NORMAL },
                    label = { Text("Normal mode") }
                )
                FilterChip(
                    selected = mode == KeyboardMode.SHORTCUT,
                    onClick = { mode = KeyboardMode.SHORTCUT },
                    label = { Text("Ctrl shortcut mode") }
                )
            }
            InfoCard {
                Text(
                    if (mode == KeyboardMode.SHORTCUT)
                        "Tap a shortcut key to see how it would dispatch Ctrl combos. " +
                            "In this preview, clipboard operations are disabled."
                    else
                        "Type in the field above with the live keyboard preview.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
            KeyboardPreview(settings, mode)
        }
    }
}
