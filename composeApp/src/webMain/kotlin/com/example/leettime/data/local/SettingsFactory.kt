package com.example.leettime.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual class SettingsFactory {
    actual fun createSettings(): Settings {
        return StorageSettings() // use localStorage
    }
}
