package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
  val onboardingComplete: StateFlow<Boolean> =
      userPreferencesRepository.hasCompletedOnboarding
          .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false,
          )

  fun onSkip() = viewModelScope.launch(Dispatchers.IO){
    userPreferencesRepository.setHasCompletedOnboarding(true)
  }
}