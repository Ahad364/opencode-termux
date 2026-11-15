package com.dualmode.keyboard

import android.app.Application
import com.dualmode.keyboard.data.SettingsRepository

class KeyboardApplication : Application() {

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
