package com.akatsukirika.openps.interop

import android.util.Log
import com.akatsukirika.openps.model.debug.DebugProgramItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PipelineDebugHelper {
    const val TAG = "PipelineDebugHelper"

    private val _programItems = MutableStateFlow(listOf<DebugProgramItem>())
    val programItems: StateFlow<List<DebugProgramItem>> = _programItems

    fun onProgramCreated(id: Int, filterName: String, isActive: Boolean) {
        val containsId = programItems.value.find { it.id == id }
        val newItem = DebugProgramItem(
            id = id,
            filterName = filterName,
            isActive = isActive
        )
        val newList = programItems.value.toMutableList()
        if (containsId != null) {
            newList.remove(containsId)
        }
        newList.add(newItem)
        _programItems.value = newList.toList()
    }

    fun onActivateProgram(id: Int) {
        val newList = mutableListOf<DebugProgramItem>()
        programItems.value.forEach {
            if (it.id == id) {
                newList.add(it.copy(isActive = true))
            } else {
                newList.add(it.copy(isActive = false))
            }
        }
        _programItems.value = newList
        Log.i(TAG, "onActivateProgram: $id")
    }

    fun clearPrograms() {
        _programItems.value = emptyList()
    }
}