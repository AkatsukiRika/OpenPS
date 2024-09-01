package com.akatsukirika.openps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<STATE: BaseState, EVENT: BaseEvent, EFFECT: BaseEffect> : ViewModel() {
    private val _uiState by lazy {
        MutableStateFlow(getInitState())
    }

    val uiState: StateFlow<STATE> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<List<EFFECT>>()

    val uiEffect: SharedFlow<List<EFFECT>> = _uiEffect.asSharedFlow()

    abstract fun getInitState(): STATE

    abstract fun dispatch(event: EVENT)

    fun emitState(reducer: STATE.() -> STATE): STATE {
        _uiState.value = _uiState.value.reducer()
        return _uiState.value
    }

    fun emitEffect(vararg effects: EFFECT) {
        viewModelScope.launch {
            _uiEffect.emit(effects.toList())
        }
    }
}

interface BaseState

interface BaseEvent

interface BaseEffect