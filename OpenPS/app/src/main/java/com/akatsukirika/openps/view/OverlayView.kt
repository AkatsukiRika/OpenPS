package com.akatsukirika.openps.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.akatsukirika.openps.R

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val actualBox = RectF()
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var scaledWidth = 0f
    private var scaledHeight = 0f
    private var faceRect = RectF()

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.overlay_rect_stroke_width).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!actualBox.isEmpty) {
            canvas.drawRect(actualBox, paint)
        }
    }

    fun setData(viewWidth: Float, viewHeight: Float, scaledWidth: Float, scaledHeight: Float, faceRect: RectF) {
        this.viewWidth = viewWidth
        this.viewHeight = viewHeight
        this.scaledWidth = scaledWidth
        this.scaledHeight = scaledHeight
        this.faceRect = faceRect

        val actualTop = (viewHeight * (1 - scaledHeight)) / 2 + viewHeight * scaledHeight * faceRect.top
        val actualLeft = (viewWidth * (1 - scaledWidth)) / 2 + viewWidth * scaledWidth * faceRect.left
        val actualRight = (viewWidth * (1 - scaledWidth)) / 2 + viewWidth * scaledWidth * faceRect.right
        val actualBottom = (viewHeight * (1 - scaledHeight)) / 2 + viewHeight * scaledHeight * faceRect.bottom

        actualBox.set(actualLeft, actualTop, actualRight, actualBottom)
        invalidate()
    }
}