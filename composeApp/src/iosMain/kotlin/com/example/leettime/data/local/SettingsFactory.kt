package com.example.leettime.data.local

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual class SettingsFactory {
    actual fun createSettings(): Settings {
        val userDefaults = NSUserDefaults.standardUserDefaults
        return NSUserDefaultsSettings(userDefaults)
    }
}
