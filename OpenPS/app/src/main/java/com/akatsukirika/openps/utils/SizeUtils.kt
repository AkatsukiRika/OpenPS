package com.akatsukirika.openps.utils

import android.content.Context

object SizeUtils {
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun spToPx(context: Context, sp: Float): Float {
        return sp * context.resources.displayMetrics.scaledDensity
    }
}