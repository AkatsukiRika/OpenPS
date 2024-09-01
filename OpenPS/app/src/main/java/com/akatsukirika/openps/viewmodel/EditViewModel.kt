package com.akatsukirika.openps.viewmodel

import com.akatsukirika.openps.compose.FunctionItem
import com.pixpark.gpupixel.OpenPSHelper
import com.pixpark.gpupixel.view.OpenPSRenderView

class EditViewModel : BaseViewModel<EditState, EditEvent, EditEffect>() {
    private var helper: OpenPSHelper? = null

    fun init(renderView: OpenPSRenderView) {
        helper = OpenPSHelper(renderView)
    }

    fun destroy() {
        helper = null
    }

    override fun getInitState() = EditState()

    override fun dispatch(event: EditEvent) {
        when (event) {
            is EditEvent.SetSmoothLevel -> {
                helper?.setSmoothLevel(event.level)
            }
            is EditEvent.SetWhiteLevel -> {
                helper?.setWhiteLevel(event.level)
            }
            is EditEvent.SetLipstickLevel -> {
                helper?.setLipstickLevel(event.level)
            }
            is EditEvent.SetBlusherLevel -> {
                helper?.setBlusherLevel(event.level)
            }
            is EditEvent.SetEyeZoomLevel -> {
                helper?.setEyeZoomLevel(event.level)
            }
            is EditEvent.SetFaceSlimLevel -> {
                helper?.setFaceSlimLevel(event.level)
            }
            is EditEvent.SetContrastLevel -> {
                helper?.setContrastLevel(event.level)
            }
            is EditEvent.BeginCompare -> {
                helper?.onCompareBegin()
            }
            is EditEvent.EndCompare -> {
                helper?.onCompareEnd()
            }
        }
    }
}

data class EditState(
    val lastSelectedTabIndex: Int = 0,
    val selectedTabIndex: Int = 0,
    val selectedFunctionIndex: Int = -1,
    val currentLevel: Float = 0f,
    val beautifyLevelMap: Map<Int, Float> = emptyMap(),
    val adjustLevelMap: Map<Int, Float> = emptyMap(),
    val itemList: List<FunctionItem> = emptyList()
): BaseState

sealed class EditEvent : BaseEvent {
    data class SetSmoothLevel(val level: Float) : EditEvent()
    data class SetWhiteLevel(val level: Float) : EditEvent()
    data class SetLipstickLevel(val level: Float) : EditEvent()
    data class SetBlusherLevel(val level: Float) : EditEvent()
    data class SetEyeZoomLevel(val level: Float) : EditEvent()
    data class SetFaceSlimLevel(val level: Float) : EditEvent()
    data class SetContrastLevel(val level: Float) : EditEvent()
    data object BeginCompare : EditEvent()
    data object EndCompare : EditEvent()
}

sealed class EditEffect : BaseEffect