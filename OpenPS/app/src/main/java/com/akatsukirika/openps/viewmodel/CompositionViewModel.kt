package com.akatsukirika.openps.viewmodel

import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.compose.CompositionTab
import com.akatsukirika.openps.compose.CropOptions
import com.pixpark.gpupixel.model.RenderViewInfo
import kotlinx.coroutines.flow.MutableStateFlow

class CompositionViewModel : ViewModel() {
    val currentTab = MutableStateFlow(CompositionTab.CROP)

    val currentCropOptions = MutableStateFlow(CropOptions.CUSTOM)

    var renderViewInfo: RenderViewInfo? = null
}