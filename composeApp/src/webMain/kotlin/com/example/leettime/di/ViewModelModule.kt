package com.example.leettime.di

import com.example.leettime.ui.viewmodels.InterviewViewModel
import com.example.leettime.ui.viewmodels.LeetCodeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

actual val viewModelModule: Module = module {
    factoryOf(::LeetCodeViewModel)
    factoryOf(::InterviewViewModel)
}
