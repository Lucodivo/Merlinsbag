package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
  fun onGetStarted() = viewModelScope.launch(Dispatchers.IO){
    userPreferencesRepository.setHasCompletedOnboarding(true)
  }
}