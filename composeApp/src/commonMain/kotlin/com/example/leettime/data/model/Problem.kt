package com.example.leettime.data.model
import kotlinx.serialization.Serializable

@Serializable
data class Problem(
    val id: Int,
    val title: String,
    val difficulty: String,
    val description: String
)