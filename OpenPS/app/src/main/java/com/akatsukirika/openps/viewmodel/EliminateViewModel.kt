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
import com.akatsukirika.openps.utils.BitmapUtils
import com.pixpark.gpupixel.GPUPixel
import com.pixpark.gpupixel.OpenPSHelper
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class EliminateViewModel : ViewModel() {
    companion object {
        const val TAG = "EliminateViewModel"
    }

    val mode = MutableStateFlow(MODE_PAINT)

    val size = MutableStateFlow(0.5f)

    val matrix = MutableStateFlow(Matrix())

    val inpaintStatus = MutableStateFlow(STATUS_IDLE)

    val resultBitmap = MutableStateFlow<Bitmap?>(null)

    val readyToGenerate = MutableStateFlow(false)

    var originalBitmap: Bitmap? = null

    var helper: OpenPSHelper? = null

    suspend fun init(context: Context) {
        originalBitmap = getCurrentImageBitmap(context)
    }

    suspend fun runInpaint(context: Context, mask: Bitmap?) {
        inpaintStatus.emit(STATUS_LOADING)
        val bitmap = getCurrentImageBitmap(context)
        if (bitmap != null && mask != null) {
            val result = NativeLib.runInpaint(
                imageBitmap = bitmap,
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

    private suspend fun getCurrentImageBitmap(context: Context): Bitmap? {
        val currentImageFileName = helper?.getCurrentImageFileName()
        val currentImageFilePath = GPUPixel.getExternalPath() + File.separator + currentImageFileName
        return BitmapUtils.getBitmap(context, currentImageFilePath)
    }
}