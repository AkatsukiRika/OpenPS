package com.akatsukirika.openps.model

import android.graphics.Bitmap
import android.net.Uri

data class GalleryImage(
    val name: String,
    val uri: Uri,
    val dateAdded: Long,
    val size: Long,
    val thumbnail: Bitmap? = null
)
