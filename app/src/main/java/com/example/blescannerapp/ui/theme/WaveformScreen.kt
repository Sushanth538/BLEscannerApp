package com.example.blescannerapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.roundToInt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path


@Composable
fun WaveformGraph(
    data: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(250.dp)
        .padding(12.dp)
        .background(Color.White)
        .border(1.dp, Color.LightGray)
) {
    if (data.isEmpty()) {
        Text("No graph data")
        return
    }

    Canvas(modifier = modifier) {
        val graphWidth = size.width
        val graphHeight = size.height
        val padding = 32f

        val maxData = data.maxOrNull() ?: 1f
        val minData = data.minOrNull() ?: 0f
        val range = (maxData - minData).takeIf { it != 0f } ?: 1f

        // Y-axis steps
        val ySteps = 5
        val stepValue = range / ySteps

        // Draw horizontal grid lines and Y-axis labels
        for (i in 0..ySteps) {
            val y = padding + i * (graphHeight - 2 * padding) / ySteps
            drawLine(
                color = Color.LightGray,
                start = Offset(padding, y),
                end = Offset(graphWidth - padding, y),
                strokeWidth = 1f
            )
        }

        // Draw vertical grid lines (every 20 points)
        val xStep = 20
        val visiblePoints = data.takeLast(200)
        val xInterval = (graphWidth - 2 * padding) / (visiblePoints.size - 1).coerceAtLeast(1)
        for (i in visiblePoints.indices step xStep) {
            val x = padding + i * xInterval
            drawLine(
                color = Color.LightGray,
                start = Offset(x, padding),
                end = Offset(x, graphHeight - padding),
                strokeWidth = 1f
            )
        }

        // Map data to coordinates
        val points = visiblePoints.mapIndexed { index, value ->
            val x = padding + index * xInterval
            val yRatio = (value - minData) / range
            val y = graphHeight - padding - yRatio * (graphHeight - 2 * padding)
            Offset(x, y)
        }

        // Smooth Bezier path
        val path = Path()
        if (points.size >= 2) {
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size - 1) {
                val midPoint = Offset(
                    (points[i].x + points[i + 1].x) / 2,
                    (points[i].y + points[i + 1].y) / 2
                )
                path.quadraticBezierTo(points[i].x, points[i].y, midPoint.x, midPoint.y)
            }
            path.lineTo(points.last().x, points.last().y)
        }

        drawPath(
            path = path,
            color = Color.Green,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Axis lines
        drawLine(
            color = Color.Black,
            start = Offset(padding, padding),
            end = Offset(padding, graphHeight - padding),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(padding, graphHeight - padding),
            end = Offset(graphWidth - padding, graphHeight - padding),
            strokeWidth = 2f
        )
    }
}
