package com.dualmode.keyboard.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dual_mode_keyboard_settings"
)

/**
 * Single source of truth for keyboard settings. Stores everything locally in a
 * private DataStore file. Nothing here ever leaves the device.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val ACCENT = intPreferencesKey("accent_override")
        val KEY_BG = intPreferencesKey("key_background_override")
        val KEY_TEXT = intPreferencesKey("key_text_override")
        val KB_BG = intPreferencesKey("keyboard_background_override")
        val KEY_HEIGHT = floatPreferencesKey("key_height_dp")
        val RADIUS = floatPreferencesKey("key_corner_radius_dp")
        val SPACING = floatPreferencesKey("key_spacing_dp")
        val FONT_SIZE = floatPreferencesKey("key_font_size_sp")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val SOUND_VOLUME = intPreferencesKey("sound_volume")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val HAPTIC_STRENGTH = intPreferencesKey("haptic_strength")
        val ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val LANGUAGE = stringPreferencesKey("default_language")
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val LAST_MODE = stringPreferencesKey("last_used_mode")
    }

    val settingsFlow: Flow<KeyboardSettings> = context.dataStore.data.map { prefs ->
        val d = SettingsDefaults.SETTINGS
        KeyboardSettings(
            themeId = prefs[Keys.THEME] ?: d.themeId,
            accentOverride = prefs[Keys.ACCENT],
            keyBackgroundOverride = prefs[Keys.KEY_BG],
            keyTextOverride = prefs[Keys.KEY_TEXT],
            keyboardBackgroundOverride = prefs[Keys.KB_BG],
            keyHeightDp = prefs[Keys.KEY_HEIGHT] ?: d.keyHeightDp,
            keyCornerRadiusDp = prefs[Keys.RADIUS] ?: d.keyCornerRadiusDp,
            keySpacingDp = prefs[Keys.SPACING] ?: d.keySpacingDp,
            keyFontSizeSp = prefs[Keys.FONT_SIZE] ?: d.keyFontSizeSp,
            soundEnabled = prefs[Keys.SOUND] ?: d.soundEnabled,
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: d.soundVolume,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: d.hapticsEnabled,
            hapticStrength = prefs[Keys.HAPTIC_STRENGTH] ?: d.hapticStrength,
            animationsEnabled = prefs[Keys.ANIMATIONS] ?: d.animationsEnabled,
            defaultLanguage = prefs[Keys.LANGUAGE]?.let { runCatching { Language.valueOf(it) }.getOrNull() }
                ?: d.defaultLanguage,
            defaultMode = prefs[Keys.DEFAULT_MODE]?.let { runCatching { KeyboardMode.valueOf(it) }.getOrNull() }
                ?: d.defaultMode,
            lastMode = prefs[Keys.LAST_MODE]?.let { runCatching { KeyboardMode.valueOf(it) }.getOrNull() }
                ?: d.defaultMode
        )
    }

    suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        context.dataStore.edit { prefs ->
            val next = transform(decode(prefs))
            prefs[Keys.THEME] = next.themeId
            next.accentOverride?.let { prefs[Keys.ACCENT] = it } ?: prefs.remove(Keys.ACCENT)
            next.keyBackgroundOverride?.let { prefs[Keys.KEY_BG] = it } ?: prefs.remove(Keys.KEY_BG)
            next.keyTextOverride?.let { prefs[Keys.KEY_TEXT] = it } ?: prefs.remove(Keys.KEY_TEXT)
            next.keyboardBackgroundOverride?.let { prefs[Keys.KB_BG] = it } ?: prefs.remove(Keys.KB_BG)
            prefs[Keys.KEY_HEIGHT] = next.keyHeightDp
            prefs[Keys.RADIUS] = next.keyCornerRadiusDp
            prefs[Keys.SPACING] = next.keySpacingDp
            prefs[Keys.FONT_SIZE] = next.keyFontSizeSp
            prefs[Keys.SOUND] = next.soundEnabled
            prefs[Keys.SOUND_VOLUME] = next.soundVolume
            prefs[Keys.HAPTICS] = next.hapticsEnabled
            prefs[Keys.HAPTIC_STRENGTH] = next.hapticStrength
            prefs[Keys.ANIMATIONS] = next.animationsEnabled
            prefs[Keys.LANGUAGE] = next.defaultLanguage.name
            prefs[Keys.DEFAULT_MODE] = next.defaultMode.name
            prefs[Keys.LAST_MODE] = next.lastMode.name
        }
    }

    /** Remembers which mode the user last used, so the keyboard reopens in it. */
    suspend fun setLastMode(mode: KeyboardMode) = update { it.copy(lastMode = mode) }

    private fun decode(prefs: Preferences): KeyboardSettings {
        val d = SettingsDefaults.SETTINGS
        return KeyboardSettings(
            themeId = prefs[Keys.THEME] ?: d.themeId,
            accentOverride = prefs[Keys.ACCENT],
            keyBackgroundOverride = prefs[Keys.KEY_BG],
            keyTextOverride = prefs[Keys.KEY_TEXT],
            keyboardBackgroundOverride = prefs[Keys.KB_BG],
            keyHeightDp = prefs[Keys.KEY_HEIGHT] ?: d.keyHeightDp,
            keyCornerRadiusDp = prefs[Keys.RADIUS] ?: d.keyCornerRadiusDp,
            keySpacingDp = prefs[Keys.SPACING] ?: d.keySpacingDp,
            keyFontSizeSp = prefs[Keys.FONT_SIZE] ?: d.keyFontSizeSp,
            soundEnabled = prefs[Keys.SOUND] ?: d.soundEnabled,
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: d.soundVolume,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: d.hapticsEnabled,
            hapticStrength = prefs[Keys.HAPTIC_STRENGTH] ?: d.hapticStrength,
            animationsEnabled = prefs[Keys.ANIMATIONS] ?: d.animationsEnabled,
            defaultLanguage = prefs[Keys.LANGUAGE]?.let { runCatching { Language.valueOf(it) }.getOrNull() }
                ?: d.defaultLanguage,
            defaultMode = prefs[Keys.DEFAULT_MODE]?.let { runCatching { KeyboardMode.valueOf(it) }.getOrNull() }
                ?: d.defaultMode,
            lastMode = prefs[Keys.LAST_MODE]?.let { runCatching { KeyboardMode.valueOf(it) }.getOrNull() }
                ?: d.defaultMode
        )
    }
}
