package com.example.leettime.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.math.PI
import co.touchlab.kermit.Logger
import kotlin.time.Duration.Companion.seconds

data class Stage(
    var name: String,
    var time_limit: Duration,
    val description: String,
    //var hint: String,
)

//val total_time_limit_example = 20.minutes
//val stagesExample = listOf(
//    Stage("Understand", 3.minutes, "Read the question. Ask the interviewer questions about edge cases."),
//    Stage("Match", 1.minutes, "Identify an appropriate data structure, algorithm, or pattern."),
//    Stage("Plan", 3.minutes, "Explain and code the steps to your solution in pseudocode."),
//    Stage("Implement", 10.minutes, "Implement your pseudocode into real code."),
//    Stage("Review", 2.minutes, "Test code and fix bugs."),
//    Stage("Evaluate", 1.minutes, "Identify time and space complexities and improvements that could be made.")
//)
// Note: there will be some functionality somewhere that checks and ensures all stages add up to the total_time.

// Changed to seconds for testing
val total_time_limit_example = 20.seconds
val stagesExample = listOf(
    Stage("Understand", 3.seconds, "Read the question. Ask the interviewer questions about edge cases."),
    Stage("Match", 1.seconds, "Identify an appropriate data structure, algorithm, or pattern."),
    Stage("Plan", 3.seconds, "Explain and code the steps to your solution in pseudocode."),
    Stage("Implement", 10.seconds, "Implement your pseudocode into real code."),
    Stage("Review", 2.seconds, "Test code and fix bugs."),
    Stage("Evaluate", 1.seconds, "Identify time and space complexities and improvements that could be made.")
)


@Composable
fun StageWheel(
    totalTimeLimit: Duration,
    stages: List<Stage>,
    currentStageIndex: Int,
    isRunning: Boolean,
    remainingTime: Duration,
    onCenterClickSwipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sectionColors = remember {
        listOf(
            Color.Green,                // Green - Understand
            Color(0xFFFF9800),   // Orange - Match
            Color.Yellow,               // Vivid Yellow - Plan
            Color.Red,                  // Red - Implement
            Color.Blue,                 // Blue - Review
            Color(0xFF9C27B0),   // Purple - Evaluate
        )
    }

    // Calculate rotation angle based on remaining time (0-1)
    val progress = if (totalTimeLimit.inWholeMilliseconds > 0) {
        1f - (remainingTime.inWholeMilliseconds.toFloat() / totalTimeLimit.inWholeMilliseconds.toFloat())
    } else {
        0f
    }

    // Rotation starts at -90 degrees
    val rotationAngle = -90f + (progress * 360f)

    // This tracks which colors to persist
    var maxRotationReached by remember { mutableStateOf(-90f) }

    if (isRunning) {
        if (rotationAngle > maxRotationReached) {
            maxRotationReached = rotationAngle
        }
    } else {
        maxRotationReached = -90f
    }

    var size by remember { mutableStateOf(IntSize.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val centerTextStyle = TextStyle(
        color = Color.White,
        fontSize = 48.sp
    )
    val sectionTextStyle = TextStyle(
        color = Color.Black,
        fontSize = 24.sp
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val centerX = this.size.width / 2f
                    val centerY = this.size.height / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val distance = sqrt(dx * dx + dy * dy)
                    val radius = minOf(this.size.width, this.size.height) / 2f
                    val innerRadius = radius * 0.45f

                    // Check if tap is within the inner circle (center button)
                    if (distance <= innerRadius) {
                        Logger.d { "Inner Circle Clicked!" }
                        onCenterClickSwipe()
                    }
                }
            }
    ) {
        if (stages.isEmpty()) return@Canvas

        val centerX = this.size.width / 2f
        val centerY = this.size.height / 2f
        val radius = minOf(this.size.width, this.size.height) / 2f
        val innerRadius = radius * 0.45f  // Controls center button size (smaller = smaller button)
        val sectionThickness = radius * 0.55f  // Controls wheel thickness (larger = thicker wheel)

        //val totalTimeLimitMinutes = totalTimeLimit.inWholeMinutes.toFloat()
        val totalTimeLimitMinutes = totalTimeLimit.inWholeSeconds.toFloat()


        // Draw outer circle sections
        var currentAngle = -90f // Start at 90 degrees (top)
        stages.forEachIndexed { index, stage ->
            //val stageDurationMinutes = stage.time_limit.inWholeMinutes.toFloat()
            val stageDurationMinutes = stage.time_limit.inWholeSeconds.toFloat()
            val sweepAngle = (stageDurationMinutes / totalTimeLimitMinutes) * 360f

            val baseColor = sectionColors.getOrNull(index) ?: Color.Gray
            val isCurrentStage = index == currentStageIndex
            val hasPointerPassed = maxRotationReached >= currentAngle

            // Grey out sections and return to normal when visited by pointer
            val color = if (isRunning && !hasPointerPassed) {
                baseColor.copy(alpha = 0.3f)
            } else {
                baseColor
            }

            // Draw the section arc
            // The arc is centered at (innerRadius + sectionThickness/2) so the stroke extends inward and outward
            val arcRadius = innerRadius + sectionThickness / 2f
            drawArc(
                color = color,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    centerX - arcRadius,
                    centerY - arcRadius
                ),
                size = Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(
                    width = sectionThickness
                )
            )

            // Draw glow effect on rim if it's the current stage
            if (isCurrentStage) {
                val glowRadius = arcRadius + sectionThickness / 2f + 6f
                drawArc(
                    color = color.copy(alpha = 0.5f),
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        centerX - glowRadius,
                        centerY - glowRadius
                    ),
                    size = Size(
                        glowRadius * 2,
                        glowRadius * 2
                    ),
                    style = Stroke(
                        width = 4f
                    )
                )
            }

            // Draw section borders
            val sectionStartAngleRad = (currentAngle * PI / 180f).toFloat()
            val borderOuterRadius = arcRadius + sectionThickness / 2f + 2f
            val borderStartX = centerX + borderOuterRadius * cos(sectionStartAngleRad)
            val borderStartY = centerY + borderOuterRadius * sin(sectionStartAngleRad)
            val borderEndX = centerX + innerRadius * cos(sectionStartAngleRad)
            val borderEndY = centerY + innerRadius * sin(sectionStartAngleRad)

            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(borderStartX, borderStartY),
                end = Offset(borderEndX, borderEndY),
                strokeWidth = 1f
            )

            // Stage labels
            val sectionCenterAngle = currentAngle + sweepAngle / 2f
            val sectionCenterAngleRad = (sectionCenterAngle * PI / 180f).toFloat()
            val textRadius = arcRadius
            val textX = centerX + textRadius * cos(sectionCenterAngleRad)
            val textY = centerY + textRadius * sin(sectionCenterAngleRad)

            val stageInitial = stage.name.firstOrNull()?.toString() ?: ""
            val initialTextLayout = textMeasurer.measure(stageInitial, sectionTextStyle)

            // clip if text overflows
            val maxTextWidth = sectionThickness * 0.8f
            if (initialTextLayout.size.width <= maxTextWidth && initialTextLayout.size.height <= maxTextWidth) {
                drawText(
                    initialTextLayout,
                    topLeft = Offset(
                        textX - initialTextLayout.size.width / 2f,
                        textY - initialTextLayout.size.height / 2f
                    )
                )
            }

            currentAngle += sweepAngle
        }

        // Draw inner circle background with color based on current stage
        val centerButtonColor = if (currentStageIndex >= 0 && currentStageIndex < sectionColors.size) {
            sectionColors[currentStageIndex].copy(alpha = 0.8f)
        } else {
            Color.DarkGray
        }
        drawCircle(
            color = centerButtonColor,
            radius = innerRadius,
            center = Offset(centerX, centerY)
        )

        // Draw inner circle border
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = innerRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )

        // Inner circle text: "Go" if no index elected, otherwise the stage name
        val centerText = if (currentStageIndex >= 0 && currentStageIndex < stages.size) {
            stages[currentStageIndex].name
        } else {
            "Go"
        }

        val textLayoutResult = textMeasurer.measure(centerText, centerTextStyle)
        drawText(
            textLayoutResult,
            topLeft = Offset(
                centerX - textLayoutResult.size.width / 2f,
                centerY - textLayoutResult.size.height / 2f
            )
        )

        // Draw the rotating pointer (clock hand)
        // Pointer starts at the edge of the inner circle and extends to the outer edge of the wheel
        val pointerAngleRad = (rotationAngle * PI / 180f).toFloat()
        val pointerStartRadius = innerRadius
        val pointerEndRadius = radius - 2f

        val pointerStartX = centerX + pointerStartRadius * cos(pointerAngleRad)
        val pointerStartY = centerY + pointerStartRadius * sin(pointerAngleRad)
        val pointerEndX = centerX + pointerEndRadius * cos(pointerAngleRad)
        val pointerEndY = centerY + pointerEndRadius * sin(pointerAngleRad)

        drawLine(
            color = Color.Black,
            start = Offset(pointerStartX, pointerStartY),
            end = Offset(pointerEndX, pointerEndY),
            strokeWidth = 8f
        )
    }
}