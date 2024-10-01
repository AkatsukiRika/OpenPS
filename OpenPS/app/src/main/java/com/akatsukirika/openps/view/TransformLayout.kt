package com.akatsukirika.openps.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import com.akatsukirika.openps.utils.MatrixUtils.getParams
import com.akatsukirika.openps.utils.SizeUtils

class TransformLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val matrix = Matrix()
    private var isDebug = false
    private val debugPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
        textSize = SizeUtils.spToPx(context, 16f)
    }

    fun setTransform(matrix: Matrix) {
        this.matrix.set(matrix)
        invalidate()
    }

    fun setDebug(debug: Boolean) {
        isDebug = debug
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        super.dispatchDraw(canvas)
        canvas.restore()

        if (isDebug) {
            drawDebugInfo(canvas)

        }
    }

    private fun drawDebugInfo(canvas: Canvas) {
        val params = matrix.getParams()
        val texts = listOf(
            "scaleX: ${params.scaleX}",
            "scaleY: ${params.scaleY}",
            "translateX: ${params.translateX}",
            "translateY: ${params.translateY}"
        )
        val paddingEnd = SizeUtils.dpToPx(context, 8f)
        val paddingTop = SizeUtils.dpToPx(context, 8f)
        val viewWidth = width.toFloat()
        var y = paddingTop + debugPaint.textSize
        for (text in texts) {
            val textWidth = debugPaint.measureText(text)
            val x = viewWidth - textWidth - paddingEnd
            canvas.drawText(text, x, y, debugPaint)
            y += debugPaint.textSize + SizeUtils.dpToPx(context, 4f)
        }
    }
}