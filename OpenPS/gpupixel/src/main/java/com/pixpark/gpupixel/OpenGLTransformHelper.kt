package com.pixpark.gpupixel

import android.opengl.Matrix

class OpenGLTransformHelper {
    var viewportWidth = 0f
        private set
    var viewportHeight = 0f
        private set
    private val glMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    private var currentScale = 1f

    init {
        Matrix.setIdentityM(glMatrix, 0)
    }

    fun setViewportSize(width: Float, height: Float) {
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

    fun reset() {
        Matrix.setIdentityM(glMatrix, 0)
        currentScale = 1f
    }

    fun getGLMatrix() = glMatrix
}