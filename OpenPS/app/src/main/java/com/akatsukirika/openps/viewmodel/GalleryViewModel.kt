package com.akatsukirika.openps.viewmodel

import android.content.Context
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

    fun init(context: Context) {
        viewModelScope.launch {
            updateAlbumList(context)
        }
    }

    fun updateSelectedAlbum(context: Context, album: GalleryAlbum) {
        _selectedAlbum.value = album
        viewModelScope.launch {
            loadThumbnails(context, album)
        }
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

    private suspend fun loadThumbnails(context: Context, album: GalleryAlbum) = withContext(Dispatchers.IO) {
        val newImages = mutableListOf<GalleryImage>()
        album.images.forEach { image ->
            GalleryUtils.getThumbnail(context, image.uri, THUMBNAIL_SIZE, THUMBNAIL_SIZE, onSuccess = { bitmap ->
                newImages.add(image.copy(thumbnail = bitmap))
                if (newImages.size == album.images.size) {
                    val newAlbum = GalleryAlbum(album.albumName, newImages)
                    _albumList.value = _albumList.value.map {
                        if (it.albumName == album.albumName) newAlbum else it
                    }
                    _selectedAlbum.value = newAlbum
                }
            }, onError = {})
        }
    }
}