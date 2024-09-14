package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

interface ComposeUIStateManager<UIEvent, UIState, UIEffect> {
  @Composable
  fun uiState(
      uiEvents: Flow<UIEvent>,
      launchUiEffect: (UIEffect) -> Unit,
  ): UIState
  val cachedState: UIState
}