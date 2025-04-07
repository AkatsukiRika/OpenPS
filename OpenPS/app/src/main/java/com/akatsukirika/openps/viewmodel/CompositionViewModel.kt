package com.akatsukirika.openps.viewmodel

import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akatsukirika.openps.compose.CompositionTab
import com.akatsukirika.openps.compose.CropOptions
import com.akatsukirika.openps.enum.RotateAction
import com.akatsukirika.openps.utils.BitmapUtils
import com.akatsukirika.openps.utils.BitmapUtils.scaleToEven
import com.pixpark.gpupixel.OpenPSHelper
import com.pixpark.gpupixel.model.RenderViewInfo
import kotlinx.coroutines.Dispatchers
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

    val rotationDegrees = MutableStateFlow(0)

    val canSave = MutableStateFlow(false)

    // 底部编辑区的高度（不包含Undo/Redo区域）
    val bottomScreenHeight = MutableStateFlow(0f)

    // 图片渲染区域
    val renderRect = MutableStateFlow(RectF())

    // 裁剪出来的区域
    val croppedRect = MutableStateFlow(RectF())

    // 裁剪出来的区域（归一化）
    val croppedRectF = MutableStateFlow(RectF(0f, 0f, 1f, 1f))

    // 上次裁剪的区域（归一化）
    private var lastCroppedRectF: RectF = RectF(0f, 0f, 1f, 1f)

    val saveEvent = MutableSharedFlow<SaveEvent>(replay = 0)

    val resultEvent = MutableSharedFlow<ResultEvent>(replay = 0)

    var renderViewInfo: RenderViewInfo? = null

    private var helper: OpenPSHelper? = null

    private var originalBitmap: Bitmap? = null

    private var skinMaskBitmap: Bitmap? = null

    private var initialMirrorState: Boolean = false

    private var initialFlipState: Boolean = false

    private var initialRotationDegrees: Int = 0

    private var isSave = false

    // 按顺序记录整个房间内的所有旋转操作
    private val rotateActionList = mutableListOf<RotateAction>()

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
                combine(isRectChanged, isMirrored, isFlipped, rotationDegrees) { flow1, flow2, flow3, flow4 ->
                    canSave.value = flow1 || (flow2 != initialMirrorState) || (flow3 != initialFlipState) || (flow4 != initialRotationDegrees)
                }.collect {}
            }

            launch {
                saveEvent.collect {
                    save()
                }
            }
        }
    }

    fun initStates(
        helper: OpenPSHelper? = null,
        originalBitmap: Bitmap? = null,
        skinMaskBitmap: Bitmap? = null,
        mirrorState: Boolean,
        flipState: Boolean,
        rotation: Int
    ) {
        currentTab.value = CompositionTab.CROP
        currentCropOptions.value = CropOptions.CUSTOM
        isRectChanged.value = false
        isMirrored.value = mirrorState
        initialMirrorState = mirrorState
        isFlipped.value = flipState
        initialFlipState = flipState
        rotationDegrees.value = rotation
        initialRotationDegrees = rotation
        canSave.value = false
        isSave = false
        this.helper = helper
        this.originalBitmap = originalBitmap
        this.skinMaskBitmap = skinMaskBitmap
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
        lastCroppedRectF = croppedRectF.value
        croppedRectF.value = RectF(
            lastCroppedRectF.left + newLeft * (lastCroppedRectF.right - lastCroppedRectF.left),
            lastCroppedRectF.top + newTop * (lastCroppedRectF.bottom - lastCroppedRectF.top),
            lastCroppedRectF.left + newRight * (lastCroppedRectF.right - lastCroppedRectF.left),
            lastCroppedRectF.top + newBottom * (lastCroppedRectF.bottom - lastCroppedRectF.top)
        )
        originalBitmap?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val beginTime = System.currentTimeMillis()
                var currentBitmap = it
                var currentSkinMask = skinMaskBitmap
                rotateActionList.forEach { action ->
                    currentBitmap = when (action) {
                        RotateAction.ROTATE_LEFT -> BitmapUtils.rotateBitmap(currentBitmap, -90f)
                        RotateAction.ROTATE_RIGHT -> BitmapUtils.rotateBitmap(currentBitmap, 90f)
                        RotateAction.MIRROR -> BitmapUtils.mirrorBitmap(currentBitmap)
                        RotateAction.FLIP -> BitmapUtils.flipBitmap(currentBitmap)
                    }
                    if (currentSkinMask != null) {
                        currentSkinMask = when (action) {
                            RotateAction.ROTATE_LEFT -> BitmapUtils.rotateBitmap(currentSkinMask!!, -90f)
                            RotateAction.ROTATE_RIGHT -> BitmapUtils.rotateBitmap(currentSkinMask!!, 90f)
                            RotateAction.MIRROR -> BitmapUtils.mirrorBitmap(currentSkinMask!!)
                            RotateAction.FLIP -> BitmapUtils.flipBitmap(currentSkinMask!!)
                        }
                    }
                }
                currentBitmap = BitmapUtils.cropBitmap(currentBitmap, newLeft, newTop, newRight, newBottom).scaleToEven()
                if (currentSkinMask != null) {
                    currentSkinMask = BitmapUtils.cropBitmap(currentSkinMask!!, newLeft, newTop, newRight, newBottom).scaleToEven()
                }
                resultEvent.emit(ResultEvent(currentBitmap, currentSkinMask))
                Log.d("CompositionViewModel", "Bitmap处理耗时: ${System.currentTimeMillis() - beginTime}ms")
            }
        }
    }

    fun rotateLeft() {
        val newRotationDegrees = (rotationDegrees.value + 90) % 360
        rotationDegrees.value = newRotationDegrees
        rotateActionList.add(RotateAction.ROTATE_LEFT)
    }

    fun rotateRight() {
        val newRotationDegrees = (rotationDegrees.value - 90) % 360
        rotationDegrees.value = newRotationDegrees
        rotateActionList.add(RotateAction.ROTATE_RIGHT)
    }

    fun mirror() {
        isMirrored.value = !isMirrored.value
        rotateActionList.add(RotateAction.MIRROR)
    }

    fun flip() {
        isFlipped.value = !isFlipped.value
        rotateActionList.add(RotateAction.FLIP)
    }

    fun restoreTransformStates() {
        if (!isSave) {
            isMirrored.value = initialMirrorState
            isFlipped.value = initialFlipState
            rotationDegrees.value = initialRotationDegrees
        }
        rotateActionList.clear()
    }
}

data object SaveEvent

data class ResultEvent(
    val bitmap: Bitmap,
    val skinMaskBitmap: Bitmap? = null,
)