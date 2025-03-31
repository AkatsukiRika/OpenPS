package com.pixpark.gpupixel

import android.graphics.Bitmap
import com.pixpark.gpupixel.model.OpenPSRecord

internal object OpenPS {
    init {
        System.loadLibrary("gpupixel")
        System.loadLibrary("vnn_core")
        System.loadLibrary("vnn_kit")
        System.loadLibrary("vnn_face")
    }

    external fun nativeInit()

    external fun nativeInitWithImage(width: Int, height: Int, channelCount: Int, bitmap: Bitmap, filename: String? = null)

    external fun nativeUpdateTransform(
        mirrored: Boolean, flipped: Boolean,
        cropLeft: Float, cropTop: Float,
        cropRight: Float, cropBottom: Float,
        rotation: Float
    )

    external fun nativeChangeImage(width: Int, height: Int, channelCount: Int, bitmap: Bitmap, filename: String? = null)

    external fun nativeDestroy()

    external fun nativeTargetViewSizeChanged(width: Int, height: Int)

    external fun nativeTargetViewGetInfo(): FloatArray?

    external fun nativeBuildBasicRenderPipeline()

    external fun nativeBuildRealRenderPipeline()

    external fun nativeBuildNoFaceRenderPipeline()

    external fun nativeRequestRender()

    external fun nativeSetLandmarkCallback(receiver: Any)

    external fun nativeManualDetectFace(receiver: Any)

    external fun nativeSetRawOutputCallback(receiver: Any)

    external fun nativeSetSmoothLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetWhiteLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetLipstickLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetBlusherLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetEyeZoomLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetFaceSlimLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetContrastLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetExposureLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetSaturationLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetSharpenLevel(level: Float, addRecord: Boolean = false)

    external fun nativeSetBrightnessLevel(level: Float, addRecord: Boolean = false)

    external fun nativeApplyCustomFilter(type: Int, level: Float = 1f, addRecord: Boolean = false)

    external fun nativeUpdateSkinMask()

    external fun nativeCompareBegin()

    external fun nativeCompareEnd()

    external fun nativeUpdateMVPMatrix(matrix: FloatArray)

    external fun nativeCanUndo(): Boolean

    external fun nativeCanRedo(): Boolean

    external fun nativeUndo(): OpenPSRecord?

    external fun nativeRedo(): OpenPSRecord?

    external fun nativeGetCurrentImageFileName(): String?
}