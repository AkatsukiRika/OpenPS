package com.akatsukirika.shadertest

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class MyGLSurfaceView(context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    private val renderer: MyGLRenderer
    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer(context)
        setRenderer(renderer)
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
        }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f)
            queueEvent {
                renderer.setScaleFactor(scaleFactor)
            }
            return true
        }
    }
}