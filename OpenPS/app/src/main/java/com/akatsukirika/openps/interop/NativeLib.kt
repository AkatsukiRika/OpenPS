package com.akatsukirika.openps.interop

import android.content.res.AssetManager
import android.graphics.Bitmap

object NativeLib {
    init {
        System.loadLibrary("openps")
    }

    external fun loadBitmap(bitmap: Bitmap): Int

    external fun releaseBitmap(): Int

    external fun runSkinModelInference(assetManager: AssetManager, modelFile: String): Int
}