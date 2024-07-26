package com.akatsukirika.openps.utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BitmapUtils {
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