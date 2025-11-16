package com.example.leettime.data.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// only one instance of httpClient allowed
object KtorClient {
    val httpClient = HttpClient {

        install(ContentNegotiation) {
            json(Json { // JSON -> Kotlin object
                ignoreUnknownKeys = true
            })
        }
    }
}