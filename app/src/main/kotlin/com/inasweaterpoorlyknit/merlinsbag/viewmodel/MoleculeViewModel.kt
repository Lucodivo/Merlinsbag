package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class MoleculeViewModel<UIEvent, UIState, UIEffect>(
  val uiStateManager: ComposeUIStateManager<UIEvent, UIState, UIEffect>
): ViewModel() {
  private val uiScope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

  // Unlike a MutableSharedFlow, Channels will hold onto values
  // until an observer is present to consumes them
  // This is important when ::onUiEvent gets called before uiStateManager
  // can subscribe to the uiEvent Flow. This can happen on the initial
  // startup of the viewmodel or after resuming from system-initiated
  // process death.
  private val _uiEvents = Channel<UIEvent>(capacity = 20)
  private val _uiEffects = Channel<UIEffect>(capacity = 20)

  val uiEffect: Flow<UIEffect> = _uiEffects.receiveAsFlow()
  val uiState: StateFlow<UIState> = moleculeFlow(mode = RecompositionMode.ContextClock) {
      uiStateManager.uiState(
        uiEvents = _uiEvents.receiveAsFlow(),
        launchUiEffect = ::launchUiEffect
      )
    }.stateIn(
      scope = uiScope,
      started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
      initialValue = uiStateManager.cachedState,
    )

  fun onUiEvent(uiEvent: UIEvent) =
      viewModelScope.launch { _uiEvents.send(uiEvent) }

  private fun launchUiEffect(uiEffect: UIEffect) =
      viewModelScope.launch { _uiEffects.send(uiEffect) }
}