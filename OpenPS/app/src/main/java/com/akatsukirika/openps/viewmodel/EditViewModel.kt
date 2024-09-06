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
import com.akatsukirika.openps.interop.NativeLib
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.BitmapUtils
import com.akatsukirika.openps.utils.EvenDimensionsTransformation
import com.akatsukirika.openps.utils.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pixpark.gpupixel.GPUPixel
import com.pixpark.gpupixel.OpenPSHelper
import com.pixpark.gpupixel.model.RenderViewInfo
import com.pixpark.gpupixel.view.OpenPSRenderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class EditViewModel : ViewModel() {
    interface Callback {
        fun showOverlayView(info: RenderViewInfo, faceRectF: RectF)
        fun setDebugImage(bitmap: Bitmap)
    }

    var helper: OpenPSHelper? = null
        private set

    private var callback: Callback? = null

    private var skinMaskBitmap: Bitmap? = null

    private val _loadStatus = MutableStateFlow(STATUS_IDLE)
    val loadStatus: StateFlow<Int> = _loadStatus

    // Face Rect
    private var faceRectLeft: Float = 0f
    private var faceRectTop: Float = 0f
    private var faceRectRight: Float = 0f
    private var faceRectBottom: Float = 0f
    private val faceRectExpandRatio: Float = 0.5f

    fun init(renderView: OpenPSRenderView, callback: Callback? = null) {
        helper = OpenPSHelper(renderView)
        this.callback = callback
    }

    fun destroy() {
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
                ToastUtils.showToast(context, context.getString(R.string.msg_face_detect_fail))
            }
        }
    }

    private fun getSizeLimit() = when (SettingsStore.photoSizeLimit) {
        SettingsStore.PHOTO_SIZE_LIMIT_4K -> 4096
        SettingsStore.PHOTO_SIZE_LIMIT_2K -> 2048
        SettingsStore.PHOTO_SIZE_LIMIT_1K -> 1024
        else -> Int.MAX_VALUE
    }
}
