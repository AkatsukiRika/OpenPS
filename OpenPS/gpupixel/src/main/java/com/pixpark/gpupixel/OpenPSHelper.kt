package com.pixpark.gpupixel

import android.graphics.Bitmap
import com.pixpark.gpupixel.GPUPixel.GPUPixelLandmarkCallback
import com.pixpark.gpupixel.model.LandmarkResult
import com.pixpark.gpupixel.model.PixelsResult
import com.pixpark.gpupixel.model.RenderViewInfo
import com.pixpark.gpupixel.utils.BitmapUtils
import com.pixpark.gpupixel.view.OpenPSRenderView
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OpenPSHelper(private val renderView: OpenPSRenderView) {
    private var landmarkCallback: GPUPixelLandmarkCallback? = null
    private var resultPixelsCallback: ((ByteArray, Int, Int, Long) -> Unit)? = null

    fun initWithImage(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val channelCount = BitmapUtils.getChannels(bitmap)

        renderView.postOnGLThread {
            OpenPS.nativeInitWithImage(width, height, channelCount, bitmap)
        }
    }

    fun buildBasicRenderPipeline() {
        renderView.postOnGLThread {
            OpenPS.nativeBuildBasicRenderPipeline()
        }
    }

    fun buildRealRenderPipeline() {
        renderView.postOnGLThread {
            OpenPS.nativeBuildRealRenderPipeline()
        }
    }

    fun buildNoFaceRenderPipeline() {
        renderView.postOnGLThread {
            OpenPS.nativeBuildNoFaceRenderPipeline()
        }
    }

    fun requestRender() {
        renderView.requestRender()
    }

    fun setSmoothLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetSmoothLevel(level, addRecord)
            requestRender()
        }
    }

    fun setWhiteLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetWhiteLevel(level, addRecord)
            requestRender()
        }
    }

    fun setLipstickLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetLipstickLevel(level, addRecord)
            requestRender()
        }
    }

    fun setBlusherLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetBlusherLevel(level, addRecord)
            requestRender()
        }
    }

    fun setEyeZoomLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetEyeZoomLevel(level, addRecord)
            requestRender()
        }
    }

    fun setFaceSlimLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetFaceSlimLevel(level, addRecord)
            requestRender()
        }
    }

    fun setContrastLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetContrastLevel(level, addRecord)
            requestRender()
        }
    }

    fun setExposureLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetExposureLevel(level, addRecord)
            requestRender()
        }
    }

    fun setSaturationLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetSaturationLevel(level, addRecord)
            requestRender()
        }
    }

    fun setSharpenLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetSharpenLevel(level, addRecord)
            requestRender()
        }
    }

    fun setBrightnessLevel(level: Float, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeSetBrightnessLevel(level, addRecord)
            requestRender()
        }
    }

    fun applyCustomFilter(type: Int, level: Float = 1f, addRecord: Boolean = false) {
        renderView.postOnGLThread {
            OpenPS.nativeApplyCustomFilter(type, level, addRecord)
            requestRender()
        }
    }

    fun onCompareBegin() {
        renderView.postOnGLThread {
            OpenPS.nativeCompareBegin()
            requestRender()
        }
    }

    fun onCompareEnd() {
        renderView.postOnGLThread {
            OpenPS.nativeCompareEnd()
            requestRender()
        }
    }

    fun updateMVPMatrix(matrix: FloatArray) {
        renderView.postOnGLThread {
            OpenPS.nativeUpdateMVPMatrix(matrix)
            requestRender()
        }
    }

    suspend fun canUndo() = suspendCoroutine {
        renderView.postOnGLThread {
            val result = OpenPS.nativeCanUndo()
            requestRender()
            it.resume(result)
        }
    }

    suspend fun canRedo() = suspendCoroutine {
        renderView.postOnGLThread {
            val result = OpenPS.nativeCanRedo()
            requestRender()
            it.resume(result)
        }
    }

    suspend fun undo() = suspendCoroutine {
        renderView.postOnGLThread {
            it.resume(OpenPS.nativeUndo())
        }
    }

    suspend fun redo() = suspendCoroutine {
        renderView.postOnGLThread {
            it.resume(OpenPS.nativeRedo())
        }
    }

    fun destroy() {
        OpenPS.nativeDestroy()
    }

    suspend fun getLandmark(): LandmarkResult = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<LandmarkResult>()

        setLandmarkCallback(object : GPUPixelLandmarkCallback {
            override fun onFaceLandmark(landmarks: FloatArray?) {}

            override fun onFaceLandmark(landmarks: FloatArray?, rect: FloatArray?) {
                deferred.complete(LandmarkResult(landmarks, rect))
            }
        })

        deferred.await()
    }

    suspend fun getResultPixels(): PixelsResult = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<PixelsResult>()

        setResultPixelsCallback { bytes, i, i2, l ->
            deferred.complete(PixelsResult(bytes, i, i2))
        }

        deferred.await()
    }

    suspend fun getRenderViewInfo() = suspendCoroutine { continuation ->
        renderView.postOnGLThread {
            val info = OpenPS.nativeTargetViewGetInfo()
            if (info != null && info.size == 4) {
                continuation.resume(RenderViewInfo(info[0], info[1], info[2], info[3]))
            } else {
                continuation.resume(null)
            }
        }
    }

    private fun setLandmarkCallback(callback: GPUPixelLandmarkCallback) {
        landmarkCallback = callback

        renderView.postOnGLThread {
            OpenPS.nativeSetLandmarkCallback(this)
            requestRender()
        }
    }

    private fun setResultPixelsCallback(callback: (ByteArray, Int, Int, Long) -> Unit) {
        resultPixelsCallback = callback

        renderView.postOnGLThread {
            OpenPS.nativeSetRawOutputCallback(this)
            requestRender()
        }
    }

    // C++层回调方法
    fun onLandmarkDetected(landmarks: FloatArray, rect: FloatArray) {
        landmarkCallback?.onFaceLandmark(landmarks, rect)
    }

    fun onResultPixels(data: ByteArray, width: Int, height: Int, ts: Long) {
        resultPixelsCallback?.invoke(data, width, height, ts)
    }
}