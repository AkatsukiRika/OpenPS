package com.akatsukirika.openps.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import com.akatsukirika.openps.model.GalleryImage
import com.akatsukirika.openps.model.ImageFormat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

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

    fun getImagesFromAlbum(context: Context, albumName: String): List<GalleryImage> {
        val images = mutableListOf<GalleryImage>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
        )
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(albumName)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )

                var width = 0
                var height = 0
                var format = ImageFormat.UNKNOWN
                runCatching {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(contentUri)?.use { input ->
                        BitmapFactory.decodeStream(input, null, options)
                    }
                    width = options.outWidth
                    height = options.outHeight
                    format = when (options.outMimeType) {
                        "image/jpeg" -> ImageFormat.JPEG
                        "image/png" -> ImageFormat.PNG
                        "image/gif" -> ImageFormat.GIF
                        "image/webp" -> ImageFormat.WEBP
                        "image/heic", "image/heif" -> ImageFormat.HEIC
                        "image/bmp" -> ImageFormat.BMP
                        else -> ImageFormat.UNKNOWN
                    }
                }.onFailure {
                    it.printStackTrace()
                }

                images.add(GalleryImage(name, contentUri, dateAdded, size, width, height, format))
            }
        }

        return images
    }

    fun getThumbnail(context: Context, uri: Uri, width: Int, height: Int, onSuccess: (Bitmap) -> Unit, onError: () -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .override(width, height)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onSuccess(resource)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    onError()
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }
}