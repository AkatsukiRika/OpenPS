package com.pixpark.gpupixel.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.pixpark.gpupixel.OpenGLTransformHelper

class OpenPSRenderView : GLSurfaceView {
    interface Callback {
        fun onMatrixChanged(matrix: Matrix)
        fun onImageMatrixChanged(matrix: Matrix)
        fun onGLMatrixChanged(glMatrix: FloatArray)
        fun onFrameRateChanged(fps: Double)
        fun onRenderRectChanged(renderRect: RectF)
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context) : super(context)

    private val renderer: OpenPSRenderer
    private val scaleGestureDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private val matrix = Matrix()   // 上层记录手势变换的矩阵（以图片默认适配为初始状态）
    private val imageMatrix = Matrix()  // 图片的缩放矩阵（以原图为初始状态）
    private val baseMatrix = Matrix()   // 图片默认适配状态时的缩放矩阵
    private var callback: Callback? = null
    val transformHelper by lazy {
        OpenGLTransformHelper()
    }

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        renderer = OpenPSRenderer(object : OpenPSRenderer.Callback {
            override fun onFrameRateChanged(fps: Double) {
                callback?.onFrameRateChanged(fps)
            }
        })
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
        }
        return true
    }

    fun initImageMatrix(matrix: Matrix) {
        imageMatrix.set(matrix)
        baseMatrix.set(matrix)
    }

    fun postOnGLThread(runnable: Runnable) {
        queueEvent(runnable)
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun resetTransform(bottomPadding: Float = 0f) {
        transformHelper.reset(bottomPadding)
        callback?.onGLMatrixChanged(transformHelper.getGLMatrix())
        callback?.onRenderRectChanged(transformHelper.getRenderRect())
        updateMatricesFromGL()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            transformHelper.postScale(detector.scaleFactor, detector.focusX, detector.focusY)
            callback?.onGLMatrixChanged(transformHelper.getGLMatrix())
            updateMatricesFromGL()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            transformHelper.postTranslate(-distanceX, -distanceY)
            callback?.onGLMatrixChanged(transformHelper.getGLMatrix())
            updateMatricesFromGL()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            resetTransform()
            return true
        }
    }

    private fun updateMatricesFromGL() {
        val glMatrix = transformHelper.getGLMatrix()
        val viewportWidth = transformHelper.viewportWidth
        val viewportHeight = transformHelper.viewportHeight

        // 创建 Android Matrix
        val androidMatrix = Matrix()

        // 1. 计算缩放分量（保持不变）
        val scaleX = glMatrix[0]
        val scaleY = glMatrix[5]

        // 2. 修改平移分量的计算
        // GL 坐标是从屏幕中心开始的，需要考虑图片初始位置
        val centerX = viewportWidth / 2f
        val centerY = viewportHeight / 2f

        val translateX = glMatrix[12] * centerX
        val translateY = -glMatrix[13] * centerY

        // 3. 设置变换
        androidMatrix.setScale(scaleX, scaleY, centerX, centerY)  // 围绕中心点缩放
        androidMatrix.postTranslate(translateX, translateY)

        // 更新 matrix
        matrix.set(androidMatrix)
        callback?.onMatrixChanged(matrix)

        // 更新 imageMatrix
        imageMatrix.set(baseMatrix)
        imageMatrix.postConcat(androidMatrix)
        callback?.onImageMatrixChanged(imageMatrix)
    }

    fun getImageMatrix() = Matrix(imageMatrix)
}