package com.example.leettime.di

import org.koin.core.context.startKoin

fun doInitKoin() {
    startKoin {
        modules(appModule, platformModule)
    }
}