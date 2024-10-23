package com.akatsukirika.openps.model

import android.net.Uri

data class GalleryImage(
    val name: String,
    val uri: Uri,
    val dateAdded: Long,
    val size: Long,
    val width: Int,
    val height: Int
) {
    fun getHumanizedSize(): String {
        val kb = size / 1024
        return if (kb < 1024) {
            "$kb KB"
        } else {
            val mb = kb / 1024
            "$mb MB"
        }
    }
}
