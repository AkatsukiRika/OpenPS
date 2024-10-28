package com.akatsukirika.openps.repo

import android.content.Context
import com.akatsukirika.openps.R
import com.akatsukirika.openps.model.FunctionItem

const val INDEX_CONTRAST = 0
const val INDEX_EXPOSURE = 1
const val INDEX_SATURATION = 2
const val INDEX_SHARPEN = 3
const val INDEX_BRIGHTNESS = 4

fun getAdjustFunctionList(context: Context) = listOf(
    FunctionItem(index = INDEX_CONTRAST, icon = R.drawable.ic_contrast, name = context.getString(R.string.contrast), hasTwoWaySlider = true),
    FunctionItem(index = INDEX_EXPOSURE, icon = R.drawable.ic_exposure, name = context.getString(R.string.exposure), hasTwoWaySlider = true),
    FunctionItem(index = INDEX_SATURATION, icon = R.drawable.ic_saturation, name = context.getString(R.string.saturation), hasTwoWaySlider = true),
    FunctionItem(index = INDEX_SHARPEN, icon = R.drawable.ic_sharpen, name = context.getString(R.string.sharpen)),
    FunctionItem(index = INDEX_BRIGHTNESS, icon = R.drawable.ic_brightness, name = context.getString(R.string.brightness), hasTwoWaySlider = true)
)