package com.akatsukirika.openps.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.compose.MODE_PAINT
import com.akatsukirika.openps.interop.NativeLib
import kotlinx.coroutines.flow.MutableStateFlow

class EliminateViewModel : ViewModel() {
    companion object {
        const val TAG = "EliminateViewModel"
    }

    val mode = MutableStateFlow(MODE_PAINT)

    val size = MutableStateFlow(0.5f)

    val matrix = MutableStateFlow(Matrix())

    var originalBitmap: Bitmap? = null

    fun runInpaint(context: Context, mask: Bitmap?) {
        if (originalBitmap != null && mask != null) {
            val resultBitmap = NativeLib.runInpaint(
                imageBitmap = originalBitmap!!,
                maskBitmap = mask,
                assetManager = context.assets,
                modelFile = "migan_pipeline_v2.onnx"
            )
            Log.d(TAG, "resultBitmap width: ${resultBitmap?.width}, height: ${resultBitmap?.height}")
        }
    }
}