package com.pixpark.gpupixel.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
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

    private fun postScale(scale: Float, focusX: Float, focusY: Float) {
        matrix.postScale(scale, scale, focusX, focusY)
        imageMatrix.postScale(scale, scale, focusX, focusY)
        callback?.onMatrixChanged(matrix)
        callback?.onImageMatrixChanged(imageMatrix)
    }

    private fun postTranslate(dx: Float, dy: Float) {
        matrix.postTranslate(dx, dy)
        imageMatrix.postTranslate(dx, dy)
        callback?.onMatrixChanged(matrix)
        callback?.onImageMatrixChanged(imageMatrix)
    }

    private fun resetMatrix() {
        matrix.reset()
        imageMatrix.set(baseMatrix)
        callback?.onMatrixChanged(matrix)
        callback?.onImageMatrixChanged(imageMatrix)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            postScale(detector.scaleFactor, detector.focusX, detector.focusY)
            transformHelper.postScale(detector.scaleFactor, detector.focusX, detector.focusY)
            callback?.onGLMatrixChanged(transformHelper.getGLMatrix())
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            postTranslate(-distanceX, -distanceY)
            transformHelper.postTranslate(-distanceX, -distanceY)
            callback?.onGLMatrixChanged(transformHelper.getGLMatrix())
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            resetMatrix()
            transformHelper.reset()
            callback?.onGLMatrixChanged(transformHelper.getGLMatrix())
            return true
        }
    }

    fun getImageMatrix() = Matrix(imageMatrix)
}