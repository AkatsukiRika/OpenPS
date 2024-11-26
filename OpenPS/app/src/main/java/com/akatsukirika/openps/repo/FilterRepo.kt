package com.akatsukirika.openps.repo

import android.content.Context
import com.akatsukirika.openps.R
import com.akatsukirika.openps.model.FunctionItem

const val INDEX_ORIGINAL = 0
const val INDEX_FAIRY_TALE = 1
const val INDEX_SUNRISE = 2
const val INDEX_SUNSET = 3
const val INDEX_WHITE_CAT = 4
const val INDEX_BLACK_CAT = 5
const val INDEX_BEAUTY = 6

fun getFilterList(context: Context) = listOf(
    FunctionItem(index = INDEX_ORIGINAL, icon = R.drawable.img_filter_original, name = context.getString(R.string.filter_original), labelBgColor = context.getColor(R.color.filter_color_grey_light), isOriginal = true),
    FunctionItem(index = INDEX_FAIRY_TALE, icon = R.drawable.img_filter_fairy_tale, name = context.getString(R.string.filter_fairy_tale), labelBgColor = context.getColor(R.color.filter_color_blue)),
    FunctionItem(index = INDEX_SUNRISE, icon = R.drawable.img_filter_sunrise, name = context.getString(R.string.filter_sunrise), labelBgColor = context.getColor(R.color.filter_color_brown_light)),
    FunctionItem(index = INDEX_SUNSET, icon = R.drawable.img_filter_sunset, name = context.getString(R.string.filter_sunset), labelBgColor = context.getColor(R.color.filter_color_brown_light)),
    FunctionItem(index = INDEX_WHITE_CAT, icon = R.drawable.img_filter_white_cat, name = context.getString(R.string.filter_white_cat), labelBgColor = context.getColor(R.color.filter_color_brown_light)),
    FunctionItem(index = INDEX_BLACK_CAT, icon = R.drawable.img_filter_black_cat, name = context.getString(R.string.filter_black_cat), labelBgColor = context.getColor(R.color.filter_color_brown_light)),
    FunctionItem(index = INDEX_BEAUTY, icon = R.drawable.img_filter_beauty, name = context.getString(R.string.filter_beauty), labelBgColor = context.getColor(R.color.filter_color_red))
)