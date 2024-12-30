package com.pixpark.gpupixel.utils

import android.graphics.Bitmap
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

    fun saveBitmapToFile(bitmap: Bitmap, directory: String, filename: String) {
        val file = File(directory, filename)
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }
    }
}