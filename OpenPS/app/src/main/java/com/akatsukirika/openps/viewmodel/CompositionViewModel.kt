package com.akatsukirika.openps.viewmodel

import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akatsukirika.openps.compose.CompositionTab
import com.akatsukirika.openps.compose.CropOptions
import com.pixpark.gpupixel.model.RenderViewInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CompositionViewModel : ViewModel() {
    val currentTab = MutableStateFlow(CompositionTab.CROP)

    val currentCropOptions = MutableStateFlow(CropOptions.CUSTOM)

    val isRectChanged = MutableStateFlow(false)

    val isMirrored = MutableStateFlow(false)

    val isFlipped = MutableStateFlow(false)

    val canSave = MutableStateFlow(false)

    // 底部编辑区的高度（不包含Undo/Redo区域）
    val bottomScreenHeight = MutableStateFlow(0f)

    // 图片渲染区域
    val renderRect = MutableStateFlow(RectF())

    var renderViewInfo: RenderViewInfo? = null

    fun initStates() {
        currentTab.value = CompositionTab.CROP
        currentCropOptions.value = CropOptions.CUSTOM
        isRectChanged.value = false
        isMirrored.value = false
        isFlipped.value = false
        canSave.value = false
        viewModelScope.launch {
            combine(isRectChanged, isMirrored, isFlipped) { flow1, flow2, flow3 ->
                canSave.value = flow1 || flow2 || flow3
            }.collect {}
        }
    }

    fun getRatio(): Float {
        val renderRect = renderRect.value
        return when (currentCropOptions.value) {
            CropOptions.CUSTOM -> 0f
            CropOptions.ORIGINAL -> renderRect.width() / renderRect.height()
            else -> currentCropOptions.value.ratio
        }
    }
}