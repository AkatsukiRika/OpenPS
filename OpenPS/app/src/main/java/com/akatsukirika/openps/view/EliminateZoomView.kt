package com.akatsukirika.openps.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.akatsukirika.openps.R
import kotlin.math.roundToInt

class EliminateZoomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        const val TAG = "EliminateZoomView"
    }

    private val borderWidth = context.resources.getDimension(R.dimen.zoom_view_border_width)
    private val borderRadius = context.resources.getDimension(R.dimen.zoom_view_border_radius)
    private val magnifyAreaWidth = context.resources.getDimension(R.dimen.zoom_area_width)
    private val magnifyAreaHeight = context.resources.getDimension(R.dimen.zoom_area_height)
    private val strokeWidthMin = context.resources.getDimension(R.dimen.stroke_width_min)
    private val strokeWidthMax = context.resources.getDimension(R.dimen.stroke_width_max)

    private val indicatorPaint = Paint().apply {
        color = context.getColor(R.color.eliminate_paint_color)
        style = Paint.Style.FILL
    }
    private val indicatorBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.white)
        style = Paint.Style.STROKE
        strokeWidth = borderWidth
    }

    private val rectF = RectF()
    private val clipPath = Path()

    private val srcRect = Rect()
    private val dstRect = Rect()

    private var magnifyBitmap: Bitmap? = null
    private var magnifyCanvas: Canvas? = null

    private var imageBitmap: Bitmap? = null
    private var drawingBitmap: Bitmap? = null
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var scale: Float = 1f
    private var indicatorSize = strokeWidthMin

    private var isLassoMode = false
    private var isEraseMode = false
    private val enablePerformanceLog = false

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        val beginTime = System.currentTimeMillis()
        super.onDraw(canvas)

        // 绘制边框
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        clipPath.addRoundRect(rectF, borderRadius, borderRadius, Path.Direction.CW)
        canvas.clipPath(clipPath)

        // 创建画布
        if (magnifyBitmap == null) {
            magnifyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            magnifyCanvas = Canvas(magnifyBitmap!!)
        }

        val canvasWidth = getWidthInCanvas()
        val canvasHeight = getHeightInCanvas()
        val left = (touchX - canvasWidth / 2).roundToInt()
        val top = (touchY - canvasHeight / 2).roundToInt()
        srcRect.set(left, top, (left + canvasWidth).roundToInt(), (top + canvasHeight).roundToInt())
        dstRect.set(0, 0, width, height)

        // 绘制背景色
        magnifyCanvas?.drawColor(resources.getColor(R.color.black, null))
        // 绘制原图
        imageBitmap?.let {
            magnifyCanvas?.drawBitmap(it, srcRect, dstRect, null)
        }
        // 绘制画笔内容
        drawingBitmap?.let {
            magnifyCanvas?.drawBitmap(it, srcRect, dstRect, null)
        }
        // 绘制画笔触点
        drawIndicator(magnifyCanvas)

        // 绘制上屏
        magnifyBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        indicatorBorderPaint.strokeWidth = borderWidth * 2
        canvas.drawRoundRect(rectF, borderRadius, borderRadius, indicatorBorderPaint)
        logPerformance("onDraw() 耗时: ${System.currentTimeMillis() - beginTime}ms")
    }

    fun update(
        imageBitmap: Bitmap, drawingBitmap: Bitmap,
        touchX: Float, touchY: Float, scale: Float,
        isLassoMode: Boolean, isEraseMode: Boolean,
        indicatorSize: Float
    ) {
        this.imageBitmap = imageBitmap
        this.drawingBitmap = drawingBitmap
        this.touchX = touchX
        this.touchY = touchY
        this.scale = scale
        this.isLassoMode = isLassoMode
        this.isEraseMode = isEraseMode
        this.indicatorSize = indicatorSize
        invalidate()
    }

    private fun drawIndicator(canvas: Canvas?) {
        val defaultIndicatorSize = EliminatePaintView.BRUSH_SIZE_DEFAULT * (strokeWidthMax - strokeWidthMin) + strokeWidthMin
        val size = if (isLassoMode && !isEraseMode) defaultIndicatorSize else indicatorSize
        val innerRadius = size / 2
        indicatorBorderPaint.strokeWidth = borderWidth

        val borderLayerId = canvas?.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas?.drawCircle(width / 2f, height / 2f, innerRadius + borderWidth / 2, indicatorBorderPaint)
        borderLayerId?.let {
            canvas.restoreToCount(it)
        }

        val indicatorLayerId = canvas?.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas?.drawCircle(width / 2f, height / 2f, innerRadius, indicatorPaint)
        indicatorLayerId?.let {
            canvas.restoreToCount(it)
        }
    }

    private fun getWidthInCanvas() = magnifyAreaWidth / scale

    private fun getHeightInCanvas() = magnifyAreaHeight / scale

    private fun logPerformance(message: String) {
        if (enablePerformanceLog) {
            Log.e(TAG, message)
        }
    }
}