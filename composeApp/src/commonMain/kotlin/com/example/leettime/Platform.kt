package com.example.leettime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform