package com.dualmode.keyboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dualmode.keyboard.R
import com.dualmode.keyboard.settings.SettingsViewModel
import com.dualmode.keyboard.themes.Themes
import com.dualmode.keyboard.ui.screens.AboutScreen
import com.dualmode.keyboard.ui.screens.AnimationScreen
import com.dualmode.keyboard.ui.screens.HomeScreen
import com.dualmode.keyboard.ui.screens.LanguageScreen
import com.dualmode.keyboard.ui.screens.PreviewScreen
import com.dualmode.keyboard.ui.screens.PrivacyScreen
import com.dualmode.keyboard.ui.screens.ShortcutsScreen
import com.dualmode.keyboard.ui.screens.SizeScreen
import com.dualmode.keyboard.ui.screens.SoundScreen
import com.dualmode.keyboard.ui.screens.ThemeScreen
import com.dualmode.keyboard.ui.screens.VibrationScreen

object Routes {
    const val HOME = "home"
    const val PREVIEW = "preview"
    const val THEME = "theme"
    const val SHORTCUTS = "shortcuts"
    const val LANGUAGE = "language"
    const val SIZE = "size"
    const val SOUND = "sound"
    const val VIBRATION = "vibration"
    const val ANIMATION = "animation"
    const val PRIVACY = "privacy"
    const val ABOUT = "about"
}

@Composable
fun App(viewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val settings by viewModel.settings.collectAsState()
    MaterialTheme(colorScheme = themeColorScheme(Themes.resolve(settings))) {
        NavHost(navController = navController, startDestination = Routes.HOME) {
            composable(Routes.HOME) { HomeScreen(viewModel, navController) }
            composable(Routes.PREVIEW) { PreviewScreen(viewModel, navController) }
            composable(Routes.THEME) { ThemeScreen(viewModel, navController) }
            composable(Routes.SHORTCUTS) { ShortcutsScreen(viewModel, navController) }
            composable(Routes.LANGUAGE) { LanguageScreen(viewModel, navController) }
            composable(Routes.SIZE) { SizeScreen(viewModel, navController) }
            composable(Routes.SOUND) { SoundScreen(viewModel, navController) }
            composable(Routes.VIBRATION) { VibrationScreen(viewModel, navController) }
            composable(Routes.ANIMATION) { AnimationScreen(viewModel, navController) }
            composable(Routes.PRIVACY) { PrivacyScreen(viewModel, navController) }
            composable(Routes.ABOUT) { AboutScreen(viewModel, navController) }
        }
    }
}

/** Standard screen scaffold with a centered title and back navigation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content(Modifier)
        }
    }
}
