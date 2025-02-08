package com.akatsukirika.openps.viewmodel

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akatsukirika.openps.compose.CompositionTab
import com.akatsukirika.openps.compose.CropOptions
import com.akatsukirika.openps.utils.BitmapUtils
import com.akatsukirika.openps.utils.BitmapUtils.scaleToEven
import com.pixpark.gpupixel.model.RenderViewInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CompositionViewModel : ViewModel() {
    val currentTab = MutableStateFlow(CompositionTab.CROP)

    val currentCropOptions = MutableStateFlow(CropOptions.CUSTOM)

    private val isRectChanged = MutableStateFlow(false)

    val isMirrored = MutableStateFlow(false)

    val isFlipped = MutableStateFlow(false)

    val canSave = MutableStateFlow(false)

    // 底部编辑区的高度（不包含Undo/Redo区域）
    val bottomScreenHeight = MutableStateFlow(0f)

    // 图片渲染区域
    val renderRect = MutableStateFlow(RectF())

    // 裁剪出来的区域
    val croppedRect = MutableStateFlow(RectF())

    val saveEvent = MutableSharedFlow<SaveEvent>(replay = 0)

    val resultBitmap = MutableStateFlow<Bitmap?>(null)

    var renderViewInfo: RenderViewInfo? = null

    private var originalBitmap: Bitmap? = null

    private var initialMirrorState: Boolean = false

    private var initialFlipState: Boolean = false

    private var isSave = false

    init {
        viewModelScope.launch {
            launch {
                combine(renderRect, croppedRect) { initialRect, it ->
                    isRectChanged.value =
                        (it.left == initialRect.left &&
                                it.top == initialRect.top &&
                                it.right == initialRect.right &&
                                it.bottom == initialRect.bottom).not()
                }.collect {}
            }

            launch {
                combine(isRectChanged, isMirrored, isFlipped) { flow1, flow2, flow3 ->
                    canSave.value = flow1 || (flow2 != initialMirrorState) || (flow3 != initialFlipState)
                }.collect {}
            }

            launch {
                saveEvent.collect {
                    save()
                }
            }
        }
    }

    fun initStates(originalBitmap: Bitmap? = null, mirrorState: Boolean, flipState: Boolean) {
        currentTab.value = CompositionTab.CROP
        currentCropOptions.value = CropOptions.CUSTOM
        isRectChanged.value = false
        isMirrored.value = mirrorState
        initialMirrorState = mirrorState
        isFlipped.value = flipState
        initialFlipState = flipState
        canSave.value = false
        isSave = false
        this.originalBitmap = originalBitmap
        resultBitmap.value = null
    }

    fun getRatio(): Float {
        val renderRect = renderRect.value
        return when (currentCropOptions.value) {
            CropOptions.CUSTOM -> 0f
            CropOptions.ORIGINAL -> renderRect.width() / renderRect.height()
            else -> currentCropOptions.value.ratio
        }
    }

    fun save() {
        isSave = true
        val initialRect = renderRect.value
        val cropRect = croppedRect.value
        val newLeft = (cropRect.left - initialRect.left) / (initialRect.right - initialRect.left)
        val newTop = (cropRect.top - initialRect.top) / (initialRect.bottom - initialRect.top)
        val newRight = (cropRect.right - initialRect.left) / (initialRect.right - initialRect.left)
        val newBottom = (cropRect.bottom - initialRect.top) / (initialRect.bottom - initialRect.top)
        Log.d("CompositionViewModel", "newLeft: $newLeft, newTop: $newTop, newRight: $newRight, newBottom: $newBottom")
        originalBitmap?.let {
            val croppedBitmap = BitmapUtils.cropBitmap(it, newLeft, newTop, newRight, newBottom).scaleToEven()
            Log.d("CompositionViewModel", "croppedBitmap: ${croppedBitmap.width}, ${croppedBitmap.height}")
            resultBitmap.value = croppedBitmap
        }
    }

    fun restoreTransformStates() {
        if (!isSave) {
            isMirrored.value = initialMirrorState
            isFlipped.value = initialFlipState
        }
    }
}

data object SaveEvent