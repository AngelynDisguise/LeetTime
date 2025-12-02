package com.example.leettime.di

import com.example.leettime.data.local.ProblemCache
import com.example.leettime.data.network.GeminiInterviewService
import com.example.leettime.data.network.LeetCodeApiService
import com.example.leettime.data.repository.LeetCodeRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule = module {
    single { LeetCodeApiService() }
    single { ProblemCache(get()) }
    single { LeetCodeRepository(get(), get()) }
    single { GeminiInterviewService() }
    includes(viewModelModule)
}

// Platform-specific ViewModel module
expect val viewModelModule: Module
