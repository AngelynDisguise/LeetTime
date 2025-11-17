package com.example.leettime.data.model

import androidx.compose.ui.graphics.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class Stage(
    var name: String,
    var time_limit: Duration,
    val description: String,
    val color: Color,
    //var hint: String,
)

val total_time_limit_example = 20.minutes
val stagesExample = listOf(
    Stage("Understand", 3.minutes, "Read the question. Ask the interviewer questions about edge cases.", Color.Green),
    Stage("Match", 1.minutes, "Identify an appropriate data structure, algorithm, or pattern.", Color(0xFFFF6F00)), // Orange
    Stage("Plan", 3.minutes, "Explain and code the steps to your solution in pseudocode.", Color(0xFFFFC107)), // Amber
    Stage("Implement", 10.minutes, "Implement your pseudocode into real code.", Color.Red),
    Stage("Review", 2.minutes, "Test code and fix bugs.", Color.Blue),
    Stage("Evaluate", 1.minutes, "Identify time and space complexities and improvements that could be made.", Color(0xFF9C27B0))
)
// Note: there will be some functionality somewhere that checks and ensures all stages add up to the total_time.

// Changed to seconds for testing
//val total_time_limit_example = 20.seconds
//val stagesExample = listOf(
//    Stage("Understand", 3.seconds, "Read the question. Ask the interviewer questions about edge cases.", Color.Green),
//    Stage("Match", 1.seconds, "Identify an appropriate data structure, algorithm, or pattern.", Color(0xFFFF6F00)), // Orange
//    Stage("Plan", 3.seconds, "Explain and code the steps to your solution in pseudocode.", Color(0xFFFFC107)), // Amber
//    Stage("Implement", 10.seconds, "Implement your pseudocode into real code.", Color.Red),
//    Stage("Review", 2.seconds, "Test code and fix bugs.", Color.Blue),
//    Stage("Evaluate", 1.seconds, "Identify time and space complexities and improvements that could be made.", Color(0xFF9C27B0))
//)