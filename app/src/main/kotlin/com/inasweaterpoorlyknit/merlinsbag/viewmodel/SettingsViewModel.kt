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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeRepository: PurgeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {

  companion object {
    private const val AUTHOR_WEBSITE_URL = "https://lucodivo.github.io/"
    private const val SOURCE_CODE_URL = "https://github.com/Lucodivo/Merlinsbag"
    private const val ECCOHEDRA_URL = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.learnopengl_androidport"
    private const val MERLINSBAG_URL = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.merlinsbag"
    private const val PRIVACY_INFO_URL = "https://lucodivo.github.io/merlinsbag_android_privacy_policy.html"
    private const val DEMO_VIDEO_URL = "https://www.youtube.com/watch?v=uUQYMU2N4kA"
  }

  enum class DropdownMenuState {
    None,
    DarkMode,
    ColorPalette,
    HighContrast,
    Typography,
    ImageQuality,
  }

  enum class AlertDialogState {
    None,
    DeleteAllData,
    ImageQuality,
  }

  data class PreferencesState(
      val darkMode: DarkMode,
      val colorPalette: ColorPalette,
      val highContrast: HighContrast,
      val imageQuality: ImageQuality,
      val typography: Typography,
  )

  sealed interface NavigationState {
    data object TipsAndInfo: NavigationState
    data object Statistics: NavigationState
    data class Web(val url: String): NavigationState
  }

  var cachePurged by mutableStateOf(Event<Unit>(null))
  var dataDeleted by mutableStateOf(Event<Unit>(null))
  var rateAndReviewRequest by mutableStateOf(Event<Unit>(null))
  var navigationEventState by mutableStateOf(Event<NavigationState>(null))
  var clearCacheEnabled by mutableStateOf(true)
  var highContrastEnabled by mutableStateOf(true)
  var dropdownMenuState by mutableStateOf(DropdownMenuState.None)
  var alertDialogState by mutableStateOf(AlertDialogState.None)
  val preferencesState = userPreferencesRepository.userPreferences.onEach {
    // System dynamic color schemes do not currently support high contrast
    if(it.colorPalette == ColorPalette.SYSTEM_DYNAMIC) {
      if(highContrastEnabled) highContrastEnabled = false
    } else if(!highContrastEnabled) highContrastEnabled = true
    cachedImageQuality = it.imageQuality
  }.map {
    PreferencesState(
      darkMode = it.darkMode,
      colorPalette = it.colorPalette,
      highContrast = if(it.colorPalette != ColorPalette.SYSTEM_DYNAMIC) it.highContrast else HighContrast.OFF,
      imageQuality = it.imageQuality,
      typography = it.typography,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
    initialValue = with(UserPreferences()) {
      PreferencesState(
        darkMode = darkMode,
        colorPalette = colorPalette,
        highContrast = highContrast,
        imageQuality = imageQuality,
        typography = typography,
      )
    },
  )

  private var cachedImageQuality: ImageQuality? = null
  private var selectedImageQuality: ImageQuality? = null

  private fun dismissDropdownMenu() {
    dropdownMenuState = DropdownMenuState.None
  }

  private fun dismissAlertDialog() {
    alertDialogState = AlertDialogState.None
  }

  fun onClickClearCache() {
    // Disable clearing cache until view model is recreated
    clearCacheEnabled = false
    viewModelScope.launch(Dispatchers.IO) {
      purgeRepository.purgeCache()
      cachePurged = Event(Unit)
    }
  }

  fun onClickDeleteAllData() {
    alertDialogState = AlertDialogState.DeleteAllData
  }

  fun onDismissDeleteAllDataAlertDialog() = dismissAlertDialog()
  fun onConfirmDeleteAllDataAlertDialog() {
    dismissAlertDialog()
    viewModelScope.launch(Dispatchers.IO) {
      purgeRepository.purgeUserData()
      dataDeleted = Event(Unit)
    }
  }


  fun onClickDarkMode() {
    dropdownMenuState = DropdownMenuState.DarkMode
  }

  fun onDismissDarkMode() = dismissDropdownMenu()
  fun setDarkMode(darkMode: DarkMode) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setDarkMode(darkMode)
    }
  }

  fun onClickColorPalette() {
    dropdownMenuState = DropdownMenuState.ColorPalette
  }

  fun onDismissColorPalette() = dismissDropdownMenu()
  fun setColorPalette(colorPalette: ColorPalette) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setColorPalette(colorPalette)
    }
  }

  fun onClickHighContrast() {
    dropdownMenuState = DropdownMenuState.HighContrast
  }

  fun onDismissHighContrast() = dismissDropdownMenu()
  fun setHighContrast(highContrast: HighContrast) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setHighContrast(highContrast)
    }
  }

  fun onClickTypography() {
    dropdownMenuState = DropdownMenuState.Typography
  }

  fun onDismissTypography() = dismissDropdownMenu()
  fun setTypography(typography: Typography) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setTypography(typography)
    }
  }

  private fun setImageQuality(imageQuality: ImageQuality) = viewModelScope.launch(Dispatchers.IO) {
    userPreferencesRepository.setImageQuality(imageQuality)
  }

  fun onClickImageQuality() {
    dropdownMenuState = DropdownMenuState.ImageQuality
  }

  fun onDismissImageQualityDropdown() = dismissDropdownMenu()
  fun onSelectedImageQuality(newImageQuality: ImageQuality) {
    dismissDropdownMenu()
    cachedImageQuality?.let { oldImageQuality ->
      if(oldImageQuality == newImageQuality) return
      val imageQualityRaised = oldImageQuality.ordinal < newImageQuality.ordinal
      if(imageQualityRaised) {
        selectedImageQuality = newImageQuality
        alertDialogState = AlertDialogState.ImageQuality
      } else {
        setImageQuality(newImageQuality)
      }
    }
  }

  fun onDismissImageQualityAlertDialog() = dismissAlertDialog()
  fun onConfirmImageQualityAlertDialog() {
    dismissAlertDialog()
    selectedImageQuality?.let { setImageQuality(it) }
    selectedImageQuality = null
  }

  fun onClickWelcome() = viewModelScope.launch(Dispatchers.IO) {
    userPreferencesRepository.setHasCompletedOnboarding(false)
  }

  fun onClickTipsAndInfo() {
    navigationEventState = Event(NavigationState.TipsAndInfo)
  }

  fun onClickStatistics() {
    navigationEventState = Event(NavigationState.Statistics)
  }

  fun onClickDemo() {
    navigationEventState = Event(NavigationState.Web(DEMO_VIDEO_URL))
  }

  fun onClickPrivacyInformation() {
    navigationEventState = Event(NavigationState.Web(PRIVACY_INFO_URL))
  }

  fun onClickDeveloper() {
    navigationEventState = Event(NavigationState.Web(AUTHOR_WEBSITE_URL))
  }

  fun onClickSource() {
    navigationEventState = Event(NavigationState.Web(SOURCE_CODE_URL))
  }

  fun onClickEccohedra() {
    navigationEventState = Event(NavigationState.Web(ECCOHEDRA_URL))
  }

  fun onClickRateAndReview() {
    rateAndReviewRequest = Event(Unit)
  }

  fun onUnableToDisplayInAppReview() {
    navigationEventState = Event(NavigationState.Web(MERLINSBAG_URL))
  }
}