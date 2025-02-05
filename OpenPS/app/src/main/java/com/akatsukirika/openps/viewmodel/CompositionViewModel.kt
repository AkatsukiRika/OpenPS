package com.akatsukirika.openps.viewmodel

import android.graphics.RectF
import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.compose.CompositionTab
import com.akatsukirika.openps.compose.CropOptions
import com.pixpark.gpupixel.model.RenderViewInfo
import kotlinx.coroutines.flow.MutableStateFlow

class CompositionViewModel : ViewModel() {
    val currentTab = MutableStateFlow(CompositionTab.CROP)

    val currentCropOptions = MutableStateFlow(CropOptions.CUSTOM)

    val canSave = MutableStateFlow(false)

    // 底部编辑区的高度（不包含Undo/Redo区域）
    val bottomScreenHeight = MutableStateFlow(0f)

    // 图片渲染区域
    val renderRect = MutableStateFlow(RectF())

    var renderViewInfo: RenderViewInfo? = null

    fun initStates() {
        currentTab.value = CompositionTab.CROP
        currentCropOptions.value = CropOptions.CUSTOM
        canSave.value = false
    }
}