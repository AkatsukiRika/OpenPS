package com.pixpark.gpupixel

import android.graphics.RectF
import android.opengl.Matrix
import androidx.core.graphics.transform
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
    private val initialRenderRect = RectF()
    private val renderRect = RectF()
    var isMirrored = false
        private set
    var isFlipped = false
        private set

    init {
        Matrix.setIdentityM(glMatrix, 0)
    }

    fun setRenderViewInfo(info: RenderViewInfo) {
        renderViewInfo = info
        setViewportSize(info.viewWidth, info.viewHeight)
        initialRenderRect.set(
            ((1 - info.scaledWidth) / 2) * info.viewWidth,
            ((1 - info.scaledHeight) / 2) * info.viewHeight,
            ((1 + info.scaledWidth) / 2) * info.viewWidth,
            ((1 + info.scaledHeight) / 2) * info.viewHeight
        )
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

    fun mirror(newState: Boolean) {
        if (newState != isMirrored) {
            postScaleNonUniform(-1f, 1f)
        }
        isMirrored = newState
    }

    fun flip(newState: Boolean) {
        if (newState != isFlipped) {
            postScaleNonUniform(1f, -1f)
        }
        isFlipped = newState
    }

    private fun postScaleNonUniform(scaleX: Float, scaleY: Float) {
        // 使用 renderRect 的中心作为旋转/缩放的焦点
        val centerX = renderRect.centerX()
        val centerY = renderRect.centerY()

        // 将焦点从屏幕坐标转换到 OpenGL 坐标系 [-1, 1] 范围内
        val glFocusX = (centerX - viewportWidth / 2f) / (viewportWidth / 2f)
        val glFocusY = (viewportHeight / 2f - centerY) / (viewportHeight / 2f)

        Matrix.setIdentityM(tempMatrix, 0)
        Matrix.translateM(tempMatrix, 0, glFocusX, glFocusY, 0f)
        Matrix.scaleM(tempMatrix, 0, scaleX, scaleY, 1f)
        Matrix.translateM(tempMatrix, 0, -glFocusX, -glFocusY, 0f)

        Matrix.multiplyMM(glMatrix, 0, tempMatrix, 0, glMatrix, 0)
    }

    fun postTranslate(dx: Float, dy: Float, fromUser: Boolean = true) {
        val glDx = (dx / viewportWidth) * 2f / currentScale
        val glDy = (-dy / viewportHeight) * 2f / currentScale

        if (fromUser) {
            Matrix.translateM(glMatrix, 0, if (isMirrored) -glDx else glDx, if (isFlipped) -glDy else glDy, 0f)
        } else {
            Matrix.translateM(glMatrix, 0, glDx, glDy, 0f)
        }
    }

    fun reset(bottomPadding: Float) {
        Matrix.setIdentityM(glMatrix, 0)
        currentScale = 1f

        this.bottomPadding = bottomPadding
        renderViewInfo?.let {
            val top = it.viewHeight * (1 - it.scaledHeight) / 2
            val rectTransformMatrix = android.graphics.Matrix()
            renderRect.set(initialRenderRect)
            if (top >= bottomPadding / 2) {
                postTranslate(0f, -bottomPadding / 2, fromUser = false)
                rectTransformMatrix.postTranslate(0f, -bottomPadding / 2)
                renderRect.transform(rectTransformMatrix)
            } else {
                val oldRenderHeight = it.viewHeight * it.scaledHeight
                val newRenderHeight = it.viewHeight - bottomPadding
                val scale = newRenderHeight / oldRenderHeight
                postScale(scale, it.viewWidth / 2, it.viewHeight / 2)
                postTranslate(0f, -bottomPadding / 2, fromUser = false)
                rectTransformMatrix.postScale(scale, scale, it.viewWidth / 2, it.viewHeight / 2)
                rectTransformMatrix.postTranslate(0f, -bottomPadding / 2)
                renderRect.transform(rectTransformMatrix)
            }
            if (isMirrored) {
                postScaleNonUniform(-1f, 1f)
            }
            if (isFlipped) {
                postScaleNonUniform(1f, -1f)
            }
        }
    }

    fun getGLMatrix() = glMatrix

    fun getRenderRect() = renderRect
}