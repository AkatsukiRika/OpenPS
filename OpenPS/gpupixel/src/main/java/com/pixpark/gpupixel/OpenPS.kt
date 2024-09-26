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

    external fun nativeSetRawOutputCallback(receiver: Any)

    external fun nativeSetSmoothLevel(level: Float)

    external fun nativeSetWhiteLevel(level: Float)

    external fun nativeSetLipstickLevel(level: Float)

    external fun nativeSetBlusherLevel(level: Float)

    external fun nativeSetEyeZoomLevel(level: Float)

    external fun nativeSetFaceSlimLevel(level: Float)

    external fun nativeSetContrastLevel(level: Float)

    external fun nativeSetExposureLevel(level: Float)

    external fun nativeSetSaturationLevel(level: Float)

    external fun nativeSetSharpenLevel(level: Float)

    external fun nativeCompareBegin()

    external fun nativeCompareEnd()

    external fun nativeSetScaleFactor(scale: Float)

    external fun nativeSetTranslateDistance(x: Float, y: Float)

    external fun nativeResetMVPMatrix()

    external fun nativeGetTranslateDistanceX(): Float

    external fun nativeGetTranslateDistanceY(): Float
}