package com.pixpark.gpupixel.utils

import android.graphics.Bitmap
import android.os.Build

object BitmapUtils {
    fun getChannels(bitmap: Bitmap): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return when (bitmap.config) {
                Bitmap.Config.ALPHA_8 -> 1
                Bitmap.Config.RGB_565 -> 3
                Bitmap.Config.ARGB_4444 -> 4
                Bitmap.Config.ARGB_8888 -> 4
                Bitmap.Config.RGBA_F16 -> 4
                else -> throw IllegalArgumentException("Unsupported bitmap configuration")
            }
        } else {
            return when (bitmap.config) {
                Bitmap.Config.ALPHA_8 -> 1
                Bitmap.Config.RGB_565 -> 3
                Bitmap.Config.ARGB_4444 -> 4
                Bitmap.Config.ARGB_8888 -> 4
                else -> throw IllegalArgumentException("Unsupported bitmap configuration")
            }
        }
    }
}