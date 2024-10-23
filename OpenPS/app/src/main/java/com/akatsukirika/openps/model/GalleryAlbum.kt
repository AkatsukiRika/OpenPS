package com.akatsukirika.openps.model

data class GalleryAlbum(
    val albumName: String = "",
    val images: List<GalleryImage> = emptyList()
)
