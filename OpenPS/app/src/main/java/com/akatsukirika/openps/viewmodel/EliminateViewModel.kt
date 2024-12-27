package com.akatsukirika.openps.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.compose.MODE_PAINT
import com.akatsukirika.openps.compose.STATUS_ERROR
import com.akatsukirika.openps.compose.STATUS_IDLE
import com.akatsukirika.openps.compose.STATUS_LOADING
import com.akatsukirika.openps.compose.STATUS_SUCCESS
import com.akatsukirika.openps.interop.NativeLib
import kotlinx.coroutines.flow.MutableStateFlow

class EliminateViewModel : ViewModel() {
    companion object {
        const val TAG = "EliminateViewModel"
    }

    val mode = MutableStateFlow(MODE_PAINT)

    val size = MutableStateFlow(0.5f)

    val matrix = MutableStateFlow(Matrix())

    val inpaintStatus = MutableStateFlow(STATUS_IDLE)

    val resultBitmap = MutableStateFlow<Bitmap?>(null)

    var originalBitmap: Bitmap? = null

    suspend fun runInpaint(context: Context, mask: Bitmap?) {
        inpaintStatus.emit(STATUS_LOADING)
        if (originalBitmap != null && mask != null) {
            val result = NativeLib.runInpaint(
                imageBitmap = originalBitmap!!,
                maskBitmap = mask,
                assetManager = context.assets,
                modelFile = "migan_pipeline_v2.onnx"
            )
            resultBitmap.emit(result)
            inpaintStatus.emit(STATUS_SUCCESS)
        } else {
            inpaintStatus.emit(STATUS_ERROR)
        }
    }
}