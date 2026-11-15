package com.dualmode.keyboard.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.SectionTitle

@Composable
fun PrivacyScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    ScreenScaffold(stringResource(R.string.privacy_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard {
                SectionTitle("What this keyboard never does")
                Text(
                    "• No keystroke logging\n" +
                        "• No password collection — password fields are detected and " +
                        "long-press popups are disabled there\n" +
                        "• No transmission of anything you type — the app has no " +
                        "network permission at all\n" +
                        "• No background clipboard reading — the clipboard is touched " +
                        "only when you tap Paste\n" +
                        "• No analytics, no ads, no tracking, no hidden services",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            InfoCard {
                SectionTitle("What the keyboard can access")
                Text(
                    "As an input method it writes to and reads from the currently " +
                        "focused text field, like every keyboard. It can set the " +
                        "clipboard (Copy/Cut) and read it (Paste) — only on your " +
                        "explicit tap. All settings are stored locally on your device.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            InfoCard {
                SectionTitle("Full policy")
                Text(
                    "Dual Mode Keyboard collects no personal data. It requires no " +
                        "network access, performs no analytics, and stores your " +
                        "preferences (theme, sizes, sounds) in a private local file " +
                        "that only this app can read. See PRIVACY.md in the project " +
                        "for the complete policy text.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
