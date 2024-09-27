package com.akatsukirika.openps.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BidirectionalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = -1f..1f,
    trackColor: Color = Color.LightGray,
    highlightColor: Color = Color.Blue
) {
    var sliderPosition by remember { mutableFloatStateOf(value) }

    LaunchedEffect(key1 = value) {
        sliderPosition = value
    }

    Box(modifier = modifier.height(40.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2

            // Draw background track
            drawLine(
                color = trackColor,
                start = Offset(0f, canvasHeight / 2),
                end = Offset(canvasWidth, canvasHeight / 2),
                strokeWidth = 4.dp.toPx()
            )

            // Draw highlight
            val highlightStart: Float
            val highlightEnd: Float
            if (valueRange.start < 0f) {
                highlightStart = if (sliderPosition < 0f) centerX + (sliderPosition * centerX) else centerX
                highlightEnd = if (sliderPosition > 0f) centerX + (sliderPosition * centerX) else centerX
            } else {
                highlightStart = 0f
                highlightEnd = sliderPosition * canvasWidth
            }
            drawLine(
                color = highlightColor,
                start = Offset(highlightStart, canvasHeight / 2),
                end = Offset(highlightEnd, canvasHeight / 2),
                strokeWidth = 4.dp.toPx()
            )
        }

        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                onValueChange(it)
            },
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = highlightColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}