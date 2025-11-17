@file:Suppress("EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")

package com.example.leettime.data.network

import android.Manifest
import androidx.annotation.RequiresPermission
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Android platform-specific implementation for Gemini Live API.
 * Uses Firebase AI Logic SDK for real-time audio streaming.
 *
 * Requirements:
 * - google-services.json with Firebase project configured
 * - Firebase Blaze plan (for Vertex AI access)
 * - Vertex AI enabled in Firebase Console
 * - RECORD_AUDIO permission in AndroidManifest.xml
 * - INTERNET permission in AndroidManifest.xml
 *
 * IMPORTANT: The Live API model (gemini-2.0-flash-live-preview-04-09) is ONLY
 * available in the us-central1 region. You must specify this location.
 */
@Suppress("EXPERIMENTAL_API_USAGE")
actual class GeminiInterviewService actual constructor() {

    private val TAG = "GeminiInterviewService"

    private val _conversationState = MutableStateFlow(InterviewState())
    actual val conversationState: StateFlow<InterviewState> = _conversationState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    @OptIn(PublicPreviewAPI::class)
    private var liveSession: LiveSession? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @OptIn(PublicPreviewAPI::class)
    actual suspend fun startConversation(systemPrompt: String) {
        Log.d(TAG, "startConversation() called")
        _conversationState.update {
            it.copy(
                conversationText = "Initializing Gemini Live API...",
                isLoading = true
            )
        }

        try {
            Log.d(TAG, "Creating Firebase AI model...")
            // Initialize the Vertex AI Gemini API backend service
            // CRITICAL: Set the location to `us-central1` (the flash-live model is only supported in that location)
            // Create a `LiveModel` instance with the flash-live model (only model that supports the Live API)
            val model = Firebase.ai(
                backend = GenerativeBackend.vertexAI(location = "us-central1")
            ).liveModel(
                modelName = "gemini-2.0-flash-live-preview-04-09",
                // Configure the model to respond with audio
                generationConfig = liveGenerationConfig {
                    responseModality = ResponseModality.AUDIO
                },
                systemInstruction = content { text(systemPrompt) }
            )

            Log.d(TAG, "Connecting to create live session...")
            // Connect to create a live session
            liveSession = model.connect()

            Log.d(TAG, "Starting audio conversation...")
            // Start the audio conversation (handles microphone and playback automatically!)
            // This is the recommended way according to Firebase docs.
            // The SDK handles:
            // - Microphone audio capture (16-bit PCM at 16kHz)
            // - Sending audio to Gemini
            // - Receiving audio responses (16-bit PCM at 24kHz)
            // - Playing back the audio response
            liveSession?.startAudioConversation()
            Log.d(TAG, "Audio conversation started successfully!")

            // Send an initial message to trigger the AI to start reading the problem
            // This is necessary because the Live API waits for input before responding
            scope.launch {
                try {
                    // Give the session a moment to fully initialize
                    kotlinx.coroutines.delay(1000)

                    Log.d(TAG, "Sending initial prompt to start interview...")
                    // Send a text prompt to trigger the AI to begin the interview
                    liveSession?.send("Please begin the interview.")
                    Log.d(TAG, "Initial prompt sent successfully!")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send initial message: ${e.message}", e)
                    // If sending the initial message fails, update state with error
                    _conversationState.update {
                        it.copy(
                            error = "Failed to initiate interview: ${e.message}",
                            isLoading = false
                        )
                    }
                }
            }

            _conversationState.update {
                it.copy(
                    conversationText = """
                        🎤 Gemini Live API Connected!

                        The microphone is now ACTIVE and continuously listening.

                        Interview Setup:
                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                        Stage 1: Understanding the Problem

                        How it works:
                        • The interviewer can hear everything you say
                        • Think out loud - this gives context
                        • Say "Gemini," at the start to ask questions
                        • The AI will respond with voice

                        Example Questions:
                        "Gemini, can you clarify the input constraints?"
                        "Gemini, what happens if the array is empty?"
                        "Gemini, should I optimize for time or space?"

                        The interviewer will now read you the problem...
                    """.trimIndent(),
                    isAwaitingUserInput = true,
                    isAiSpeaking = false,
                    isLoading = false
                )
            }

            // NOTE: For wake-word detection and transcription display, you would need to:
            // 1. Use a custom audio recorder instead of startAudioConversation()
            // 2. Process audio chunks for wake-word detection
            // 3. Send audio manually using session.sendRealtimeInput()
            // 4. Collect responses using session.receive().collect { }
            //
            // For now, we use the simple startAudioConversation() which handles everything
            // automatically but doesn't provide transcriptions or wake-word detection.

        } catch (e: Exception) {
            Log.e(TAG, "Live API initialization error: ${e.message}", e)
            _conversationState.update {
                it.copy(
                    error = """
                        Live API initialization error: ${e.message}

                        Checklist:
                        ✓ Firebase configured (google-services.json)
                        ✓ Vertex AI enabled in Firebase Console
                        ✓ Project on Blaze plan
                        ✓ RECORD_AUDIO permission granted
                        ✓ Using us-central1 region (required for Live API)

                        Common issues:
                        - Vertex AI not enabled in Firebase Console
                        - Project not on Blaze (pay-as-you-go) plan
                        - Microphone permission not granted
                    """.trimIndent(),
                    isLoading = false
                )
            }
        }
    }

    @OptIn(PublicPreviewAPI::class)
    actual fun stopUserInput() {
        // Stop the live audio conversation
        liveSession?.stopAudioConversation()
    }

    @OptIn(PublicPreviewAPI::class)
    actual fun sendTextQuery(text: String) {
        scope.launch {
            try {
                val session = liveSession ?: run {
                    _conversationState.update {
                        it.copy(error = "Live session not initialized. Call startConversation first.")
                    }
                    return@launch
                }

                // Send text message through the live session
                // This is useful for debugging or fallback when audio isn't working
                session.send(text)

                _conversationState.update {
                    it.copy(
                        conversationText = it.conversationText + "\n\n[You (text): $text]",
                        isAwaitingUserInput = false
                    )
                }

            } catch (e: Exception) {
                _conversationState.update {
                    it.copy(
                        error = "Error sending text: ${e.message}",
                        isAwaitingUserInput = true
                    )
                }
            }
        }
    }

    @OptIn(PublicPreviewAPI::class)
    actual fun endConversation() {
        Log.d(TAG, "endConversation() called")
        scope.launch {
            try {
                // Stop audio and close the session
                liveSession?.stopAudioConversation()
                liveSession?.close()
                liveSession = null

                Log.d(TAG, "Conversation ended successfully")
                _conversationState.update {
                    it.copy(
                        conversationText = "Interview session ended. Thank you!",
                        isLoading = false,
                        isAwaitingUserInput = false
                    )
                }
            } catch (e: Exception) {
                // Log error but don't show to user during cleanup
                Log.e(TAG, "Error ending conversation: ${e.message}", e)
            }
        }
    }
}
