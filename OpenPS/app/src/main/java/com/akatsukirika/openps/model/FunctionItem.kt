package com.akatsukirika.openps.model

data class FunctionItem(
    val index: Int,
    val icon: Int,
    val name: String,
    val hasTwoWaySlider: Boolean = false,
    val labelBgColor: Int = 0,
    val isOriginal: Boolean = false
)
