package com.akatsukirika.openps.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class EvenDimensionsTransformation : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val newWidth = if (toTransform.width % 2 == 0) toTransform.width else toTransform.width - 1
        val newHeight = if (toTransform.height % 2 == 0) toTransform.height else toTransform.height - 1

        return if (newWidth == toTransform.width && newHeight == toTransform.height) {
            toTransform
        } else {
            Bitmap.createBitmap(toTransform, 0, 0, newWidth, newHeight)
        }
    }
}