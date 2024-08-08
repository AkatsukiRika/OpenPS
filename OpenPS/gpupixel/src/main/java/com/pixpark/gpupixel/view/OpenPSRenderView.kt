package com.pixpark.gpupixel.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class OpenPSRenderView : GLSurfaceView {
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context) : super(context)

    private val renderer: OpenPSRenderer

    init {
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
        renderer = OpenPSRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun postOnGLThread(runnable: Runnable) {
        queueEvent(runnable)
    }
}