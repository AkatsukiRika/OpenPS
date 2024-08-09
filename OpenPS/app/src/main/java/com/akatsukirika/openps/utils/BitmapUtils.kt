package com.akatsukirika.openps.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): Uri? {
        val contentResolver = context.contentResolver

        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val imageUri = contentResolver.insert(imageCollection, contentValues)

        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw IOException("Failed to save bitmap.")
                }
            } ?: throw IOException("Failed to open output stream.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
        }

        return imageUri
    }

    fun cropBitmap(bitmap: Bitmap, left: Float, top: Float, right: Float, bottom: Float): Bitmap {
        if (left < 0f || top < 0f || right > 1f || bottom > 1f || left >= right || top >= bottom) {
            throw IllegalArgumentException("Invalid crop parameters")
        }

        val x = (left * bitmap.width).toInt()
        val y = (top * bitmap.height).toInt()
        val width = ((right - left) * bitmap.width).toInt()
        val height = ((bottom - top) * bitmap.height).toInt()

        return Bitmap.createBitmap(bitmap, x, y, width, height)
    }

    fun mergeBitmap(original: Bitmap, cropped: Bitmap, left: Float, top: Float, right: Float, bottom: Float): Bitmap {
        if (left < 0f || top < 0f || right > 1f || bottom > 1f || left >= right || top >= bottom) {
            throw IllegalArgumentException("Invalid merge parameters")
        }

        val resultBitmap = Bitmap.createBitmap(original.width, original.height, original.config)
        val canvas = Canvas(resultBitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        canvas.drawRect(0f, 0f, original.width.toFloat(), original.height.toFloat(), paint)

        val x = (left * original.width).toInt()
        val y = (top * original.height).toInt()
        val width = ((right - left) * original.width).toInt()
        val height = ((bottom - top) * original.height).toInt()

        canvas.drawBitmap(cropped, null, Rect(x, y, x + width, y + height), null)
        return resultBitmap
    }
}