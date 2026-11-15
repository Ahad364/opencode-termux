package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.dualmode.keyboard.shortcuts.ShortcutId
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.SectionTitle

@Composable
fun ShortcutsScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    ScreenScaffold(stringResource(R.string.shortcuts_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard {
                Text(
                    "These shortcuts are available on the dedicated Ctrl keyboard. " +
                        "Switch to it any time with the ⇄ key on the typing keyboard.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            InfoCard {
                SectionTitle("How shortcuts work on Android")
                Text(
                    "Copy, Cut, Paste and Select all are performed directly on the " +
                        "focused text field and work in normal editors.\n\n" +
                        "Undo, Redo, Bold, Italic, Align, Save, Print, Open, New and " +
                        "Find are sent as real Ctrl key combinations. Android apps " +
                        "must implement Ctrl handling for these to take effect — " +
                        "for example Termux, Google Docs and some office or code " +
                        "editors. Apps that ignore Ctrl keys will simply not react; " +
                        "the keyboard tells you when an action was sent.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            InfoCard {
                SectionTitle("All shortcuts")
                ShortcutId.entries.forEachIndexed { index, id ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(id.description, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                id.comboLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = {
                                Text(
                                    when (id) {
                                        ShortcutId.COPY, ShortcutId.CUT,
                                        ShortcutId.PASTE, ShortcutId.SELECT_ALL ->
                                            "Works in most editors"
                                        else -> "Depends on the app"
                                    }
                                )
                            }
                        )
                    }
                    if (index != ShortcutId.entries.size - 1) HorizontalDivider()
                }
            }
        }
    }
}
