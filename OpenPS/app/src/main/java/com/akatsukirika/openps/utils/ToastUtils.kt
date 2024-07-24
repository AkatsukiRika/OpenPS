package com.akatsukirika.openps.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    fun showToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}