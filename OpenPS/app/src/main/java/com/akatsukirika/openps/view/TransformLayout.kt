package com.akatsukirika.openps.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.widget.FrameLayout

class TransformLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val matrix = Matrix()

    fun setTransform(matrix: Matrix) {
        this.matrix.set(matrix)
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        super.dispatchDraw(canvas)
        canvas.restore()
    }
}