package com.akatsukirika.openps.utils

import android.graphics.Matrix
import com.akatsukirika.openps.model.MatrixParams

object MatrixUtils {
    fun Matrix.getParams(): MatrixParams {
        val values = FloatArray(9)
        getValues(values)
        return MatrixParams(
            values[Matrix.MSCALE_X],
            values[Matrix.MSCALE_Y],
            values[Matrix.MTRANS_X],
            values[Matrix.MTRANS_Y]
        )
    }

    fun Matrix.getScaleX(): Float {
        val values = FloatArray(9)
        getValues(values)
        return values[Matrix.MSCALE_X]
    }
}