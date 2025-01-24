package com.akatsukirika.openps.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.akatsukirika.openps.viewmodel.CompositionViewModel
import kotlin.math.max
import kotlin.math.min

class CompositionFragment(private val viewModel: CompositionViewModel) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                CompositionFragScreen(viewModel)
            }
        }
    }
}

@Composable
fun CompositionFragScreen(viewModel: CompositionViewModel) {
    var width by remember {
        mutableIntStateOf(0)
    }
    var height by remember {
        mutableIntStateOf(0)
    }
    val initialRect = remember(width, height) {
        val scaledWidth = viewModel.renderViewInfo?.scaledWidth ?: 0f
        val scaledHeight = viewModel.renderViewInfo?.scaledHeight ?: 0f

        val offsetX = ((1 - scaledWidth) / 2) * width
        val offsetY = ((1 - scaledHeight) / 2) * height
        val sizeWidth = scaledWidth * width
        val sizeHeight = scaledHeight * height

        Rect(offset = Offset(offsetX, offsetY), size = Size(sizeWidth, sizeHeight))
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged {
            width = it.width
            height = it.height
        }
    ) {
        DraggableRect(initialRect = initialRect, onRectChanged = {})
    }
}

@Composable
private fun DraggableRect(initialRect: Rect, onRectChanged: (Rect) -> Unit) {
    val density = LocalDensity.current
    var rect by remember(initialRect) {
        mutableStateOf(initialRect)
    }
    val strokeSize = with(density) {
        4.dp.toPx()
    }
    val paint = remember {
        Paint().apply {
            color = Color.White
            strokeWidth = strokeSize
            style = PaintingStyle.Stroke
        }
    }
    val gridStrokeSize = with(density) {
        1.dp.toPx()
    }
    val gridPaint = remember {
        Paint().apply {
            color = Color.White
            strokeWidth = gridStrokeSize
            style = PaintingStyle.Stroke
        }
    }
    val detectArea = with(density) {
        36.dp.toPx()
    }
    var isDragging by remember {
        mutableStateOf(false)
    }

    /**
     * 除裁剪区域外，绘制半透明黑色遮罩
     */
    fun DrawScope.drawMask(canvas: Canvas) {
        canvas.save()

        // 裁剪出选择区域（这个区域将不会被绘制）
        canvas.clipRect(rect, ClipOp.Difference)
        // 绘制整个画布的半透明黑色遮罩
        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            paint = Paint().apply {
                color = Color.Black.copy(alpha = 0.5f)
                style = PaintingStyle.Fill
            }
        )

        canvas.restore()
    }

    /**
     * 绘制裁剪区域
     */
    fun drawCropArea(canvas: Canvas) {
        canvas.drawRect(
            left = rect.left + strokeSize / 2,
            top = rect.top,
            right = rect.right - strokeSize / 2,
            bottom = rect.bottom,
            paint
        )
    }

    /**
     * 绘制九宫格
     */
    fun drawGrid(canvas: Canvas) {
        val cellWidth = rect.width / 3
        val cellHeight = rect.height / 3

        // 绘制垂直线
        for (i in 1..2) {
            val x = rect.left + cellWidth * i
            canvas.drawLine(
                p1 = Offset(x, rect.top),
                p2 = Offset(x, rect.bottom),
                paint = gridPaint
            )
        }

        // 绘制水平线
        for (i in 1..2) {
            val y = rect.top + cellHeight * i
            canvas.drawLine(
                p1 = Offset(rect.left, y),
                p2 = Offset(rect.right, y),
                paint = gridPaint
            )
        }
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    isDragging = true
                },
                onDragEnd = {
                    isDragging = false
                },
                onDragCancel = {
                    isDragging = false
                }
            ) { change, dragAmount ->
                val newRect = when {
                    // 拖动左边
                    change.position.x in (rect.left - detectArea)..(rect.left + detectArea) -> {
                        rect.copy(left = max(0f, rect.left + dragAmount.x))
                    }
                    // 拖动右边
                    change.position.x in (rect.right - detectArea)..(rect.right + detectArea) -> {
                        rect.copy(right = min(size.width.toFloat(), rect.right + dragAmount.x))
                    }
                    // 拖动上边
                    change.position.y in (rect.top - detectArea)..(rect.top + detectArea) -> {
                        rect.copy(top = max(0f, rect.top + dragAmount.y))
                    }
                    // 拖动下边
                    change.position.y in (rect.bottom - detectArea)..(rect.bottom + detectArea) -> {
                        rect.copy(bottom = min(size.height.toFloat(), rect.bottom + dragAmount.y))
                    }

                    else -> rect
                }
                rect = newRect
                onRectChanged(newRect)
            }
        }
    ) {
        drawIntoCanvas { canvas ->
            drawMask(canvas)

            drawCropArea(canvas)

            // 拖动时绘制九宫格
            if (isDragging) {
                drawGrid(canvas)
            }
        }
    }
}