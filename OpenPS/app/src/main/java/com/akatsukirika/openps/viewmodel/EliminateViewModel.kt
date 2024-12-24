package com.akatsukirika.openps.viewmodel

import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.compose.MODE_PAINT
import kotlinx.coroutines.flow.MutableStateFlow

class EliminateViewModel : ViewModel() {
    val mode = MutableStateFlow(MODE_PAINT)

    val size = MutableStateFlow(0.5f)
}