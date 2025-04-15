package com.akatsukirika.openps.interop

import android.util.Log

object PipelineDebugHelper {
    fun onProgramCreated(id: Int, filterName: String, isActive: Boolean) {
        Log.d("PipelineDebugHelper", "Program created: $id, Filter: $filterName, Active: $isActive")
    }
}