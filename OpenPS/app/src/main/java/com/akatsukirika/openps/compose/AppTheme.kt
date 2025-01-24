package com.akatsukirika.openps.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = AppColors.Green500,
            primaryVariant = AppColors.Green200,
            background = AppColors.DarkBG
        )
    ) {
        content()
    }
}