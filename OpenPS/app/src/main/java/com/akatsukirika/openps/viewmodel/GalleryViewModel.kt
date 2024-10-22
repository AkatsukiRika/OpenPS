package com.akatsukirika.openps.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.model.GalleryAlbum
import com.akatsukirika.openps.utils.GalleryUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GalleryViewModel : ViewModel() {
    private val _albumList = MutableStateFlow<List<GalleryAlbum>>(emptyList())
    val albumList: StateFlow<List<GalleryAlbum>> = _albumList

    private val _selectedAlbum = MutableStateFlow<GalleryAlbum?>(null)
    val selectedAlbum: StateFlow<GalleryAlbum?> = _selectedAlbum

    fun init(context: Context) {
        updateAlbumList(context)
    }

    fun updateSelectedAlbum(album: GalleryAlbum) {
        _selectedAlbum.value = album
    }

    private fun updateAlbumList(context: Context) {
        val albumNames = GalleryUtils.getAlbumNames(context)
        _albumList.value = albumNames.map { GalleryAlbum(albumName = it) }
        if (albumList.value.isNotEmpty()) {
            updateSelectedAlbum(albumList.value.first())
        }
    }
}