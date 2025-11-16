package com.example.leettime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.leettime.components.StageWheel
import com.example.leettime.components.stagesExample
import com.example.leettime.components.total_time_limit_example
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var leetcodeNumber by remember { mutableStateOf("1") }
        var showEditDialog by remember { mutableStateOf(false) }
        var currentStageIndex by remember { mutableStateOf(-1) }
        var isRunning by remember { mutableStateOf(false) }
        var isFinished by remember { mutableStateOf(false) }
        var remainingTime by remember { mutableStateOf(total_time_limit_example) }

        // Timer countdown
        LaunchedEffect(isRunning) {
            while (isRunning && remainingTime.inWholeMilliseconds > 0) {
                delay(16) // updates every 16ms (~60fps)
                remainingTime -= 16.milliseconds
            }
            if (remainingTime.inWholeMilliseconds <= 0) {
                isRunning = false
                isFinished = true
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
                                "Leetcode #$leetcodeNumber",
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
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        "Leetcode name",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Solve in:",
                        fontSize = 34.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        timeString,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    StageWheel(
                        totalTimeLimit = total_time_limit_example,
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
                            } else if (!isRunning && currentStageIndex < 0) { // start
                                isRunning = true
                                currentStageIndex = 0
                            } else if (currentStageIndex < stagesExample.size - 1) { // advance stages
                                currentStageIndex++
                            } else { // Last stage: Finish - stop timer and keep visual state
                                isRunning = false
                                isFinished = true
                            }
                        },
                        modifier = Modifier
                            .size(350.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        if (showEditDialog) {
            EditLeetcodeDialog(
                currentNumber = leetcodeNumber,
                onConfirm = { newNumber ->
                    leetcodeNumber = newNumber
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