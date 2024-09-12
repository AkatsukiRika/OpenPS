package com.pixpark.gpupixel.view

import android.opengl.GLSurfaceView
import com.pixpark.gpupixel.OpenPS
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class OpenPSRenderer(private val callback: Callback) : GLSurfaceView.Renderer {
    interface Callback {
        fun onFrameRateChanged(fps: Double)
    }

    private var lastFrameTime = 0L
    private var frameCount = 0L

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        OpenPS.nativeInit()
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        OpenPS.nativeTargetViewSizeChanged(p1, p2)
    }

    override fun onDrawFrame(p0: GL10?) {
        val currentTime = System.currentTimeMillis()
        if (lastFrameTime > 0) {
            frameCount++
            val deltaTime = currentTime - lastFrameTime
            if (deltaTime >= 1000) {
                val fps = frameCount * 1000.0 / deltaTime
                callback.onFrameRateChanged(fps)
                frameCount = 0
                lastFrameTime = currentTime
            }
        } else {
            lastFrameTime = currentTime
        }
        OpenPS.nativeRequestRender()
    }
}