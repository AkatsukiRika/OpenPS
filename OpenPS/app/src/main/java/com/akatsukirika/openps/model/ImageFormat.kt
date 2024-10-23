package com.akatsukirika.openps.model

import com.akatsukirika.openps.OpenPSApplication
import com.akatsukirika.openps.R

enum class ImageFormat(val displayName: String) {
    JPEG("JPEG"),
    PNG("PNG"),
    GIF("GIF"),
    WEBP("WEBP"),
    HEIC("HEIC"),
    BMP("BMP"),
    UNKNOWN(OpenPSApplication.instance?.getString(R.string.unknown) ?: "")
}