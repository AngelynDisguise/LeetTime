package com.example.leettime.data.repository

import co.touchlab.kermit.Logger
import com.example.leettime.data.model.Problem
import com.example.leettime.data.network.LeetCodeApiService
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException

class LeetCodeRepository(
    private val apiService: LeetCodeApiService = LeetCodeApiService()
) {

    suspend fun getProblem(slug: String? = null, id: Int? = null): Problem? {
        require(slug != null || id != null) { "Either slug or id must be provided" }

        return try {
            apiService.getProblem(slug = slug, id = id)

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