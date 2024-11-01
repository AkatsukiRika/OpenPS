package com.akatsukirika.openps.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.STATUS_ERROR
import com.akatsukirika.openps.compose.STATUS_IDLE
import com.akatsukirika.openps.compose.STATUS_LOADING
import com.akatsukirika.openps.compose.STATUS_SUCCESS
import com.akatsukirika.openps.compose.TAB_ADJUST
import com.akatsukirika.openps.compose.TAB_BEAUTIFY
import com.akatsukirika.openps.compose.TAB_FILTER
import com.akatsukirika.openps.interop.NativeLib
import com.akatsukirika.openps.model.FunctionItem
import com.akatsukirika.openps.repo.INDEX_BLUSHER
import com.akatsukirika.openps.repo.INDEX_BRIGHTNESS
import com.akatsukirika.openps.repo.INDEX_CONTRAST
import com.akatsukirika.openps.repo.INDEX_EXPOSURE
import com.akatsukirika.openps.repo.INDEX_EYE_ZOOM
import com.akatsukirika.openps.repo.INDEX_FACE_SLIM
import com.akatsukirika.openps.repo.INDEX_LIPSTICK
import com.akatsukirika.openps.repo.INDEX_SATURATION
import com.akatsukirika.openps.repo.INDEX_SHARPEN
import com.akatsukirika.openps.repo.INDEX_SMOOTH
import com.akatsukirika.openps.repo.INDEX_WHITE
import com.akatsukirika.openps.repo.getAdjustFunctionList
import com.akatsukirika.openps.repo.getBeautifyFunctionList
import com.akatsukirika.openps.repo.getFilterList
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.BitmapUtils
import com.akatsukirika.openps.utils.EvenDimensionsTransformation
import com.akatsukirika.openps.utils.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pixpark.gpupixel.GPUPixel
import com.pixpark.gpupixel.OpenPSHelper
import com.pixpark.gpupixel.model.OpenPSRecord
import com.pixpark.gpupixel.model.RenderViewInfo
import com.pixpark.gpupixel.view.OpenPSRenderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class EditViewModel : ViewModel() {
    interface Callback {
        fun showOverlayView(info: RenderViewInfo, faceRectF: RectF)
        fun setDebugImage(bitmap: Bitmap)
        fun onRenderViewInfoReady(info: RenderViewInfo)
    }

    var helper: OpenPSHelper? = null
        private set

    private var callback: Callback? = null

    private var skinMaskBitmap: Bitmap? = null

    private val _loadStatus = MutableStateFlow(STATUS_IDLE)
    val loadStatus: StateFlow<Int> = _loadStatus

    private val beautifyLevelMap = MutableStateFlow(mapOf<Int, Float>())

    private val adjustLevelMap = MutableStateFlow(mapOf<Int, Float>())

    private val filterLevelMap = MutableStateFlow(mapOf<Int, Float>())

    private val _currentLevel = MutableStateFlow(0f)
    val currentLevel: StateFlow<Float> = _currentLevel

    private val _selectedFunctionIndex = MutableStateFlow(-1)
    val selectedFunctionIndex: StateFlow<Int> = _selectedFunctionIndex

    private val _selectedFilterIndex = MutableStateFlow(-1)
    val selectedFilterIndex: StateFlow<Int> = _selectedFilterIndex

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo

    private val lastSelectedTabIndex = MutableStateFlow(0)

    private val _itemList = MutableStateFlow(listOf<FunctionItem>())
    val itemList: StateFlow<List<FunctionItem>> = _itemList

    val showFrameRate = MutableStateFlow(false)

    val showMatrixInfo = MutableStateFlow(false)

    val uiFrameRate = MutableStateFlow(0.0)

    val glFrameRate = MutableStateFlow(0.0)

    // Face Rect
    private var faceRectLeft: Float = 0f
    private var faceRectTop: Float = 0f
    private var faceRectRight: Float = 0f
    private var faceRectBottom: Float = 0f
    private val faceRectExpandRatio: Float = 0.5f

    fun init(renderView: OpenPSRenderView, callback: Callback? = null) {
        helper = OpenPSHelper(renderView)
        this.callback = callback
        viewModelScope.launch {
            val context = renderView.context
            selectedTabIndex.collect {
                when (selectedTabIndex.value) {
                    TAB_BEAUTIFY -> {
                        _itemList.value = getBeautifyFunctionList(context)
                    }

                    TAB_ADJUST -> {
                        _itemList.value = getAdjustFunctionList(context)
                    }

                    TAB_FILTER -> {
                        _itemList.value = getFilterList(context)
                    }
                }
                if (selectedTabIndex.value != lastSelectedTabIndex.value) {
                    _selectedFunctionIndex.value = -1
                }
                lastSelectedTabIndex.value = selectedTabIndex.value
            }
        }
    }

    fun destroy() {
        helper?.destroy()
        helper = null
    }

    fun loadImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = if (SettingsStore.photoSizeLimit != SettingsStore.PHOTO_SIZE_NO_LIMIT) {
                Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .transform(EvenDimensionsTransformation())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(getSizeLimit())
                    .submit()
                    .get()
            } else {
                Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .transform(EvenDimensionsTransformation())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .submit()
                    .get()
            }
            startImageFilter(context, bitmap)
        }
    }

    fun onSelect(index: Int) {
        if (selectedTabIndex.value == TAB_FILTER) {
            _selectedFilterIndex.value = index
        } else {
            _selectedFunctionIndex.value = if (selectedFunctionIndex.value == -1 || index != selectedFunctionIndex.value) index else -1
        }
        when (selectedTabIndex.value) {
            TAB_BEAUTIFY -> {
                if (selectedFunctionIndex.value != -1) {
                    _currentLevel.value = beautifyLevelMap.value[selectedFunctionIndex.value] ?: 0f
                }
            }

            TAB_ADJUST -> {
                if (selectedFunctionIndex.value != -1) {
                    _currentLevel.value = adjustLevelMap.value[selectedFunctionIndex.value] ?: 0f
                }
            }

            TAB_FILTER -> {
                if (selectedFilterIndex.value != -1) {
                    _currentLevel.value = filterLevelMap.value[selectedFilterIndex.value] ?: 1f
                    helper?.applyCustomFilter(selectedFilterIndex.value, currentLevel.value, true)
                    viewModelScope.launch {
                        refreshUndoRedo()
                    }
                }
            }
        }
    }

    fun onValueChange(value: Float) {
        _currentLevel.value = value
        updateHelperValue()
    }

    fun onValueChangeFinished() {
        updateHelperValue(addRecord = true)
        viewModelScope.launch {
            refreshUndoRedo()
        }
    }

    private fun updateHelperValue(addRecord: Boolean = false) {
        when (selectedTabIndex.value) {
            TAB_BEAUTIFY -> {
                beautifyLevelMap.update {
                    it + (selectedFunctionIndex.value to currentLevel.value)
                }
                when (selectedFunctionIndex.value) {
                    INDEX_SMOOTH -> helper?.setSmoothLevel(currentLevel.value, addRecord)
                    INDEX_WHITE -> helper?.setWhiteLevel(currentLevel.value, addRecord)
                    INDEX_LIPSTICK -> helper?.setLipstickLevel(currentLevel.value, addRecord)
                    INDEX_BLUSHER -> helper?.setBlusherLevel(currentLevel.value, addRecord)
                    INDEX_EYE_ZOOM -> helper?.setEyeZoomLevel(currentLevel.value, addRecord)
                    INDEX_FACE_SLIM -> helper?.setFaceSlimLevel(currentLevel.value, addRecord)
                }
            }

            TAB_ADJUST -> {
                adjustLevelMap.update {
                    it + (selectedFunctionIndex.value to currentLevel.value)
                }
                when (selectedFunctionIndex.value) {
                    INDEX_CONTRAST -> helper?.setContrastLevel(currentLevel.value, addRecord)
                    INDEX_EXPOSURE -> helper?.setExposureLevel(currentLevel.value, addRecord)
                    INDEX_SATURATION -> helper?.setSaturationLevel(currentLevel.value, addRecord)
                    INDEX_SHARPEN -> helper?.setSharpenLevel(currentLevel.value, addRecord)
                    INDEX_BRIGHTNESS -> helper?.setBrightnessLevel(currentLevel.value, addRecord)
                }
            }

            TAB_FILTER -> {
                filterLevelMap.update {
                    it + (selectedFunctionIndex.value to currentLevel.value)
                }
                helper?.applyCustomFilter(selectedFilterIndex.value, currentLevel.value, addRecord)
            }
        }
    }

    fun updateSelectedTab(tabIndex: Int) {
        _selectedTabIndex.value = tabIndex
        if (tabIndex == TAB_FILTER && selectedFilterIndex.value != -1) {
            _currentLevel.value = filterLevelMap.value[selectedFilterIndex.value] ?: 1f
        }
    }

    fun updateLoadStatus(status: Int) {
        _loadStatus.value = status
    }

    fun undo() {
        viewModelScope.launch(Dispatchers.IO) {
            val record = helper?.undo()
            record?.let {
                updateMap(it)
            }
            refreshUndoRedo()
        }
    }

    fun redo() {
        viewModelScope.launch(Dispatchers.IO) {
            val record = helper?.redo()
            record?.let {
                updateMap(it)
            }
            refreshUndoRedo()
        }
    }

    private fun updateMap(record: OpenPSRecord) {
        beautifyLevelMap.update {
            mapOf(
                INDEX_SMOOTH to record.smoothLevel,
                INDEX_WHITE to record.whiteLevel,
                INDEX_LIPSTICK to record.lipstickLevel,
                INDEX_BLUSHER to record.blusherLevel,
                INDEX_EYE_ZOOM to record.eyeZoomLevel,
                INDEX_FACE_SLIM to record.faceSlimLevel
            )
        }
        adjustLevelMap.update {
            mapOf(
                INDEX_CONTRAST to record.contrastLevel,
                INDEX_EXPOSURE to record.exposureLevel,
                INDEX_SATURATION to record.saturationLevel,
                INDEX_SHARPEN to record.sharpenLevel,
                INDEX_BRIGHTNESS to record.brightnessLevel
            )
        }
        filterLevelMap.update {
            mapOf(record.customFilterType to record.customFilterIntensity)
        }
        _selectedFunctionIndex.value = -1
        if (selectedTabIndex.value == TAB_FILTER) {
            _currentLevel.value = record.customFilterIntensity
            _selectedFilterIndex.value = record.customFilterType
        }
    }

    private fun startImageFilter(context: Context, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Main) {
            // 0. 使用最基本的渲染管线，先让图片显示到屏幕上（加载状态开始，不得操作效果滑杆）
            _loadStatus.emit(STATUS_LOADING)
            helper?.initWithImage(bitmap)
            helper?.buildBasicRenderPipeline()
            helper?.requestRender()

            // 1. 从Native层获取VNN人脸识别的结果
            val landmarkResult = helper?.getLandmark()
            val rect = landmarkResult?.rect
            if (rect != null && rect.size == 4) {
                val rectLeft = rect[0]
                val rectTop = rect[1]
                val rectRight = rect[2]
                val rectBottom = rect[3]
                val rectWidth = abs(rectRight - rectLeft)
                val rectHeight = abs(rectBottom - rectTop)

                // 2. 适当扩大人脸框的区域
                faceRectLeft = (rectLeft - rectWidth * faceRectExpandRatio).coerceAtLeast(0f)
                faceRectTop = (rectTop - rectHeight * faceRectExpandRatio).coerceAtLeast(0f)
                faceRectRight = (rectRight + rectWidth * faceRectExpandRatio).coerceAtMost(1f)
                faceRectBottom = (rectBottom + rectHeight * faceRectExpandRatio).coerceAtMost(1f)

                // 3. 根据原图缩放比例展示人脸框
                val renderViewInfo = helper?.getRenderViewInfo()
                renderViewInfo?.let { info ->
                    callback?.showOverlayView(info, RectF(faceRectLeft, faceRectTop, faceRectRight, faceRectBottom))
                }

                // 4. 使用深度学习模型进行皮肤分割
                withContext(Dispatchers.IO) {
                    val croppedBitmap = BitmapUtils.cropBitmap(bitmap, faceRectLeft, faceRectTop, faceRectRight, faceRectBottom)
                    var result = NativeLib.loadBitmap(croppedBitmap)

                    if (result != 0) {
                        // 加载失败
                        _loadStatus.emit(STATUS_ERROR)
                        ToastUtils.showToast(context, context.getString(R.string.msg_image_load_fail))
                        return@withContext
                    }

                    result = NativeLib.runSkinModelInference(context.assets, "79999_iter.tflite")
                    if (result != 0) {
                        // 加载失败
                        _loadStatus.emit(STATUS_ERROR)
                        ToastUtils.showToast(context, context.getString(R.string.msg_image_process_fail))
                        return@withContext
                    }

                    skinMaskBitmap = NativeLib.getSkinMaskBitmap()
                    if (skinMaskBitmap == null) {
                        // 加载失败
                        _loadStatus.emit(STATUS_ERROR)
                        ToastUtils.showToast(context, context.getString(R.string.msg_image_process_fail))
                        return@withContext
                    }

                    // 5. 把皮肤分割结果保存到资源目录
                    skinMaskBitmap = BitmapUtils.mergeBitmap(bitmap, skinMaskBitmap!!, faceRectLeft, faceRectTop, faceRectRight, faceRectBottom)
                    skinMaskBitmap?.let {
                        BitmapUtils.saveBitmapToFile(it, GPUPixel.getResource_path(), "skin_mask.png")

                        withContext(Dispatchers.Main) {
                            callback?.setDebugImage(it)

                            // 6. 重新搭建一套带美颜滤镜的渲染管线（加载成功，可以操作效果滑杆）
                            helper?.buildRealRenderPipeline()
                            helper?.requestRender()
                            _loadStatus.emit(STATUS_SUCCESS)
                        }
                    }
                }
            } else {
                // 人脸识别失败
                _loadStatus.emit(STATUS_ERROR)
                _selectedTabIndex.emit(TAB_ADJUST)
                ToastUtils.showToast(context, context.getString(R.string.msg_face_detect_fail))
                // 设置RenderViewInfo
                val renderViewInfo = helper?.getRenderViewInfo()
                renderViewInfo?.let { info ->
                    callback?.onRenderViewInfoReady(info)
                }
                withContext(Dispatchers.Main) {
                    // 搭建一套不支持美颜的渲染管线
                    helper?.buildNoFaceRenderPipeline()
                    helper?.requestRender()
                }
            }
        }
    }

    private fun getSizeLimit() = when (SettingsStore.photoSizeLimit) {
        SettingsStore.PHOTO_SIZE_LIMIT_4K -> 4096
        SettingsStore.PHOTO_SIZE_LIMIT_2K -> 2048
        SettingsStore.PHOTO_SIZE_LIMIT_1K -> 1024
        else -> Int.MAX_VALUE
    }

    private suspend fun refreshUndoRedo() {
        _canUndo.emit(helper?.canUndo() ?: false)
        _canRedo.emit(helper?.canRedo() ?: false)
    }
}
