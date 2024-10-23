package com.akatsukirika.openps.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akatsukirika.openps.model.GalleryAlbum
import com.akatsukirika.openps.model.GalleryImage
import com.akatsukirika.openps.utils.GalleryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryViewModel : ViewModel() {
    companion object {
        const val THUMBNAIL_SIZE = 200
    }

    private val _albumList = MutableStateFlow<List<GalleryAlbum>>(emptyList())
    val albumList: StateFlow<List<GalleryAlbum>> = _albumList

    private val _selectedAlbum = MutableStateFlow<GalleryAlbum?>(null)
    val selectedAlbum: StateFlow<GalleryAlbum?> = _selectedAlbum

    private val _previewImage = MutableStateFlow<GalleryImage?>(null)
    val previewImage: StateFlow<GalleryImage?> = _previewImage

    private var selectImageCallback: ((Uri) -> Unit)? = null

    fun init(context: Context, selectImageCallback: ((Uri) -> Unit)? = null) {
        this.selectImageCallback = selectImageCallback
        viewModelScope.launch {
            updateAlbumList(context)
        }
    }

    fun updateSelectedAlbum(context: Context, album: GalleryAlbum) {
        _selectedAlbum.value = album
    }

    fun selectImage(uri: Uri) {
        selectImageCallback?.invoke(uri)
    }

    fun updatePreviewImage(image: GalleryImage?) {
        _previewImage.value = image
    }

    private suspend fun updateAlbumList(context: Context) = withContext(Dispatchers.IO) {
        val albumNames = GalleryUtils.getAlbumNames(context)
        _albumList.value = albumNames.map { GalleryAlbum(albumName = it) }
        if (albumList.value.isNotEmpty()) {
            updateImages(context)
            updateSelectedAlbum(context, albumList.value.first())
        }
    }

    private suspend fun updateImages(context: Context) = withContext(Dispatchers.IO) {
        val albumList = _albumList.value
        val newAlbumList = mutableListOf<GalleryAlbum>()
        albumList.forEach {
            val images = GalleryUtils.getImagesFromAlbum(context, it.albumName)
            newAlbumList.add(GalleryAlbum(it.albumName, images))
        }
        _albumList.value = newAlbumList
    }
}