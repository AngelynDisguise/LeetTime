package com.example.leettime.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leettime.data.model.Problem
import com.example.leettime.data.network.GeminiInterviewService
import com.example.leettime.data.network.InterviewState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * This is your shared ViewModel in commonMain.
 * It holds the business logic and exposes state to the Compose UI.
 * It uses the `GeminiInterviewService` via abstraction.
 *
 * (We get `ViewModel` from the `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose`
 * dependency in our build.gradle.kts)
 */
class InterviewViewModel(
    private val geminiService: GeminiInterviewService
) : ViewModel() {

    // Expose the conversation state directly from the service
    val uiState: StateFlow<InterviewState> = geminiService.conversationState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InterviewState()
        )

    fun startInterview(problem: Problem) {
        viewModelScope.launch {
            val systemPrompt = """
                You are a senior software engineering manager at Google conducting a technical interview.

                **Interview Context:**
                - Duration: 20 minutes
                - Problem Difficulty: ${problem.difficulty}
                - Problem: LeetCode #${problem.id} - ${problem.title}
                - Description: ${problem.description}

                **Your Role & Behavior:**
                You are an interviewer guiding the candidate through this technical interview simulation. The interview proceeds in stages:

                **Stage 1 - Understanding (Current Stage):**
                1. Start by clearly reading the problem description to the candidate
                2. Give the candidate space to ask clarifying questions
                3. Listen carefully to their thought process as they think aloud
                4. Answer their questions when they address you with "Gemini" at the start

                **Continuous Listening Mode:**
                - The microphone stays ON continuously - this simulates a real interview environment
                - Listen to EVERYTHING the candidate says for context (they're thinking aloud)
                - However, ONLY respond when they start their sentence with "Gemini" followed by a question or request
                - Use their voiced thought process to understand their approach, but don't interrupt
                - Be aware of what they're working on even without seeing their code directly

                **Response Guidelines:**
                - When "Gemini" triggers a response:
                  * Provide clear, helpful answers to clarifying questions
                  * Be encouraging but professional - this is a real interview
                  * Don't give away the solution, but guide them when stuck
                  * Ask follow-up questions to assess their understanding

                - When NOT triggered by "Gemini":
                  * Stay silent and listen
                  * Process their thought process for context
                  * Prepare to help if they get stuck

                **Interview Stages (for reference):**
                1. Understand - Clarify the problem (current stage)
                2. Match - Identify patterns and approaches
                3. Plan - Design the solution approach
                4. Implement - Write the code
                5. Review & Test - Verify correctness

                **IMPORTANT: You MUST immediately begin the interview when it starts.**
                As soon as the session connects, introduce yourself briefly and start reading the problem description out loud.
                Do NOT wait for the candidate to speak first.

                Example opening:
                "Hello, I'm your interviewer today. Let's begin with the problem. Today's question is [problem title]. [Read problem description]. Do you have any clarifying questions before we proceed?"
            """.trimIndent()

            geminiService.startConversation(systemPrompt)
        }
    }

    fun userFinishedTalking() {
        geminiService.stopUserInput()
    }

    fun sendTextMessage(text: String) {
        geminiService.sendTextQuery(text)
    }

    override fun onCleared() {
        geminiService.endConversation()
        super.onCleared()
    }
}