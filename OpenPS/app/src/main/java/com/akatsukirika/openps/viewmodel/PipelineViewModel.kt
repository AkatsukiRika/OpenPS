package com.akatsukirika.openps.viewmodel

import androidx.lifecycle.ViewModel
import com.akatsukirika.openps.model.debug.DebugProgramItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PipelineViewModel : ViewModel() {
    private val _programItemList = MutableStateFlow(listOf<DebugProgramItem>())
    val programItemList: StateFlow<List<DebugProgramItem>> = _programItemList
}