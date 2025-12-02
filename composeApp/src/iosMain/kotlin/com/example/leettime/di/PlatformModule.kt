package com.example.leettime.di


import com.example.leettime.data.local.SettingsFactory
import org.koin.dsl.module

actual val platformModule = module {
    single { SettingsFactory().createSettings() }
}