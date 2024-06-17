package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainActivityUiState {
  data object Loading : MainActivityUiState
  data object Success : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
): ViewModel() {

  private val _uiState = MutableStateFlow<MainActivityUiState>(MainActivityUiState.Loading)
  val uiState: StateFlow<MainActivityUiState> get() = _uiState

  val userPreferences = userPreferencesRepository.userPreferences
      .onEach { _uiState.value = MainActivityUiState.Success }
      .stateIn(
        scope = viewModelScope,
        initialValue = UserPreferences(
          hasCompletedOnboarding = true,
        ),
        started = SharingStarted.WhileSubscribed(5_000),
      )
}