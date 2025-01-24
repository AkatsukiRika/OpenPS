package com.akatsukirika.openps.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.AppTheme
import com.akatsukirika.openps.compose.CropOptions
import com.akatsukirika.openps.viewmodel.CompositionViewModel
import kotlin.math.max
import kotlin.math.min

class CompositionFragment(private val viewModel: CompositionViewModel) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.initStates()

        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CompositionFragScreen(viewModel)
                }
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
    val canSave = viewModel.canSave.collectAsState().value

    Box(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged {
            width = it.width
            height = it.height
        }
    ) {
        DraggableRect(viewModel, initialRect = initialRect, onRectChanged = {
            viewModel.canSave.value =
                        (it.left == initialRect.left &&
                        it.top == initialRect.top &&
                        it.right == initialRect.right &&
                        it.bottom == initialRect.bottom).not()
        })

        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp),
            shape = RoundedCornerShape(100.dp),
            enabled = canSave
        ) {
            Text(text = stringResource(id = R.string.save_changes))
        }
    }
}

enum class DraggingMode {
    NOT_DRAGGING, DRAGGING_INSIDE,
    DRAGGING_LEFT, DRAGGING_RIGHT, DRAGGING_TOP, DRAGGING_BOTTOM,
    DRAGGING_TOP_LEFT, DRAGGING_TOP_RIGHT, DRAGGING_BOTTOM_LEFT, DRAGGING_BOTTOM_RIGHT
}

@Composable
private fun DraggableRect(viewModel: CompositionViewModel, initialRect: Rect, onRectChanged: (Rect) -> Unit) {
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
            color = Color.White.copy(alpha = 0.5f)
            strokeWidth = gridStrokeSize
            style = PaintingStyle.Stroke
        }
    }
    val detectArea = with(density) {
        36.dp.toPx()
    }
    var draggingMode by remember {
        mutableStateOf(DraggingMode.NOT_DRAGGING)
    }
    val minCropAreaSize = with(density) {
        100.dp.toPx()
    }
    val currentCropOptions = viewModel.currentCropOptions.collectAsState().value

    fun resetWithRatio(ratio: Float) {
        val initialRatio = initialRect.width / initialRect.height
        rect = if (initialRatio < ratio) {
            // 宽度占满initialRect的宽度，高在initialRect内居中按比例适配
            val newHeight = initialRect.width / ratio
            val newTop = initialRect.top + (initialRect.height - newHeight) / 2
            rect.copy(left = initialRect.left, top = newTop, right = initialRect.right, bottom = newTop + newHeight)
        } else {
            // 高度占满initialRect的高度，宽在initialRect内居中按比例适配
            val newWidth = initialRect.height * ratio
            val newLeft = initialRect.left + (initialRect.width - newWidth) / 2
            rect.copy(left = newLeft, top = initialRect.top, right = newLeft + newWidth, bottom = initialRect.bottom)
        }
        onRectChanged(rect)
    }

    LaunchedEffect(key1 = currentCropOptions) {
        when (currentCropOptions) {
            CropOptions.CUSTOM, CropOptions.ORIGINAL -> {
                rect = rect.copy(left = initialRect.left, top = initialRect.top, right = initialRect.right, bottom = initialRect.bottom)
                onRectChanged(rect)
            }
            else -> {
                resetWithRatio(currentCropOptions.ratio)
            }
        }
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
        val left = rect.left + strokeSize / 2
        val right = rect.right - strokeSize / 2
        val top = rect.top + strokeSize / 2
        val bottom = rect.bottom - strokeSize / 2
        val width = right - left
        val height = bottom - top

        fun drawEdge(start: Offset, isHorizontal: Boolean) {
            val length = if (isHorizontal) width else height
            val direction = if (isHorizontal) Offset(1f, 0f) else Offset(0f, 1f)

            for (i in 0..4) {
                paint.strokeWidth = if (i == 2 && currentCropOptions != CropOptions.CUSTOM) {
                    gridStrokeSize
                } else if (i % 2 == 0) strokeSize else gridStrokeSize
                val segmentStart = start + direction * (length * i / 5)
                val segmentEnd = start + direction * (length * (i + 1) / 5)
                canvas.drawLine(p1 = segmentStart, p2 = segmentEnd, paint)
            }
        }

        drawEdge(Offset(left, top), true)  // 上边
        drawEdge(Offset(right, top), false)  // 右边
        drawEdge(Offset(left, bottom), true)  // 下边
        drawEdge(Offset(left, top), false)  // 左边
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
                onDragEnd = {
                    draggingMode = DraggingMode.NOT_DRAGGING
                },
                onDragCancel = {
                    draggingMode = DraggingMode.NOT_DRAGGING
                }
            ) { change, dragAmount ->
                if (draggingMode == DraggingMode.NOT_DRAGGING) {
                    // 状态转移
                    draggingMode = when {
                        change.position.x in (rect.left - detectArea)..(rect.left + detectArea) &&
                                change.position.y in (rect.top - detectArea)..(rect.top + detectArea) -> DraggingMode.DRAGGING_TOP_LEFT

                        change.position.x in (rect.right - detectArea)..(rect.right + detectArea) &&
                                change.position.y in (rect.top - detectArea)..(rect.top + detectArea) -> DraggingMode.DRAGGING_TOP_RIGHT

                        change.position.x in (rect.left - detectArea)..(rect.left + detectArea) &&
                                change.position.y in (rect.bottom - detectArea)..(rect.bottom + detectArea) -> DraggingMode.DRAGGING_BOTTOM_LEFT

                        change.position.x in (rect.right - detectArea)..(rect.right + detectArea) &&
                                change.position.y in (rect.bottom - detectArea)..(rect.bottom + detectArea) -> DraggingMode.DRAGGING_BOTTOM_RIGHT

                        change.position.x in (rect.left - detectArea)..(rect.left + detectArea) -> DraggingMode.DRAGGING_LEFT
                        change.position.x in (rect.right - detectArea)..(rect.right + detectArea) -> DraggingMode.DRAGGING_RIGHT
                        change.position.y in (rect.top - detectArea)..(rect.top + detectArea) -> DraggingMode.DRAGGING_TOP
                        change.position.y in (rect.bottom - detectArea)..(rect.bottom + detectArea) -> DraggingMode.DRAGGING_BOTTOM
                        change.position.x in (rect.left + detectArea)..(rect.right - detectArea) &&
                                change.position.y in (rect.top + detectArea)..(rect.bottom - detectArea) -> DraggingMode.DRAGGING_INSIDE

                        else -> DraggingMode.NOT_DRAGGING
                    }
                }

                val newRect = when (draggingMode) {
                    // 四个角落的拖拽
                    DraggingMode.DRAGGING_TOP_LEFT -> {
                        var newLeft = max(0f, rect.left + dragAmount.x)
                        var newTop = max(initialRect.top, rect.top + dragAmount.y)
                        if (rect.right - newLeft < minCropAreaSize) newLeft = rect.right - minCropAreaSize
                        if (rect.bottom - newTop < minCropAreaSize) newTop = rect.bottom - minCropAreaSize
                        rect.copy(left = newLeft, top = newTop)
                    }

                    DraggingMode.DRAGGING_TOP_RIGHT -> {
                        var newRight = min(size.width.toFloat(), rect.right + dragAmount.x)
                        var newTop = max(initialRect.top, rect.top + dragAmount.y)
                        if (newRight - rect.left < minCropAreaSize) newRight = rect.left + minCropAreaSize
                        if (rect.bottom - newTop < minCropAreaSize) newTop = rect.bottom - minCropAreaSize
                        rect.copy(right = newRight, top = newTop)
                    }

                    DraggingMode.DRAGGING_BOTTOM_LEFT -> {
                        var newLeft = max(0f, rect.left + dragAmount.x)
                        var newBottom = min(initialRect.bottom, rect.bottom + dragAmount.y)
                        if (rect.right - newLeft < minCropAreaSize) newLeft = rect.right - minCropAreaSize
                        if (newBottom - rect.top < minCropAreaSize) newBottom = rect.top + minCropAreaSize
                        rect.copy(left = newLeft, bottom = newBottom)
                    }

                    DraggingMode.DRAGGING_BOTTOM_RIGHT -> {
                        var newRight = min(size.width.toFloat(), rect.right + dragAmount.x)
                        var newBottom = min(initialRect.bottom, rect.bottom + dragAmount.y)
                        if (newRight - rect.left < minCropAreaSize) newRight = rect.left + minCropAreaSize
                        if (newBottom - rect.top < minCropAreaSize) newBottom = rect.top + minCropAreaSize
                        rect.copy(right = newRight, bottom = newBottom)
                    }
                    // 拖动左边
                    DraggingMode.DRAGGING_LEFT -> {
                        var newLeft = max(0f, rect.left + dragAmount.x)
                        if (rect.right - newLeft < minCropAreaSize) {
                            newLeft = rect.right - minCropAreaSize
                        }
                        rect.copy(left = newLeft)
                    }
                    // 拖动右边
                    DraggingMode.DRAGGING_RIGHT -> {
                        var newRight = min(size.width.toFloat(), rect.right + dragAmount.x)
                        if (newRight - rect.left < minCropAreaSize) {
                            newRight = rect.left + minCropAreaSize
                        }
                        rect.copy(right = newRight)
                    }
                    // 拖动上边
                    DraggingMode.DRAGGING_TOP -> {
                        var newTop = max(initialRect.top, rect.top + dragAmount.y)
                        if (rect.bottom - newTop < minCropAreaSize) {
                            newTop = rect.bottom - minCropAreaSize
                        }
                        rect.copy(top = newTop)
                    }
                    // 拖动下边
                    DraggingMode.DRAGGING_BOTTOM -> {
                        var newBottom = min(initialRect.bottom, rect.bottom + dragAmount.y)
                        if (newBottom - rect.top < minCropAreaSize) {
                            newBottom = rect.top + minCropAreaSize
                        }
                        rect.copy(bottom = newBottom)
                    }
                    // 在裁剪区域内部拖动时，移动整个区域
                    DraggingMode.DRAGGING_INSIDE -> {
                        val newLeft = (rect.left + dragAmount.x).coerceIn(initialRect.left, initialRect.right - rect.width)
                        val newTop = (rect.top + dragAmount.y).coerceIn(initialRect.top, initialRect.bottom - rect.height)
                        rect.copy(left = newLeft, top = newTop, right = newLeft + rect.width, bottom = newTop + rect.height)
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
            if (draggingMode != DraggingMode.NOT_DRAGGING) {
                drawGrid(canvas)
            }
        }
    }
}