package com.example.leettime.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.dsl.module

val platformModule = module {
    single<Settings> { StorageSettings() }
}
