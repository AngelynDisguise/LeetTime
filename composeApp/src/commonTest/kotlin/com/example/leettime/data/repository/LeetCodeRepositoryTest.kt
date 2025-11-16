package com.example.leettime.data.repository

import com.example.leettime.data.model.Problem
import com.example.leettime.data.network.LeetCodeApiService
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
import kotlin.test.assertFailsWith
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
                single { LeetCodeRepository(get()) }
            })
        }
        fakeApiService.reset()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `getProblem with valid slug returns problem`() = runTest {
        // Given
        val slug = "two-sum"
        val expectedProblem = Problem(
            id = 1,
            title = "Two Sum",
            difficulty = "Easy",
            description = "Given an array of integers..."
        )
        fakeApiService.problemToReturn = expectedProblem

        // When
        val result = repository.getProblem(slug = slug)

        // Then
        assertEquals(expectedProblem, result)
        assertEquals(slug, fakeApiService.lastSlugCalled)
        assertNull(fakeApiService.lastIdCalled)
    }

    @Test
    fun `getProblem with valid id returns problem`() = runTest {
        // Given
        val id = 1
        val expectedProblem = Problem(
            id = 1,
            title = "Two Sum",
            difficulty = "Easy",
            description = "Given an array of integers..."
        )
        fakeApiService.problemToReturn = expectedProblem

        // When
        val result = repository.getProblem(id = id)

        // Then
        assertEquals(expectedProblem, result)
        assertNull(fakeApiService.lastSlugCalled)
        assertEquals(id, fakeApiService.lastIdCalled)
    }

    @Test
    fun `getProblem with both slug and id null throws IllegalArgumentException`() = runTest {
        // When/Then
        assertFailsWith<IllegalArgumentException> {
            repository.getProblem(slug = null, id = null)
        }
    }

    @Test
    fun `getProblem with generic exception returns null`() = runTest {
        // Given
        val slug = "two-sum"
        fakeApiService.exceptionToThrow = RuntimeException("Network error")

        // When
        val result = repository.getProblem(slug = slug)

        // Then
        assertNull(result)
    }

    @Test
    fun `getProblem when apiService returns null returns null`() = runTest {
        // Given
        val slug = "two-sum"
        fakeApiService.problemToReturn = null

        // When
        val result = repository.getProblem(slug = slug)

        // Then
        assertNull(result)
    }
}
