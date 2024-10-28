package com.akatsukirika.openps.repo

import android.content.Context
import com.akatsukirika.openps.R
import com.akatsukirika.openps.model.FunctionItem

const val INDEX_SMOOTH = 0
const val INDEX_WHITE = 1
const val INDEX_LIPSTICK = 2
const val INDEX_BLUSHER = 3
const val INDEX_EYE_ZOOM = 4
const val INDEX_FACE_SLIM = 5

fun getBeautifyFunctionList(context: Context) = listOf(
    FunctionItem(index = INDEX_SMOOTH, icon = R.drawable.ic_smooth, name = context.getString(R.string.smooth)),
    FunctionItem(index = INDEX_WHITE, icon = R.drawable.ic_white, name = context.getString(R.string.white)),
    FunctionItem(index = INDEX_LIPSTICK, icon = R.drawable.ic_lipstick, name = context.getString(R.string.lipstick)),
    FunctionItem(index = INDEX_BLUSHER, icon = R.drawable.ic_blusher, name = context.getString(R.string.blusher)),
    FunctionItem(index = INDEX_EYE_ZOOM, icon = R.drawable.ic_eye_zoom, name = context.getString(R.string.eye_zoom)),
    FunctionItem(index = INDEX_FACE_SLIM, icon = R.drawable.ic_face_slim, name = context.getString(R.string.face_slim))
)