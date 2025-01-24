package com.akatsukirika.openps.viewmodel

import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.compose.CompositionTab
import com.akatsukirika.openps.compose.CropOptions
import com.pixpark.gpupixel.model.RenderViewInfo
import kotlinx.coroutines.flow.MutableStateFlow

class CompositionViewModel : ViewModel() {
    val currentTab = MutableStateFlow(CompositionTab.CROP)

    val currentCropOptions = MutableStateFlow(CropOptions.CUSTOM)

    val canSave = MutableStateFlow(false)

    var renderViewInfo: RenderViewInfo? = null

    fun initStates() {
        currentTab.value = CompositionTab.CROP
        currentCropOptions.value = CropOptions.CUSTOM
        canSave.value = false
    }
}