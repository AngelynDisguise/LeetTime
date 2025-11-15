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
import kotlin.time.Duration.Companion.minutes
import kotlin.math.PI
import co.touchlab.kermit.Logger

data class Stage(
    var name: String,
    var time_limit: Duration,
    val description: String,
    //var hint: String,
)

val total_time_limit = 20.minutes
val stagesExample = listOf(
    Stage("Understand", 3.minutes, "Read the question. Ask the interviewer questions about edge cases."),
    Stage("Match", 1.minutes, "Identify an appropriate data structure, algorithm, or pattern."),
    Stage("Plan", 3.minutes, "Explain and code the steps to your solution in pseudocode."),
    Stage("Implement", 10.minutes, "Implement your pseudocode into real code."),
    Stage("Review", 2.minutes, "Test code and fix bugs."),
    Stage("Evaluate", 1.minutes, "Identify time and space complexities and improvements that could be made.")
)
// Note: there will be some functionality somewhere that checks and ensures all stages add up to the total_time.

@Composable
fun StageWheel(
    stages: List<Stage>,
    currentStageIndex: Int,
    isRunning: Boolean,
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

    var size by remember { mutableStateOf(IntSize.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = Color.White,
        fontSize = 48.sp
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

        // Calculate total time limit in minutes
        val totalTimeLimitMinutes = total_time_limit.inWholeMinutes.toFloat()

        // Draw outer circle sections
        var currentAngle = -90f // Start at 90 degrees (top)
        stages.forEachIndexed { index, stage ->
            val stageDurationMinutes = stage.time_limit.inWholeMinutes.toFloat()
            val sweepAngle = (stageDurationMinutes / totalTimeLimitMinutes) * 360f

            val color = sectionColors.getOrNull(index) ?: Color.Gray
            val isCurrentStage = index == currentStageIndex

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

            // Draw section borders (radial lines)
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

        val textLayoutResult = textMeasurer.measure(centerText, textStyle)
        drawText(
            textLayoutResult,
            topLeft = Offset(
                centerX - textLayoutResult.size.width / 2f,
                centerY - textLayoutResult.size.height / 2f
            )
        )
    }
}