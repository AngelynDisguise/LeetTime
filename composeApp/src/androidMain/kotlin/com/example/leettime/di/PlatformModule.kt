package com.example.leettime.di

import android.content.Context
import com.example.leettime.data.local.SettingsFactory
import org.koin.dsl.module

private lateinit var appContext: Context

fun initAndroidContext(context: Context) {
    appContext = context
}

actual val platformModule = module {
    single { appContext }
    single { SettingsFactory(appContext).createSettings() }
}
