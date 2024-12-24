package com.akatsukirika.openps.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.akatsukirika.openps.R
import com.akatsukirika.openps.model.EliminatePenPath
import com.akatsukirika.openps.utils.MatrixUtils.getParams
import com.akatsukirika.openps.utils.MatrixUtils.getScaleX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class EliminatePaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    companion object {
        const val TAG = "EliminatePaintView"
        const val BRUSH_SIZE_DEFAULT = 0.5f
    }

    private val strokeWidthMin = context.resources.getDimension(R.dimen.stroke_width_min)
    private val strokeWidthMax = context.resources.getDimension(R.dimen.stroke_width_max)
    private val strokeWidthLasso = context.resources.getDimension(R.dimen.stroke_width_lasso)

    private val pathList = mutableListOf<EliminatePenPath>()
    private var currentEraserPath: EliminatePenPath? = null
    private var currentPath: Path? = null
    private var currentPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        color = context.getColor(R.color.eliminate_paint_color)
        strokeWidth = strokeWidthMin
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var eraserPaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        strokeWidth = strokeWidthMin
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.eliminate_paint_color)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        isAntiAlias = true
    }
    private var restorePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
        strokeWidth = strokeWidthMin
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val debugPaint = Paint().apply {
        color = Color.RED
        textSize = 40f
        isAntiAlias = true
    }
    private var showIndicator = false
    private var showIndicatorPreview = false
    private var disableTouch = true
    private var isTouching = false
    private var isMultiTouch = false
    private var isLassoMode = false
    private var isEraseMode = false
    private var isRestoreMode = false
    private var isDebugMode = false
    private var isMoved = false
    private var touchX = 0f
    private var touchY = 0f
    private var rawTouchX = 0f
    private var rawTouchY = 0f
    private var indicatorSize = strokeWidthMin
    private val enablePerformanceLog = false

    private var magnifyView: EliminateZoomView? = null
    private var imageView: View? = null
    private var imageBitmap: Bitmap? = null
    private var drawingBitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private var callback: Callback? = null

    private val handler = Handler(Looper.getMainLooper())
    private val drawingDelay = 50L
    private val imageMatrix = Matrix()
    private val inverseMatrix = Matrix()
    private val defaultMatrix = Matrix()
    private var initialScale: Float? = null

    private val startDrawingRunnable = Runnable {
        if (!isMultiTouch) {
            Log.d(TAG, "Start drawing!!")
            currentPath?.let {
                currentEraserPath = EliminatePenPath(
                    paint = if (isEraseMode) eraserPaint else if (isRestoreMode) restorePaint else currentPaint,
                    path = it,
                    isRestore = isRestoreMode,
                    isErase = isEraseMode
                )
                currentEraserPath?.let { eraserPath ->
                    pathList.add(eraserPath)
                }
                setTouchStatus(true)
                updateMagnifyView()
            }
        }
    }
    private var dismissIndicatorPreviewJob: Job? = null

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private fun resetCanvas() {
        imageBitmap?.let {
            drawingBitmap = Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(drawingBitmap!!)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            resetCanvas()
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawingBitmap ?: return
        val beginTime = System.currentTimeMillis()
        canvas.concat(imageMatrix)
        bitmapCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        var needNewLayer = false
        var newLayerId: Int? = null
        pathList.forEachIndexed { index, it ->
            if (needNewLayer) {
                newLayerId?.let {
                    bitmapCanvas?.restoreToCount(it)
                }
                newLayerId = bitmapCanvas?.saveLayer(null, null)
                needNewLayer = false
            }
            bitmapCanvas?.drawPath(it.path, it.paint)
            if (it.isRestore && pathList.getOrNull(index + 1)?.isRestore == false) {
                needNewLayer = true
            }
        }
        newLayerId?.let {
            bitmapCanvas?.restoreToCount(it)
        }

        canvas.drawBitmap(drawingBitmap!!, 0f, 0f, null)

        if (showIndicator && isTouching) {
            drawIndicator(canvas)
        }
        if (isDebugMode) {
            drawDebugInfo(canvas)
        }

        val saveCount = canvas.save()
        canvas.setMatrix(defaultMatrix)
        if (showIndicatorPreview) {
            drawIndicatorForPreview(canvas)
            dismissIndicatorPreviewJob?.cancel()
            dismissIndicatorPreviewJob = MainScope().launch(Dispatchers.Main) {
                delay(500)
                showIndicatorPreview = false
                invalidate()
            }
        }
        canvas.restoreToCount(saveCount)

        val elapsedTime = System.currentTimeMillis() - beginTime
        logPerformance("onDraw() 绘制 ${pathList.size} 条 Path 耗时 ${elapsedTime}ms")
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.dispatchTouchEvent(null)

        if (disableTouch) {
            return false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isMultiTouch = false
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                isMultiTouch = true
                handler.removeCallbacks(startDrawingRunnable)
                setTouchStatus(false)
                callback?.onActionUpOrCancel()
            }
        }

        if (isMultiTouch) {
            imageView?.let {
                val newEvent = MotionEvent.obtain(event)
                val location = IntArray(2)
                it.getLocationOnScreen(location)
                newEvent.offsetLocation(-location[0].toFloat(), -location[1].toFloat())

                it.dispatchTouchEvent(newEvent)
                newEvent.recycle()
            }
            return true
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(null)
        if (isMultiTouch) {
            return false
        }

        rawTouchX = event.x
        rawTouchY = event.y

        val touchPoint = floatArrayOf(event.x, event.y)
        inverseMatrix.mapPoints(touchPoint)

        touchX = touchPoint[0]
        touchY = touchPoint[1]
        callback?.onTouchEvent(rawTouchX, rawTouchY)

        val normalizedTouchPoint = convertToNormalizedCoordinates(x = touchX, y = touchY)
        val normalTouchX = normalizedTouchPoint.first
        val normalTouchY = normalizedTouchPoint.second
        val isOutsideTouch = normalTouchX < 0f || normalTouchX > 1f || normalTouchY < 0f || normalTouchY > 1f

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "ACTION_DOWN touchX=$touchX, touchY=$touchY, isOutsideTouch=$isOutsideTouch")
                isMoved = false
                if (isOutsideTouch) {
                    return false
                }
                currentPath = Path().apply {
                    moveTo(touchX, touchY)
                }.also {
                    handler.postDelayed(startDrawingRunnable, drawingDelay)
                }
                if (showIndicatorPreview) {
                    showIndicatorPreview = false
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "ACTION_MOVE touchX=$touchX, touchY=$touchY")
                isMoved = true
                if (isOutsideTouch) {
                    magnifyView?.visibility = GONE
                } else {
                    magnifyView?.visibility = VISIBLE
                    updateMagnifyView()
                }
                currentPath?.lineTo(touchX, touchY)
                if (isRestoreMode) {
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "ACTION_UP/ACTION_CANCEL touchX=$touchX, touchY=$touchY")
                // 点一下也要展示涂抹效果
                currentPath?.lineTo(touchX.roundToInt().toFloat(), touchY.roundToInt().toFloat())
                actionUpOrCancel()
                isMoved = false
            }

            else -> return false
        }
        invalidate()
        return true
    }

    private fun actionUpOrCancel() {
        if (isLassoMode && !isEraseMode) {
            currentPath?.let {
                it.close()
                pathList.remove(currentEraserPath)
                if (isMoved) {
                    val fillPath = EliminatePenPath(
                        paint = fillPaint,
                        path = it,
                        isLasso = true
                    )
                    pathList.add(fillPath)
                }
            }
        }
        currentPath = null
        setTouchStatus(false)
        isMultiTouch = false
        callback?.onActionUpOrCancel()
    }

    private fun drawIndicator(canvas: Canvas?) {
        val borderSize = context.resources.getDimension(R.dimen.indicator_border_width)
        val defaultIndicatorSize = BRUSH_SIZE_DEFAULT * (strokeWidthMax - strokeWidthMin) + strokeWidthMin
        val size = if (isLassoMode && !isEraseMode) defaultIndicatorSize else indicatorSize
        val outerRadius = (size + borderSize) / (imageMatrix.getScaleX()) / 2
        val innerRadius = size / (imageMatrix.getScaleX()) / 2

        canvas?.drawCircle(touchX, touchY, outerRadius, Paint().apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.STROKE
            strokeWidth = borderSize / imageMatrix.getScaleX()
        })

        canvas?.drawCircle(touchX, touchY, innerRadius, Paint().apply {
            color = context.getColor(R.color.eliminate_paint_color)
            style = Paint.Style.FILL
        })
    }

    private fun drawIndicatorForPreview(canvas: Canvas?) {
        val borderSize = context.resources.getDimension(R.dimen.indicator_border_width)
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() - resources.getDimension(R.dimen.indicator_preview_margin_bottom)
        canvas?.drawCircle(centerX, centerY, (indicatorSize + borderSize) / 2, Paint().apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.STROKE
            strokeWidth = borderSize
        })
        canvas?.drawCircle(centerX, centerY, indicatorSize / 2, Paint().apply {
            color = context.getColor(R.color.eliminate_paint_color)
            style = Paint.Style.FILL
        })
    }

    private fun setTouchStatus(isTouching: Boolean) {
        this.isTouching = isTouching
        magnifyView?.visibility = if (isTouching) VISIBLE else GONE
    }

    private fun updateMagnifyView() {
        if (drawingBitmap != null && imageBitmap != null) {
            magnifyView?.update(
                imageBitmap = imageBitmap!!,
                drawingBitmap = drawingBitmap!!,
                touchX = touchX,
                touchY = touchY,
                scale = imageMatrix.getScaleX(),
                isLassoMode, isEraseMode, indicatorSize
            )
        }
    }

    private fun drawDebugInfo(canvas: Canvas) {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        val drawingViewLocX = location[0]
        val drawingViewLocY = location[1]
        val drawingViewWidth = width
        val drawingViewHeight = height

        val matrixParams = imageMatrix.getParams()
        val normalizedTouchPoint = convertToNormalizedCoordinates(x = touchX, y = touchY)

        canvas.drawText("drawingView: X=$drawingViewLocX, Y=$drawingViewLocY, W=$drawingViewWidth, H=$drawingViewHeight", 10f, 80f, debugPaint)
        canvas.drawText("imageMatrix: scaleX=${matrixParams.scaleX}, scaleY=${matrixParams.scaleY}", 10f, 120f, debugPaint)
        canvas.drawText("imageMatrix: translateX=${matrixParams.translateX}, translateY=${matrixParams.translateY}", 10f, 160f, debugPaint)
        canvas.drawText("touch: X=$touchX, Y=$touchY", 10f, 200f, debugPaint)
        canvas.drawText("normalizedTouch: X=${normalizedTouchPoint.first}, Y=${normalizedTouchPoint.second}", 10f, 240f, debugPaint)
    }

    /**
     * 将屏幕上任意一个点的坐标转换为基于图片区域的归一化坐标
     */
    private fun convertToNormalizedCoordinates(x: Float, y: Float): Pair<Float, Float> {
        val imageWidth = imageBitmap?.width ?: return Pair(0f, 0f)
        val imageHeight = imageBitmap?.height ?: return Pair(0f, 0f)

        val normalizedX = x / imageWidth
        val normalizedY = y / imageHeight

        return Pair(normalizedX, normalizedY)
    }

    /**
     * @param size 0.0f ~ 1.0f
     */
    fun setBrushSize(size: Float, previewIndicator: Boolean = true) {
        isLassoMode = false
        val originalStrokeWidth = size * (strokeWidthMax - strokeWidthMin) + strokeWidthMin
        val realStrokeWidth = originalStrokeWidth / imageMatrix.getScaleX()
        currentPaint = Paint(currentPaint).apply {
            strokeWidth = realStrokeWidth
            indicatorSize = originalStrokeWidth
            pathEffect = null
            color = context.getColor(R.color.eliminate_paint_color)
            isAntiAlias = true
        }
        eraserPaint = Paint(eraserPaint).apply {
            strokeWidth = realStrokeWidth
            indicatorSize = originalStrokeWidth
        }
        restorePaint = Paint(restorePaint).apply {
            strokeWidth = realStrokeWidth
            indicatorSize = originalStrokeWidth
        }
        showIndicatorPreview = previewIndicator
        invalidate()
    }

    fun setDashedLine() {
        isLassoMode = true
        currentPaint = Paint(currentPaint).apply {
            val scale = initialScale ?: 1f
            strokeWidth = strokeWidthLasso / scale
            val distance = context.resources.getDimension(R.dimen.lasso_dash_distance)
            pathEffect = DashPathEffect(floatArrayOf(distance / scale, distance / scale), 0f)
            color = context.getColor(R.color.green_500)
            isAntiAlias = true
        }
    }

    fun showIndicator(show: Boolean) {
        showIndicator = show
    }

    fun setMagnifier(magnifier: EliminateZoomView) {
        magnifyView = magnifier
    }

    fun setCustomImageView(customImageView: View, bitmap: Bitmap?) {
        imageView = customImageView
        if (bitmap != imageBitmap) {
            imageBitmap = bitmap
            resetCanvas()
        }
    }

    fun setDebug(debug: Boolean) {
        isDebugMode = debug
    }

    fun setErase(erase: Boolean) {
        isEraseMode = erase
    }

    fun setRestoreBitmap(bitmap: Bitmap) {
        restorePaint.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    fun startRestore() {
        isRestoreMode = true
    }

    fun endRestore() {
        isRestoreMode = false
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setImageMatrix(matrix: Matrix, isInit: Boolean) {
        imageMatrix.set(matrix)
        imageMatrix.invert(inverseMatrix)
        if (isInit && initialScale == null) {
            initialScale = imageMatrix.getScaleX()
        }
        if (!isLassoMode) {
            currentPaint = Paint(currentPaint).apply {
                strokeWidth = indicatorSize / imageMatrix.getScaleX()
            }
        }
        eraserPaint = Paint(eraserPaint).apply {
            strokeWidth = indicatorSize / imageMatrix.getScaleX()
        }
        restorePaint = Paint(restorePaint).apply {
            strokeWidth = indicatorSize / imageMatrix.getScaleX()
        }
        invalidate()
    }

    /**
     * @return 清除前[pathList]非空返回true，否则返回false
     * @param strokeOnly 只清除画笔路径，不清除恢复模式下的路径
     */
    fun clearDrawing(strokeOnly: Boolean = false): Boolean {
        val result = pathList.isEmpty().not()
        if (!strokeOnly) {
            pathList.clear()
        } else {
            pathList.removeAll { !it.isRestore }
        }
        if (width > 0 && height > 0) {
            resetCanvas()
        }
        invalidate()
        return result
    }

    fun getPaths(): List<EliminatePenPath> {
        return pathList
    }

    fun setPaths(newPaths: List<EliminatePenPath>) {
        pathList.clear()
        pathList.addAll(newPaths)
        invalidate()
    }

    fun setDisableTouch(disable: Boolean) {
        disableTouch = disable
        invalidate()
    }

    fun getDrawingAreaBitmap() = drawingBitmap

    fun getDrawingAreaMask(): Bitmap? {
        drawingBitmap?.let {
            val width = it.width
            val height = it.height
            val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val outputCanvas = Canvas(outputBitmap)
            pathList.filter { !it.isRestore }.forEach { eraserPath ->
                outputCanvas.drawPath(eraserPath.path, eraserPath.paint)
            }
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pixel = outputBitmap.getPixel(x, y)
                    val alpha = Color.alpha(pixel)
                    if (alpha == 0) {
                        outputBitmap.setPixel(x, y, Color.BLACK)
                    } else {
                        outputBitmap.setPixel(x, y, Color.WHITE)
                    }
                }
            }
            return outputBitmap
        }
        return null
    }

    private fun logPerformance(message: String) {
        if (enablePerformanceLog) {
            Log.e(TAG, message)
        }
    }

    interface Callback {
        fun onActionUpOrCancel()
        fun onTouchEvent(touchX: Float, touchY: Float)
    }
}