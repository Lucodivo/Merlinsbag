package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.data.repository.PurgeRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.HighContrast
import com.inasweaterpoorlyknit.core.model.ImageQuality
import com.inasweaterpoorlyknit.core.model.Typography
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeRepository: PurgeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {
  var cachePurged by mutableStateOf(Event<Unit>(null))
  var dataDeleted by mutableStateOf(Event<Unit>(null))
  var showDeleteAllDataAlertDialog by mutableStateOf(false)
  var expandedDarkModeMenu by mutableStateOf(false)
  var expandedColorPaletteMenu by mutableStateOf(false)
  var expandedHighContrastMenu by mutableStateOf(false)
  var expandedTypographyMenu by mutableStateOf(false)
  var expandedImageQualityMenu by mutableStateOf(false)
  var clearCacheEnabled by mutableStateOf(true)

  val userPreferences = userPreferencesRepository.userPreferences
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = UserPreferences(),
      )

  fun clearCache() {
    // Disable clearing cache until view model is recreated
    clearCacheEnabled = false
    viewModelScope.launch(Dispatchers.IO) {
      purgeRepository.purgeCache()
      cachePurged = Event(Unit)
    }
  }

  fun onClickDeleteAllData() { showDeleteAllDataAlertDialog = true }
  fun onDismissDeleteAllDataAlertDialog() { showDeleteAllDataAlertDialog = false }
  fun deleteAllData() {
    showDeleteAllDataAlertDialog = false
    viewModelScope.launch(Dispatchers.IO) {
      purgeRepository.purgeUserData()
      dataDeleted = Event(Unit)
    }
  }

  fun onClickDarkMode() { expandedDarkModeMenu = !expandedDarkModeMenu }
  fun onDismissDarkMode() { expandedDarkModeMenu = false }
  fun setDarkMode(darkMode: DarkMode) {
    expandedDarkModeMenu = false
    viewModelScope.launch(Dispatchers.IO){
      userPreferencesRepository.setDarkMode(darkMode)
    }
  }

  fun onClickColorPalette() { expandedColorPaletteMenu = !expandedColorPaletteMenu }
  fun onDismissColorPalette() { expandedColorPaletteMenu = false }
  fun setColorPalette(colorPalette: ColorPalette) {
    expandedColorPaletteMenu = false
    viewModelScope.launch(Dispatchers.IO){
      userPreferencesRepository.setColorPalette(colorPalette)
    }
  }

  fun onClickHighContrast() { expandedHighContrastMenu = !expandedHighContrastMenu }
  fun onDismissHighContrast() { expandedHighContrastMenu = false }
  fun setHighContrast(highContrast: HighContrast) {
    expandedHighContrastMenu = false
    viewModelScope.launch(Dispatchers.IO){
      userPreferencesRepository.setHighContrast(highContrast)
    }
  }

  fun onClickTypography() { expandedTypographyMenu = !expandedTypographyMenu }
  fun onDismissTypography() { expandedTypographyMenu = false }
  fun setTypography(typography: Typography) {
    expandedTypographyMenu = false
    viewModelScope.launch(Dispatchers.IO){
      userPreferencesRepository.setTypography(typography)
    }
  }

  fun onClickImageQuality() { expandedImageQualityMenu = !expandedImageQualityMenu }
  fun onDismissImageQuality() { expandedImageQualityMenu = false }
  fun setImageQuality(imageQuality: ImageQuality) {
    expandedImageQualityMenu = false
    viewModelScope.launch(Dispatchers.IO){
      userPreferencesRepository.setImageQuality(imageQuality)
    }
  }

  fun showWelcomePage() = viewModelScope.launch(Dispatchers.IO){
    userPreferencesRepository.setHasCompletedOnboarding(false)
  }
}