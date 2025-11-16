package com.example.leettime

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.leettime.di.appModule
import com.example.leettime.di.platformModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Initialize Koin
    startKoin {
        modules(platformModule, appModule)
    }

    ComposeViewport {
        App()
    }
}