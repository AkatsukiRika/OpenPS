package com.akatsukirika.openps.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.PreferredColorSpace
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.Downsampler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    /**
     * @param maxSize 长边像素数
     */
    fun Bitmap.scaleToMaxLongSide(maxSize: Int): Bitmap {
        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
    }

    fun Bitmap.isFullyTransparent(): Boolean {
        if (!hasAlpha()) {
            return false
        }
        val pixels = IntArray(width)
        for (y in 0 until height) {
            getPixels(pixels, 0, width, 0, y, width, 1)
            for (pixel in pixels) {
                val alpha = (pixel shr 24) and 0xFF
                if (alpha != 0) {
                    return false
                }
            }
        }
        return true
    }

    fun Bitmap.scaleToEven(): Bitmap {
        // 获取Bitmap的当前宽度和高度
        val bitmap = this
        val width = bitmap.width
        val height = bitmap.height

        // 计算目标宽度和高度
        val targetWidth = if (width % 2 == 0) width else width + 1
        val targetHeight = if (height % 2 == 0) height else height + 1

        // 如果宽度和高度都是偶数，直接返回原Bitmap
        if (targetWidth == width && targetHeight == height) {
            return bitmap
        }

        // 创建一个缩放矩阵
        val scaleWidth = targetWidth.toFloat() / width
        val scaleHeight = targetHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        // 创建一个新的Bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    suspend fun getBitmap(context: Context, model: Any?) = withContext(Dispatchers.IO) {
        try {
            Glide.with(context)
                .asBitmap()
                .load(model)
                .priority(Priority.IMMEDIATE)
                .set(Downsampler.PREFERRED_COLOR_SPACE, PreferredColorSpace.SRGB)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .submit()
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}