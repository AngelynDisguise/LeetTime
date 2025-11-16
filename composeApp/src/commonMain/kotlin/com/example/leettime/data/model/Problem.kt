package com.example.leettime.data.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Problem(
    @SerialName("questionId")
    val id: String,

    @SerialName("questionFrontendId")
    val frontendId: String,

    @SerialName("title")
    val title: String,

    @SerialName("content")
    val description: String,

    @SerialName("difficulty")
    val difficulty: String,

    @SerialName("likes")
    val likes: Int? = null,

    @SerialName("dislikes")
    val dislikes: Int? = null
)