package com.example.leettime.data.local

import com.russhwolf.settings.Settings

expect class SettingsFactory {
    fun createSettings(): Settings
}