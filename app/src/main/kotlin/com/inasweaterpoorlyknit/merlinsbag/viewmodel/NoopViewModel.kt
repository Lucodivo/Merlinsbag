package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class NoopViewModel<UIEvent, UIState, UIEffect>(): ViewModel() {
  private val _uiEvents = MutableSharedFlow<UIEvent>(extraBufferCapacity = 20)
  private val _uiEffects = MutableSharedFlow<UIEffect>(extraBufferCapacity = 20)

  val uiEffect: SharedFlow<UIEffect> = _uiEffects
  val uiState: StateFlow<UIState> by lazy(LazyThreadSafetyMode.NONE) {
    uiStateFlow().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
      initialValue = initialUiState(),
    )
  }

  fun onUiEvent(uiEvent: UIEvent) {
    if(!_uiEvents.tryEmit(uiEvent)) {
      error("SettingsViewModel: UI event buffer overflow.")
    }
  }

  protected fun launchUiEffect(uiEffect: UIEffect){
    if(!_uiEffects.tryEmit(uiEffect)) {
      error("SettingsViewModel: UI effect buffer overflow.")
    }
  }

  init {
    viewModelScope.launch(Dispatchers.IO) {
      _uiEvents.collect { handleUiEvent(it) }
    }
  }

  protected abstract fun initialUiState(): UIState
  protected abstract fun uiStateFlow(): Flow<UIState>
  protected abstract suspend fun handleUiEvent(uiEvent: UIEvent)
}

abstract class NoopViewModel2<UIEvent, UIState, UIEffect>(): ViewModel() {
  private val _uiEffects = MutableSharedFlow<UIEffect>(extraBufferCapacity = 20)

  val uiEffect: SharedFlow<UIEffect> = _uiEffects
  val uiState: StateFlow<UIState> by lazy(LazyThreadSafetyMode.NONE) {
    uiStateFlow().stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
      initialValue = initialUiState(),
    )
  }

  protected fun launchUiEffect(uiEffect: UIEffect){
    if(!_uiEffects.tryEmit(uiEffect)) {
      error("SettingsViewModel: UI effect buffer overflow.")
    }
  }

  protected abstract fun initialUiState(): UIState
  protected abstract fun uiStateFlow(): Flow<UIState>
  abstract fun onUiEvent(uiEvent: UIEvent)
}
