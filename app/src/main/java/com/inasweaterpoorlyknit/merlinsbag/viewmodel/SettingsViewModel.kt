package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.PurgeRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.HighContrast
import com.inasweaterpoorlyknit.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeRepository: PurgeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {
  private val _cacheClearedTrigger = MutableSharedFlow<Unit>()
  val cacheClearedTrigger: SharedFlow<Unit> = _cacheClearedTrigger

  private val _allDataDeletedTrigger = MutableSharedFlow<Unit>()
  val allDataDeletedTrigger: SharedFlow<Unit> = _allDataDeletedTrigger

  val userPreferences = userPreferencesRepository.userPreferences
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UserPreferences(),
      )

  fun clearCache() = viewModelScope.launch(Dispatchers.IO) {
    purgeRepository.purgeCache()
    _cacheClearedTrigger.emit(Unit)
  }
  fun deleteAllData() = viewModelScope.launch(Dispatchers.IO) {
    purgeRepository.purgeUserData()
    _allDataDeletedTrigger.emit(Unit)
  }

  fun setDarkMode(darkMode: DarkMode) = viewModelScope.launch(Dispatchers.IO){
    userPreferencesRepository.setDarkMode(darkMode)
  }

  fun setColorPalette(colorPalette: ColorPalette) = viewModelScope.launch(Dispatchers.IO){
    userPreferencesRepository.setColorPalette(colorPalette)
  }

  fun setHighContrast(highContrast: HighContrast) = viewModelScope.launch(Dispatchers.IO){
    userPreferencesRepository.setHighContrast(highContrast)
  }
}