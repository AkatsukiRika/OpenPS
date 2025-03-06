package com.akatsukirika.openps.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.activity.EditActivity
import com.akatsukirika.openps.compose.AppTheme
import com.akatsukirika.openps.compose.CropOptions
import com.akatsukirika.openps.viewmodel.CompositionViewModel
import com.pixpark.gpupixel.view.OpenPSRenderView
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class CompositionFragment : Fragment() {
    private val viewModel: CompositionViewModel
        get() = (requireActivity() as EditActivity).compositionViewModel

    private val renderView: OpenPSRenderView
        get() = (requireActivity() as EditActivity).binding.surfaceView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.initStates(
            originalBitmap = (requireActivity() as EditActivity).viewModel.currentBitmap,
            mirrorState = renderView.transformHelper.isMirrored,
            flipState = renderView.transformHelper.isFlipped
        )

        renderView.enterComposition()

        lifecycleScope.launch {
            launch {
                viewModel.isMirrored.collect {
                    renderView.doMirrorTransform(it)
                }
            }

            launch {
                viewModel.isFlipped.collect {
                    renderView.doFlipTransform(it)
                }
            }

            launch {
                viewModel.rotationDegrees.collect {
                    Log.d("CompositionFragment", "rotationDegrees = $it")
                    renderView.doRotateTransform(it)
                }
            }

            launch {
                viewModel.croppedRectF.collect {
                    Log.d("CompositionFragment", "croppedRect = $it")
                    renderView.transformHelper.setCropRect(it)
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CompositionFragScreen(viewModel)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.restoreTransformStates()
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
    val initialRect = viewModel.renderRect.collectAsState().value.toComposeRect()

    Box(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged {
            width = it.width
            height = it.height
        }
    ) {
        DraggableRect(viewModel, initialRect = initialRect, onRectChanged = {
            viewModel.croppedRect.value = it.toAndroidRectF()
        })
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

    fun getDragAmountY(dragAmount: Offset): Float {
        val ratio = viewModel.currentCropOptions.value.ratio
        val dragAmountY = if (ratio != 0f) {
            dragAmount.x / ratio
        } else {
            dragAmount.y
        }
        return dragAmountY
    }

    fun getMinCropArea(): Pair<Float, Float> {
        val ratio = viewModel.currentCropOptions.value.ratio
        return if (ratio == 0f) {
            Pair(minCropAreaSize, minCropAreaSize)
        } else {
            if (ratio > 1) {
                Pair(minCropAreaSize, minCropAreaSize / ratio)
            } else {
                Pair(minCropAreaSize * ratio, minCropAreaSize)
            }
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
                        var newLeft = max(initialRect.left, rect.left + dragAmount.x)
                        var newTop = max(initialRect.top, rect.top + dragAmount.y)
                        val minCropArea = getMinCropArea()
                        if (rect.right - newLeft < minCropArea.first) newLeft = rect.right - minCropArea.first
                        if (rect.bottom - newTop < minCropArea.second) newTop = rect.bottom - minCropArea.second

                        val ratio = viewModel.getRatio()
                        if (ratio != 0f) {
                            // 计算基于左边界的理想高度
                            val heightFromLeft = (rect.right - newLeft) / ratio
                            // 计算基于上边界的理想宽度
                            val widthFromTop = (rect.bottom - newTop) * ratio

                            if (newLeft == initialRect.left) {
                                // 如果左边到达边界，调整上边以保持比例
                                newTop = rect.bottom - heightFromLeft
                            } else if (newTop == initialRect.top) {
                                // 如果上边到达边界，调整左边以保持比例
                                newLeft = rect.right - widthFromTop
                            } else {
                                // 选择限制最严格的一边
                                if (heightFromLeft > rect.bottom - newTop) {
                                    newTop = rect.bottom - heightFromLeft
                                } else {
                                    newLeft = rect.right - widthFromTop
                                }
                            }
                        }

                        rect.copy(left = newLeft, top = newTop)
                    }

                    DraggingMode.DRAGGING_TOP_RIGHT -> {
                        var newRight = min(initialRect.right, rect.right + dragAmount.x)
                        var newTop = max(initialRect.top, rect.top + getDragAmountY(Offset(-dragAmount.x, dragAmount.y)))
                        val minCropArea = getMinCropArea()
                        if (newRight - rect.left < minCropArea.first) newRight = rect.left + minCropArea.first
                        if (rect.bottom - newTop < minCropArea.second) newTop = rect.bottom - minCropArea.second

                        // 处理比例约束
                        val ratio = viewModel.getRatio()
                        if (ratio != 0f) {
                            val heightFromRight = (newRight - rect.left) / ratio
                            val widthFromTop = (rect.bottom - newTop) * ratio

                            if (newRight == initialRect.right) {
                                newTop = rect.bottom - heightFromRight
                            } else if (newTop == initialRect.top) {
                                newRight = rect.left + widthFromTop
                            } else {
                                if (heightFromRight > rect.bottom - newTop) {
                                    newTop = rect.bottom - heightFromRight
                                } else {
                                    newRight = rect.left + widthFromTop
                                }
                            }
                        }

                        rect.copy(right = newRight, top = newTop)
                    }

                    DraggingMode.DRAGGING_BOTTOM_LEFT -> {
                        var newLeft = max(initialRect.left, rect.left + dragAmount.x)
                        var newBottom = min(initialRect.bottom, rect.bottom + getDragAmountY(Offset(-dragAmount.x, dragAmount.y)))
                        val minCropArea = getMinCropArea()
                        if (rect.right - newLeft < minCropArea.first) newLeft = rect.right - minCropArea.first
                        if (newBottom - rect.top < minCropArea.second) newBottom = rect.top + minCropArea.second

                        // 处理比例约束
                        val ratio = viewModel.getRatio()
                        if (ratio != 0f) {
                            val heightFromLeft = (rect.right - newLeft) / ratio
                            val widthFromBottom = (newBottom - rect.top) * ratio

                            if (newLeft == 0f) {
                                newBottom = rect.top + heightFromLeft
                            } else if (newBottom == initialRect.bottom) {
                                newLeft = rect.right - widthFromBottom
                            } else {
                                if (heightFromLeft < newBottom - rect.top) {
                                    newBottom = rect.top + heightFromLeft
                                } else {
                                    newLeft = rect.right - widthFromBottom
                                }
                            }
                        }

                        rect.copy(left = newLeft, bottom = newBottom)
                    }

                    DraggingMode.DRAGGING_BOTTOM_RIGHT -> {
                        var newRight = min(initialRect.right, rect.right + dragAmount.x)
                        var newBottom = min(initialRect.bottom, rect.bottom + getDragAmountY(dragAmount))
                        val minCropArea = getMinCropArea()
                        if (newRight - rect.left < minCropArea.first) newRight = rect.left + minCropArea.first
                        if (newBottom - rect.top < minCropArea.second) newBottom = rect.top + minCropArea.second

                        // 处理比例约束
                        val ratio = viewModel.getRatio()
                        if (ratio != 0f) {
                            val heightFromRight = (newRight - rect.left) / ratio
                            val widthFromBottom = (newBottom - rect.top) * ratio

                            if (newRight == size.width.toFloat()) {
                                newBottom = rect.top + heightFromRight
                            } else if (newBottom == initialRect.bottom) {
                                newRight = rect.left + widthFromBottom
                            } else {
                                if (heightFromRight < newBottom - rect.top) {
                                    newBottom = rect.top + heightFromRight
                                } else {
                                    newRight = rect.left + widthFromBottom
                                }
                            }
                        }

                        rect.copy(right = newRight, bottom = newBottom)
                    }
                    // 拖动左边
                    DraggingMode.DRAGGING_LEFT -> {
                        if (viewModel.currentCropOptions.value == CropOptions.CUSTOM) {
                            val newLeft = (rect.left + dragAmount.x)
                                .coerceIn(initialRect.left, rect.right - minCropAreaSize)
                            rect.copy(left = newLeft)
                        } else rect
                    }
                    // 拖动右边
                    DraggingMode.DRAGGING_RIGHT -> {
                        if (viewModel.currentCropOptions.value == CropOptions.CUSTOM) {
                            val newRight = (rect.right + dragAmount.x)
                                .coerceIn(rect.left + minCropAreaSize, initialRect.right)
                            rect.copy(right = newRight)
                        } else rect
                    }
                    // 拖动上边
                    DraggingMode.DRAGGING_TOP -> {
                        if (viewModel.currentCropOptions.value == CropOptions.CUSTOM) {
                            var newTop = max(initialRect.top, rect.top + dragAmount.y)
                            if (rect.bottom - newTop < minCropAreaSize) {
                                newTop = rect.bottom - minCropAreaSize
                            }
                            rect.copy(top = newTop)
                        } else rect
                    }
                    // 拖动下边
                    DraggingMode.DRAGGING_BOTTOM -> {
                        if (viewModel.currentCropOptions.value == CropOptions.CUSTOM) {
                            var newBottom = min(initialRect.bottom, rect.bottom + dragAmount.y)
                            if (newBottom - rect.top < minCropAreaSize) {
                                newBottom = rect.top + minCropAreaSize
                            }
                            rect.copy(bottom = newBottom)
                        } else rect
                    }
                    // 在裁剪区域内部拖动时，移动整个区域
                    DraggingMode.DRAGGING_INSIDE -> {
                        val newLeft = runCatching {
                            (rect.left + dragAmount.x).coerceIn(initialRect.left, initialRect.right - rect.width)
                        }.getOrNull() ?: rect.left
                        val newTop = runCatching {
                            (rect.top + dragAmount.y).coerceIn(initialRect.top, initialRect.bottom - rect.height)
                        }.getOrNull() ?: rect.top
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