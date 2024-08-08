package com.pixpark.gpupixel

import android.graphics.Bitmap

internal object OpenPS {
    init {
        System.loadLibrary("gpupixel")
        System.loadLibrary("vnn_core")
        System.loadLibrary("vnn_kit")
        System.loadLibrary("vnn_face")
    }

    external fun nativeInit()

    external fun nativeInitWithImage(width: Int, height: Int, channelCount: Int, bitmap: Bitmap)

    external fun nativeDestroy()

    external fun nativeTargetViewSizeChanged(width: Int, height: Int)

    external fun nativeTargetViewGetInfo(): FloatArray?

    external fun nativeBuildBasicRenderPipeline()

    external fun nativeBuildRealRenderPipeline()

    external fun nativeRequestRender()

    external fun nativeSetLandmarkCallback(receiver: Any)

    external fun nativeSetSmoothLevel(level: Float)

    external fun nativeSetWhiteLevel(level: Float)
}