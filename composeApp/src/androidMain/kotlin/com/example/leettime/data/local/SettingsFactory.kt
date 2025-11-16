package com.example.leettime.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toBlockingSettings
import com.russhwolf.settings.datastore.DataStoreSettings

// Helper function to create a DataStore delegate in place of SharedPreferences
private val Context.dataStore by preferencesDataStore(name = "problem_cache")

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
actual class SettingsFactory(private val context: Context) {
    actual fun createSettings(): Settings {
        // Use DataStore with blocking wrapper for synchronous API
        return DataStoreSettings(context.dataStore).toBlockingSettings()
    }
}