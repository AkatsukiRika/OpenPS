package com.pixpark.gpupixel.view

import android.opengl.GLSurfaceView
import com.pixpark.gpupixel.OpenPS
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class OpenPSRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        OpenPS.nativeInit()
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
        OpenPS.nativeTargetViewSizeChanged(p1, p2)
    }

    override fun onDrawFrame(p0: GL10?) {
        OpenPS.nativeRequestRender()
    }
}