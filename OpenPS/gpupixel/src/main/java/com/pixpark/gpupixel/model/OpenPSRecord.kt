package com.pixpark.gpupixel.model

data class OpenPSRecord(
    val smoothLevel: Float,
    val whiteLevel: Float,
    val lipstickLevel: Float,
    val blusherLevel: Float,
    val eyeZoomLevel: Float,
    val faceSlimLevel: Float,
    val contrastLevel: Float,
    val exposureLevel: Float,
    val saturationLevel: Float,
    val sharpenLevel: Float,
    val brightnessLevel: Float,
    val customFilterType: Int,
    val customFilterIntensity: Float,
    val isMirrored: Boolean,
    val isFlipped: Boolean
)