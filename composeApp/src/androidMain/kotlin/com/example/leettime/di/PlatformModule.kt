package com.example.leettime.di

import android.content.Context
import com.example.leettime.data.local.SettingsFactory
import org.koin.dsl.module

fun platformModule(context: Context) = module {
    single { context }
    single { SettingsFactory(context).createSettings() }
}
