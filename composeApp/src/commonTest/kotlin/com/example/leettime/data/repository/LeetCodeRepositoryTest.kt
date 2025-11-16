package com.example.leettime.data.repository

import com.example.leettime.data.local.ProblemCache
import com.example.leettime.data.model.Problem
import com.example.leettime.data.network.LeetCodeApiService
import com.russhwolf.settings.Settings
import com.russhwolf.settings.MapSettings as SettingsMapSettings
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Fake implementation of LeetCodeApiService for testing
 */
class FakeLeetCodeApiService : LeetCodeApiService() {
    var problemToReturn: Problem? = null
    var exceptionToThrow: Exception? = null
    var lastSlugCalled: String? = null
    var lastIdCalled: Int? = null

    override suspend fun getProblem(slug: String?, id: Int?): Problem? {
        lastSlugCalled = slug
        lastIdCalled = id

        exceptionToThrow?.let { throw it }
        return problemToReturn
    }

    fun reset() {
        problemToReturn = null
        exceptionToThrow = null
        lastSlugCalled = null
        lastIdCalled = null
    }
}

class LeetCodeRepositoryTest : KoinTest {
    private val fakeApiService = FakeLeetCodeApiService()
    private val repository: LeetCodeRepository by inject()

    @BeforeTest
    fun setup() {
        startKoin {
            modules(module {
                single<LeetCodeApiService> { fakeApiService }
                single<Settings> { SettingsMapSettings() }
                single { ProblemCache(get()) }
                single { LeetCodeRepository(get(), get()) }
            })
        }
        fakeApiService.reset()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `getProblem with valid id returns problem`() = runTest {
        // Given
        val id = 1
        val expectedProblem = Problem(
            id = "1",
            frontendId = "1",
            title = "Two Sum",
            difficulty = "Easy",
            description = "Given an array of integers...",
            likes = 1000,
            dislikes = 50
        )
        fakeApiService.problemToReturn = expectedProblem

        // When
        val result = repository.getProblem(id = id)

        // Then
        assertEquals(expectedProblem, result)
    }

    @Test
    fun `getProblem fetches from cache first`() = runTest {
        // Given
        val id = 1
        val expectedProblem = Problem(
            id = "1",
            frontendId = "1",
            title = "Two Sum",
            difficulty = "Easy",
            description = "Given an array of integers...",
            likes = 1000,
            dislikes = 50
        )
        // First call should fetch from API and cache
        fakeApiService.problemToReturn = expectedProblem
        repository.getProblem(id = id)

        // Reset fake to return null
        fakeApiService.problemToReturn = null

        // When - second call should hit cache
        val result = repository.getProblem(id = id)

        // Then
        assertEquals(expectedProblem, result)
    }

    @Test
    fun `getProblem with generic exception returns null`() = runTest {
        // Given
        val id = 1
        fakeApiService.exceptionToThrow = RuntimeException("Network error")

        // When
        val result = repository.getProblem(id = id)

        // Then
        assertNull(result)
    }

    @Test
    fun `getProblem when apiService returns null returns null`() = runTest {
        // Given
        val id = 1
        fakeApiService.problemToReturn = null

        // When
        val result = repository.getProblem(id = id)

        // Then
        assertNull(result)
    }
}
