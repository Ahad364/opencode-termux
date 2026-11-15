package com.dualmode.keyboard.ui.screens

import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.ui.ScreenScaffold
import com.dualmode.keyboard.ui.components.InfoCard
import com.dualmode.keyboard.ui.components.SectionTitle

@Composable
fun AboutScreen(viewModel: SettingsViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val version = rememberVersionName()
    ScreenScaffold(stringResource(R.string.about_title), navController) { modifier ->
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard {
                Text("Dual Mode Keyboard", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Version $version",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "A native Android keyboard with a full typing mode and a " +
                        "dedicated Ctrl shortcut mode, built with Kotlin, " +
                        "InputMethodService and Jetpack Compose.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            InfoCard {
                SectionTitle("Technology")
                Text(
                    "Kotlin • InputMethodService • Canvas-rendered keyboard views • " +
                        "Jetpack Compose (Material 3) • DataStore • Gradle Kotlin DSL • " +
                        "Version catalog • GitHub Actions CI",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            InfoCard {
                SectionTitle("Privacy")
                Text(
                    "This app has no INTERNET permission and collects no data. " +
                        "See the Privacy screen or PRIVACY.md for details.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            InfoCard {
                SectionTitle("Device info")
                Text(
                    "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun rememberVersionName(): String {
    val context = LocalContext.current
    return runCatching {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    }.getOrDefault("?")
}
