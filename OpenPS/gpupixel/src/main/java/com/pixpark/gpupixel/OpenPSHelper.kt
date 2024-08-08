package com.pixpark.gpupixel

import android.graphics.Bitmap
import com.pixpark.gpupixel.GPUPixel.GPUPixelLandmarkCallback
import com.pixpark.gpupixel.model.LandmarkResult
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

    fun requestRender() {
        renderView.requestRender()
    }

    fun setSmoothLevel(level: Float) {
        renderView.postOnGLThread {
            OpenPS.nativeSetSmoothLevel(level)
            requestRender()
        }
    }

    fun setWhiteLevel(level: Float) {
        renderView.postOnGLThread {
            OpenPS.nativeSetWhiteLevel(level)
            requestRender()
        }
    }

    fun setLipstickLevel(level: Float) {
        renderView.postOnGLThread {
            OpenPS.nativeSetLipstickLevel(level)
            requestRender()
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

    // C++层回调方法
    fun onLandmarkDetected(landmarks: FloatArray, rect: FloatArray) {
        landmarkCallback?.onFaceLandmark(landmarks, rect)
    }
}