package com.pixpark.gpupixel

import android.opengl.Matrix
import com.pixpark.gpupixel.model.RenderViewInfo

class OpenGLTransformHelper {
    var viewportWidth = 0f
        private set
    var viewportHeight = 0f
        private set
    private val glMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private var currentScale = 1f
    private var renderViewInfo: RenderViewInfo? = null
    private var bottomPadding: Float = 0f

    init {
        Matrix.setIdentityM(glMatrix, 0)
    }

    fun setRenderViewInfo(info: RenderViewInfo) {
        renderViewInfo = info
        setViewportSize(info.viewWidth, info.viewHeight)
    }

    private fun setViewportSize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
    }

    fun postScale(scale: Float, focusX: Float, focusY: Float) {
        val newScale = currentScale * scale
        val effectiveScale = newScale / currentScale
        currentScale = newScale

        val glFocusX = (focusX - viewportWidth / 2f) / (viewportWidth / 2f)
        val glFocusY = (viewportHeight / 2f - focusY) / (viewportHeight / 2f)

        Matrix.setIdentityM(tempMatrix, 0)
        Matrix.translateM(tempMatrix, 0, glFocusX, glFocusY, 0f)
        Matrix.scaleM(tempMatrix, 0, effectiveScale, effectiveScale, 1f)
        Matrix.translateM(tempMatrix, 0, -glFocusX, -glFocusY, 0f)

        Matrix.multiplyMM(glMatrix, 0, tempMatrix, 0, glMatrix, 0)
    }

    fun postTranslate(dx: Float, dy: Float) {
        val glDx = (dx / viewportWidth) * 2f / currentScale
        val glDy = (-dy / viewportHeight) * 2f / currentScale

        Matrix.translateM(glMatrix, 0, glDx, glDy, 0f)
    }

    fun reset(bottomPadding: Float) {
        Matrix.setIdentityM(glMatrix, 0)
        currentScale = 1f

        this.bottomPadding = bottomPadding
        renderViewInfo?.let {
            val top = it.viewHeight * (1 - it.scaledHeight) / 2
            if (top >= bottomPadding / 2) {
                postTranslate(0f, -bottomPadding / 2)
            } else {
            }
        }
    }

    fun getGLMatrix() = glMatrix
}