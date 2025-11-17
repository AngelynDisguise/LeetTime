package com.example.leettime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leettime.data.model.stagesExample
import com.example.leettime.data.model.total_time_limit_example
import com.example.leettime.ui.components.InterviewerModeButton
import com.example.leettime.ui.components.StageWheel
import com.example.leettime.ui.viewmodels.InterviewViewModel
import com.example.leettime.ui.viewmodels.LeetCodeViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(
    leetCodeViewModel: LeetCodeViewModel = koinViewModel(),
    interviewViewModel: InterviewViewModel = koinViewModel()
) {
    MaterialTheme {
        var totalTimeLimit by remember { mutableStateOf(total_time_limit_example) } // I need to store this in DataStore
        var leetcodeNumber by remember { mutableStateOf(1) }
        var leetcodeName by remember { mutableStateOf("--") }
        var currentStageTitle by remember { mutableStateOf("Solve in:") }
        var showEditDialog by remember { mutableStateOf(false) }
        var isInterviewerModeEnabled by remember { mutableStateOf(true) }

        // Collect problem from LeetCodeViewModel
        val currentProblem by leetCodeViewModel.currentProblem.collectAsStateWithLifecycle()
        val isLoading by leetCodeViewModel.isLoading.collectAsStateWithLifecycle()

        // Collect conversation state from InterviewViewModel
        val conversationState by interviewViewModel.uiState.collectAsStateWithLifecycle()

        // Update leetcodeName when problem is loaded
        LaunchedEffect(currentProblem) {
            currentProblem?.let { problem ->
                leetcodeName = problem.title
            }
        }

        // Load problem when number changes
        LaunchedEffect(leetcodeNumber) {
            leetCodeViewModel.loadProblem(leetcodeNumber)
        }


        var currentStageIndex by remember { mutableStateOf(-1) }
        var isRunning by remember { mutableStateOf(false) }
        var isFinished by remember { mutableStateOf(false) }
        var remainingTime by remember { mutableStateOf(total_time_limit_example) }

        // Start/End Gemini conversation based on timer state and interviewer mode
        LaunchedEffect(isRunning, isFinished, currentProblem, isInterviewerModeEnabled, currentStageIndex) {
            if (isRunning && !isFinished && currentProblem != null && isInterviewerModeEnabled) {
                // End any existing conversation first (important for stage changes)
                interviewViewModel.endInterview()

                // Small delay to ensure clean transition between stages
                kotlinx.coroutines.delay(300)

                // Start the conversation with the current stage context
                interviewViewModel.startInterview(
                    problem = currentProblem!!,
                    totalTimeLimit = totalTimeLimit,
                    stages = stagesExample,
                    currentStageIndex = currentStageIndex
                )
            } else if (isFinished || !isInterviewerModeEnabled) {
                // End the conversation when timer finishes or interviewer mode is disabled
                interviewViewModel.endInterview()
            }
        }

        // Timer countdown
        LaunchedEffect(isRunning) {
            while (isRunning && remainingTime.inWholeMilliseconds > 0) {
                delay(16) // updates every 16ms (~60fps)
                remainingTime -= 16.milliseconds
            }
            if (remainingTime.inWholeMilliseconds <= 0) {
                isRunning = false
                isFinished = true
                currentStageTitle = "Time's up!"
            }
        }

        val timeString = remember(remainingTime) {
            val totalSeconds = remainingTime.inWholeSeconds
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "LeetCode #$leetcodeNumber: $leetcodeName",
                                fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Handle hamburger menu */ }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            val backgroundColor = if (currentStageIndex >= 0 && currentStageIndex < stagesExample.size) {
                stagesExample[currentStageIndex].color.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }

            Column(
                modifier = Modifier
                    .background(backgroundColor)
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    InterviewerModeButton(
                        isEnabled = isInterviewerModeEnabled,
                        isAiSpeaking = conversationState.isAiSpeaking,
                        onClick = { isInterviewerModeEnabled = !isInterviewerModeEnabled },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val stageTitleColor = if (isRunning && currentStageIndex >= 0 && currentStageIndex < stagesExample.size) {
                        stagesExample[currentStageIndex].color
                    } else {
                        Color.Unspecified
                    }

                    Text(
                        currentStageTitle,
                        fontSize = 50.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.ExtraBold,
                        color = stageTitleColor
                    )

                    // Timer with refresh button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            timeString,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = {
                                // Reset everything
                                isRunning = false
                                isFinished = false
                                currentStageIndex = -1
                                remainingTime = totalTimeLimit
                                currentStageTitle = "Solve in:"
                                interviewViewModel.endInterview()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StageWheel(
                        totalTimeLimit = totalTimeLimit,
                        stages = stagesExample,
                        currentStageIndex = currentStageIndex,
                        isRunning = isRunning,
                        isFinished = isFinished,
                        remainingTime = remainingTime,
                        onCenterClickSwipe = {
                            if (isFinished) { // reset
                                isFinished = false
                                isRunning = false
                                currentStageIndex = -1
                                remainingTime = total_time_limit_example
                                currentStageTitle = "Solve in:"
                            } else if (!isRunning && currentStageIndex < 0) { // start
                                isRunning = true
                                currentStageIndex = 0
                                currentStageTitle = stagesExample[currentStageIndex].name.uppercase()
                            } else if (currentStageIndex < stagesExample.size - 1) { // advance stages
                                currentStageIndex++
                                currentStageTitle = stagesExample[currentStageIndex].name.uppercase()
                            } else { // Last stage: Finish - stop timer and keep visual state
                                isRunning = false
                                isFinished = true
                                currentStageTitle = "Finished!"
                            }
                        },
                        modifier = Modifier
                            .size(350.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Display conversation state for debugging
                    if (conversationState.error != null) {
                        Text(
                            "Error: ${conversationState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 12.sp
                        )
                    } else if (conversationState.isLoading) {
                        Text(
                            "Loading Gemini...",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 12.sp
                        )
                    } else if (conversationState.conversationText.isNotEmpty()) {
                        Text(
                            conversationState.conversationText,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        if (showEditDialog) {
            EditLeetcodeDialog(
                currentNumber = leetcodeNumber.toString(),
                onConfirm = { newNumber ->
                    leetcodeNumber = newNumber.toIntOrNull() ?: leetcodeNumber
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false }
            )
        }
    }
}

@Composable
fun EditLeetcodeDialog(
    currentNumber: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(currentNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Leetcode Number") },
        text = {
            TextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Leetcode Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(inputValue) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}