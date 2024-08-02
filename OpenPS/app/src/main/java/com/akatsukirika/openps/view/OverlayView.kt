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
    private var imageMatrix: Matrix? = null
    private val normalizedBox = RectF()
    private val actualBox = RectF()
    private var imageWidth = 0
    private var imageHeight = 0

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

    fun setData(matrix: Matrix, box: RectF, width: Int, height: Int) {
        imageMatrix = matrix
        normalizedBox.set(box)
        imageWidth = width
        imageHeight = height
        updateBoxes()
    }

    private fun updateBoxes() {
        val matrix = imageMatrix ?: return
        val box = normalizedBox
        val values = FloatArray(9)
        matrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]
        actualBox.set(RectF(
            box.left * imageWidth * scaleX + transX,
            box.top * imageHeight * scaleY + transY,
            box.right * imageWidth * scaleX + transX,
            box.bottom * imageHeight * scaleY + transY
        ))
        invalidate()
    }
}