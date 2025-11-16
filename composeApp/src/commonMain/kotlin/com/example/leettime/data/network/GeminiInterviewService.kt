package com.example.leettime.data.network

import kotlinx.coroutines.flow.StateFlow

// Interface for Gemini Live API service
data class InterviewState(
    val conversationText: String = "",
    val isAwaitingUserInput: Boolean = true,
    val isAiSpeaking: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

// The `expect` interface
expect class GeminiInterviewService() {

    /**
     * A flow that emits the current state of the interview.
     * The ViewModel will observe this.
     */
    val conversationState: StateFlow<InterviewState>

    /**
     * Initializes and starts a new voice conversation.
     * @param systemPrompt The "persona" for the AI (e.g., "You are an interviewer...")
     */
    suspend fun startConversation(systemPrompt: String)

    /**
     * Called when the user presses the "stop talking" button or after a timeout.
     */
    fun stopUserInput()

    /**
     * Cleans up the session.
     */
    fun endConversation()

    /**
     * Sends a text message instead of voice (for debugging or text fallback).
     */
    fun sendTextQuery(text: String)
}