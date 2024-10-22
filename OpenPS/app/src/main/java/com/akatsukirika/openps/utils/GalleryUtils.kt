package com.akatsukirika.openps.utils

import android.content.Context
import android.provider.MediaStore

object GalleryUtils {
    fun getAlbumNames(context: Context): List<String> {
        val albumNames = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID)
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        )?.use { cursor ->
            val bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val albumName = cursor.getString(bucketNameColumn)
                if (!albumNames.contains(albumName)) {
                    albumNames.add(albumName)
                }
            }
        }
        return albumNames
    }
}