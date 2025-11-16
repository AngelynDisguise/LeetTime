package com.example.leettime.di

import com.example.leettime.data.local.ProblemCache
import com.example.leettime.data.network.LeetCodeApiService
import com.example.leettime.data.repository.LeetCodeRepository
import com.example.leettime.ui.LeetCodeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { LeetCodeApiService() }
    single { ProblemCache(get()) }
    single { LeetCodeRepository(get(), get()) }
    viewModel { LeetCodeViewModel(get()) }
}
