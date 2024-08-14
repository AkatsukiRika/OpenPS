package com.pixpark.gpupixel.view

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.pixpark.gpupixel.OpenPS

class OpenPSRenderView : GLSurfaceView {
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context) : super(context)

    private val renderer: OpenPSRenderer
    private val scaleGestureDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        renderer = OpenPSRenderer()
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

    fun postOnGLThread(runnable: Runnable) {
        queueEvent(runnable)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            postOnGLThread {
                OpenPS.nativeSetScaleFactor(scale)
                requestRender()
            }
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            postOnGLThread {
                OpenPS.nativeSetTranslateDistance(distanceX, distanceY)
                requestRender()
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            postOnGLThread {
                OpenPS.nativeResetMVPMatrix()
                requestRender()
            }
            return true
        }
    }
}