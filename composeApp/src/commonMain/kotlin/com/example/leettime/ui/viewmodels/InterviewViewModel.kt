package com.example.leettime.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leettime.data.model.Problem
import com.example.leettime.data.model.Stage
import com.example.leettime.data.network.GeminiInterviewService
import com.example.leettime.data.network.InterviewState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

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

    fun startInterview(problem: Problem, totalTimeLimit: Duration, stages: List<Stage>, currentStageIndex: Int = 0) {
        viewModelScope.launch {
            // Format stage timing information
            val stageTimings = stages.mapIndexed { index, stage ->
                val minutes = stage.time_limit.inWholeMinutes
                val seconds = stage.time_limit.inWholeSeconds % 60
                val timeStr = if (minutes > 0) {
                    if (seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
                } else {
                    "${seconds}s"
                }
                "${index + 1}. ${stage.name} ($timeStr) - ${stage.description}"
            }.joinToString("\n                ")

            val totalMinutes = totalTimeLimit.inWholeMinutes

            // Determine current stage info
            val actualStageIndex = if (currentStageIndex >= 0 && currentStageIndex < stages.size) currentStageIndex else 0
            val currentStage = stages[actualStageIndex]
            val currentStageNumber = actualStageIndex + 1

            val systemPrompt = """
                You are a senior software engineering manager at Google conducting a technical interview.

                **Interview Context:**
                - Total Duration: $totalMinutes minutes
                - Problem Difficulty: ${problem.difficulty}
                - Problem: LeetCode #${problem.id} - ${problem.title}
                - Description: ${problem.description}

                **Time Management:**
                The candidate has exactly $totalMinutes minutes to complete this interview, divided into the following stages:
                $stageTimings

                The candidate can see a timer and will advance through stages manually. Be aware of time pressure and help them manage it effectively.

                **Your Role & Behavior:**
                You are an interviewer guiding the candidate through this technical interview simulation. The interview proceeds in stages:

                **Current Stage: Stage $currentStageNumber - ${currentStage.name} (${currentStage.time_limit.inWholeMinutes}m):**
                ${currentStage.description}

                ${if (actualStageIndex == 0) """
                1. Start by clearly reading the problem description to the candidate. You have all of the Leetcode information, but try to read the essential description in its entirety within 1-2 minutes.
                2. After reading, give the candidate space to ask clarifying questions
                3. Listen carefully to their thought process as they think aloud
                4. Answer their questions when they address you with "Gemini" at the start
                """ else """
                - The candidate is currently in the ${currentStage.name} stage
                - They have already heard the problem description
                - Provide guidance appropriate for this stage when they ask with "Gemini"
                - Listen to their thought process and only respond when prompted
                """}

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
                  * If they're spending too long on one stage, gently remind them of the time

                - When NOT triggered by "Gemini":
                  * Stay silent and listen
                  * Process their thought process for context
                  * Prepare to help if they get stuck

                ${if (actualStageIndex == 0) """
                **IMPORTANT: You MUST immediately begin the interview when it starts.**
                As soon as the session connects, introduce yourself briefly and start reading the problem description out loud.
                Do NOT wait for the candidate to speak first.

                Example opening:
                "Hello, I'm your interviewer today. First, I'm going to read you a LeetCode problem, and then you can ask any clarifying questions after.
                Today's question is [problem title]. [Briefly read problem description in its entirety within 1-2 minutes]. Feel free to ask me anything by saying "Gemini" at the start of your question."
                
                """ else """
                **IMPORTANT: The candidate has moved to the ${currentStage.name} stage.**
                The interview is in progress. Do NOT re-read the problem description.
                Simply listen and be ready to help with them with this stage.
                """}
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

    fun endInterview() {
        geminiService.endConversation()
    }

    override fun onCleared() {
        geminiService.endConversation()
        super.onCleared()
    }
}