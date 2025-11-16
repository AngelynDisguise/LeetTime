package com.example.leettime.data.local

import com.example.leettime.data.model.Problem
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.json.Json

class ProblemCache(
    private val settings: Settings,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    companion object {
        private const val PROBLEM_KEY_PREFIX = "problem_"
    }

    /**
     * Save a problem to cache by its ID
     */
    fun saveProblem(problemId: Int, problem: Problem) {
        val key = "$PROBLEM_KEY_PREFIX$problemId"
        val jsonString = json.encodeToString(problem)
        settings[key] = jsonString
    }

    /**
     * Get a problem from cache by its ID or name
     * Returns null if not found
     */
    fun getProblem(id: Int): Problem? {
        val key = "$PROBLEM_KEY_PREFIX$id"
        val jsonString: String? = settings[key]
        return jsonString?.let { json.decodeFromString<Problem>(it) }
    }

    /**
     * Check if a problem exists in cache
     */
    fun hasProblem(problemId: Int): Boolean {
        val key = "$PROBLEM_KEY_PREFIX$problemId"
        return settings.hasKey(key)
    }

    /**
     * Remove a problem from cache
     */
    fun removeProblem(problemId: Int) {
        val key = "$PROBLEM_KEY_PREFIX$problemId"
        settings.remove(key)
    }

    /**
     * Clear all cached problems
     */
    fun clearAll() {
        settings.clear()
    }
}
