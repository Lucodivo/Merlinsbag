package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainActivityUiState {
  data object Loading : MainActivityUiState
  data class Success(val userPreferences: UserPreferences) : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
  val uiState: StateFlow<MainActivityUiState> = userPreferencesRepository.userPreferences.map {
    MainActivityUiState.Success(it)
  }.stateIn(
    scope = viewModelScope,
    initialValue = MainActivityUiState.Loading,
    started = SharingStarted.WhileSubscribed(5_000),
  )
}