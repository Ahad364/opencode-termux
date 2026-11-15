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
import com.dualmode.keyboard.ui.components.KeyboardPreview

@Composable
fun PreviewScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val settings by viewModel.settings.collectAsState()

    ScreenScaffold(stringResource(R.string.preview_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard {
                Text(
                    "Type in the field above with the live keyboard preview. Tap the " +
                        "sticky Ctrl key, then a letter, to see how Ctrl shortcuts behave.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            KeyboardPreview(settings)
        }
    }
}
