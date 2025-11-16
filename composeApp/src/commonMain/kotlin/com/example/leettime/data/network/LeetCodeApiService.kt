package com.example.leettime.data.network

import co.touchlab.kermit.Logger
import com.example.leettime.data.model.Problem
import io.ktor.client.call.body
import io.ktor.client.request.get

open class LeetCodeApiService {
    private val client = KtorClient.httpClient

    private val baseUrl = "https://leetcode-api-pied.vercel.app/problem/"

    open suspend fun getProblem(slug: String? = null, id: Int? = null): Problem? {
        require(slug != null || id != null) { "Either slug or id must be provided" }

        return try {
            client.get(baseUrl + (slug ?: id)).body()
        } catch (e: Exception) {
            Logger.e(e) { e.message.toString() }
            null
        }
    }
}