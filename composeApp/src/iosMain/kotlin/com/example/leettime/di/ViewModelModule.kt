package com.example.leettime.di

import com.example.leettime.ui.viewmodels.InterviewViewModel
import com.example.leettime.ui.viewmodels.LeetCodeViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

actual val viewModelModule: Module = module {
    factory { LeetCodeViewModel(get()) }
    factory { InterviewViewModel(get()) }
}
