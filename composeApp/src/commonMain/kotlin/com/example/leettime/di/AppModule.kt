package com.example.leettime.di

import com.example.leettime.data.network.LeetCodeApiService
import com.example.leettime.data.repository.LeetCodeRepository
import org.koin.dsl.module

val appModule = module {
    single { LeetCodeApiService() }
    single { LeetCodeRepository(get()) }
}
