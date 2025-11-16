package com.example.leettime.data.repository

import co.touchlab.kermit.Logger
import com.example.leettime.data.local.ProblemCache
import com.example.leettime.data.model.Problem
import com.example.leettime.data.network.LeetCodeApiService
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException

class LeetCodeRepository(
    private val apiService: LeetCodeApiService = LeetCodeApiService(),
    private val problemCache: ProblemCache
) {

    /**
     * Gets a LeetCode problem by ID from the cache first,
     * otherwise calls LeetCode API if empty.
     */
    suspend fun getProblem(id: Int): Problem? {

        val cachedProblem = problemCache.getProblem(id = id)
        Logger.i("Cache: $cachedProblem")
        if (cachedProblem != null) {
            Logger.i("Cache Hit: Found problem $id in cache!")
            return cachedProblem
        } else {
            // reset cache to preserve memory
            problemCache.clearAll()
        }

        return try {
            Logger.i("Cache Miss: Fetching Leetcode Problem...")

            val problem = apiService.getProblem(id = id)

            problem?.let { problemCache.saveProblem(id, it) }

            problem

        } catch (e: ClientRequestException) {
            Logger.e(e) { "Error [Client]: ${e.message}" }
            null
        } catch (e: ServerResponseException) {
            Logger.e(e) { "Error [Server]: ${e.message}" }
            null
        } catch (e: Exception) {
            Logger.e(e) { "Error [General]: ${e.message}" }
            null
        }
    }
}