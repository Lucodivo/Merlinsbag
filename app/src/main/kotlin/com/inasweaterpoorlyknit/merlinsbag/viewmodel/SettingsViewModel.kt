package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SettingsDropdownMenuState {
  None,
  DarkMode,
  ColorPalette,
  HighContrast,
  Typography,
  ImageQuality,
}

enum class SettingsAlertDialogState {
  None,
  DeleteAllData,
  ImageQuality,
}

sealed interface SettingsNavigationState {
  data object TipsAndInfo: SettingsNavigationState
  data object Statistics: SettingsNavigationState
  data class Web(val url: String): SettingsNavigationState
}

sealed interface SettingsEvent {
  data object ClickRateAndReview: SettingsEvent
  data object ClickStatistics: SettingsEvent
  data object ClickTipsAndInfo: SettingsEvent
  data object ClickDemo: SettingsEvent
  data object ClickSource: SettingsEvent
  data object ClickDeveloper: SettingsEvent
  data object ClickEccohedra: SettingsEvent
  data object ClickPrivacyInfo: SettingsEvent
  data object ClickWelcome: SettingsEvent
  data object ClickClearCache: SettingsEvent
  data object UnableToDisplayInAppReview: SettingsEvent
  data object ClickDeleteAllData: SettingsEvent
  data object ClickDismissDeleteAllDialog: SettingsEvent
  data object ClickConfirmDeleteAllDialog: SettingsEvent
  data object ClickDarkMode: SettingsEvent
  data object ClickDismissDarkMode: SettingsEvent
  data class SelectDarkMode(val darkMode: DarkMode): SettingsEvent
  data object ClickHighContrast: SettingsEvent
  data object ClickDismissHighContrast: SettingsEvent
  data class SelectHighContrast(val highContrast: HighContrast): SettingsEvent
  data object ClickTypography: SettingsEvent
  data object ClickDismissTypography: SettingsEvent
  data class SelectTypography(val typography: Typography): SettingsEvent
  data object ClickColorPalette: SettingsEvent
  data object ClickDismissColorPalette: SettingsEvent
  data class SelectColorPalette(val colorPalette: ColorPalette): SettingsEvent
  data object ClickImageQuality: SettingsEvent
  data object ClickDismissImageQuality: SettingsEvent
  data class SelectImageQuality(val imageQuality: ImageQuality): SettingsEvent
  data object ClickDismissImageQualityDialog: SettingsEvent
  data object ClickConfirmImageQualityDialog: SettingsEvent
}

data class SettingsUiState(
    val darkMode: DarkMode,
    val colorPalette: ColorPalette,
    val highContrast: HighContrast,
    val imageQuality: ImageQuality,
    val typography: Typography,
    var clearCacheEnabled: Boolean,
    var highContrastEnabled: Boolean,
    var dropdownMenuState: SettingsDropdownMenuState,
    var alertDialogState: SettingsAlertDialogState,
    var cachePurged: Event<Unit>,
    var dataDeleted: Event<Unit>,
    var rateAndReviewRequest: Event<Unit>,
    var navigationEventState: Event<SettingsNavigationState>,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeRepository: PurgeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {

  companion object {
    private const val TAG = "SettingsViewModel"
    private const val AUTHOR_WEBSITE_URL = "https://lucodivo.github.io/"
    private const val SOURCE_CODE_URL = "https://github.com/Lucodivo/Merlinsbag"
    private const val ECCOHEDRA_URL = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.learnopengl_androidport"
    private const val MERLINSBAG_URL = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.merlinsbag"
    private const val PRIVACY_INFO_URL = "https://lucodivo.github.io/merlinsbag_android_privacy_policy.html"
    private const val DEMO_VIDEO_URL = "https://www.youtube.com/watch?v=uUQYMU2N4kA"
  }

  private val events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 20)

  private fun setImageQuality(imageQuality: ImageQuality) = viewModelScope.launch(Dispatchers.IO) {
    userPreferencesRepository.setImageQuality(imageQuality)
  }

  fun onEvent(event: SettingsEvent) {
    if(!events.tryEmit(event)){
      Log.e(TAG, "Event buffer overflow")
    }
  }

  private var cachePurged by mutableStateOf(Event<Unit>(null))
  private var dataDeleted by mutableStateOf(Event<Unit>(null))
  private var rateAndReviewRequest by mutableStateOf(Event<Unit>(null))
  private var navigationEventState by mutableStateOf(Event<SettingsNavigationState>(null))
  private var clearCacheEnabled by mutableStateOf(true)
  private var dropdownMenuState by mutableStateOf(SettingsDropdownMenuState.None)
  private var alertDialogState by mutableStateOf(SettingsAlertDialogState.None)

  @Composable
  fun UiState(): SettingsUiState {
    val userPreferences by remember { userPreferencesRepository.userPreferences }.collectAsState(UserPreferences())

    var selectedImageQuality: ImageQuality? = remember { null }

    LaunchedEffect(Unit) {
      events.collect { event ->
        when(event){
          SettingsEvent.ClickDemo -> navigationEventState = Event(SettingsNavigationState.Web(DEMO_VIDEO_URL))
          SettingsEvent.ClickDeveloper -> navigationEventState = Event(SettingsNavigationState.Web(AUTHOR_WEBSITE_URL))
          SettingsEvent.ClickEccohedra -> navigationEventState = Event(SettingsNavigationState.Web(ECCOHEDRA_URL))
          SettingsEvent.ClickPrivacyInfo -> navigationEventState = Event(SettingsNavigationState.Web(PRIVACY_INFO_URL))
          SettingsEvent.ClickRateAndReview -> rateAndReviewRequest = Event(Unit)
          SettingsEvent.ClickSource -> navigationEventState = Event(SettingsNavigationState.Web(SOURCE_CODE_URL))
          SettingsEvent.ClickStatistics -> navigationEventState = Event(SettingsNavigationState.Statistics)
          SettingsEvent.ClickTipsAndInfo -> navigationEventState = Event(SettingsNavigationState.TipsAndInfo)
          SettingsEvent.UnableToDisplayInAppReview -> navigationEventState = Event(SettingsNavigationState.Web(MERLINSBAG_URL))
          SettingsEvent.ClickColorPalette -> dropdownMenuState = SettingsDropdownMenuState.ColorPalette
          SettingsEvent.ClickDarkMode -> dropdownMenuState = SettingsDropdownMenuState.DarkMode
          SettingsEvent.ClickDeleteAllData -> alertDialogState = SettingsAlertDialogState.DeleteAllData
          SettingsEvent.ClickDismissColorPalette -> dropdownMenuState = SettingsDropdownMenuState.None
          SettingsEvent.ClickDismissDarkMode -> dropdownMenuState = SettingsDropdownMenuState.None
          SettingsEvent.ClickDismissDeleteAllDialog -> alertDialogState = SettingsAlertDialogState.None
          SettingsEvent.ClickDismissHighContrast -> dropdownMenuState = SettingsDropdownMenuState.None
          SettingsEvent.ClickDismissImageQuality -> dropdownMenuState = SettingsDropdownMenuState.None
          SettingsEvent.ClickDismissImageQualityDialog -> alertDialogState = SettingsAlertDialogState.None
          SettingsEvent.ClickDismissTypography -> dropdownMenuState = SettingsDropdownMenuState.None
          SettingsEvent.ClickHighContrast -> dropdownMenuState = SettingsDropdownMenuState.HighContrast
          SettingsEvent.ClickImageQuality -> dropdownMenuState = SettingsDropdownMenuState.ImageQuality
          SettingsEvent.ClickTypography -> dropdownMenuState = SettingsDropdownMenuState.Typography
          SettingsEvent.ClickWelcome -> userPreferencesRepository.setHasCompletedOnboarding(false)
          SettingsEvent.ClickClearCache -> {
            // Disable clearing cache until view model is recreated
            clearCacheEnabled = false
            viewModelScope.launch(Dispatchers.IO) {
              purgeRepository.purgeCache()
              cachePurged = Event(Unit)
            }
          }
          SettingsEvent.ClickConfirmDeleteAllDialog -> {
            alertDialogState = SettingsAlertDialogState.None
            viewModelScope.launch(Dispatchers.IO) {
              purgeRepository.purgeUserData()
              dataDeleted = Event(Unit)
            }
          }
          SettingsEvent.ClickConfirmImageQualityDialog -> {
            alertDialogState = SettingsAlertDialogState.None
            selectedImageQuality?.let { setImageQuality(it) }
            selectedImageQuality = null
          }
          is SettingsEvent.SelectColorPalette -> {
            dropdownMenuState = SettingsDropdownMenuState.None
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setColorPalette(event.colorPalette)
            }
          }
          is SettingsEvent.SelectDarkMode -> {
            dropdownMenuState = SettingsDropdownMenuState.None
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setDarkMode(event.darkMode)
            }
          }
          is SettingsEvent.SelectHighContrast -> {
            dropdownMenuState = SettingsDropdownMenuState.None
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setHighContrast(event.highContrast)
            }
          }
          is SettingsEvent.SelectImageQuality -> {
            dropdownMenuState = SettingsDropdownMenuState.None
            userPreferences.imageQuality.let { oldImageQuality ->
              if(oldImageQuality != event.imageQuality) {
                val imageQualityRaised = oldImageQuality.ordinal < event.imageQuality.ordinal
                if(imageQualityRaised) {
                  selectedImageQuality = event.imageQuality
                  alertDialogState = SettingsAlertDialogState.ImageQuality
                } else {
                  setImageQuality(event.imageQuality)
                }
              }
            }
          }
          is SettingsEvent.SelectTypography -> {
            dropdownMenuState = SettingsDropdownMenuState.None
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setTypography(event.typography)
            }
          }
        }
      }
    }

    return SettingsUiState(
      darkMode = userPreferences.darkMode,
      colorPalette = userPreferences.colorPalette,
      highContrast = userPreferences.highContrast,
      imageQuality = userPreferences.imageQuality,
      typography = userPreferences.typography,
      clearCacheEnabled = clearCacheEnabled,
      highContrastEnabled = userPreferences.colorPalette != ColorPalette.SYSTEM_DYNAMIC,
      dropdownMenuState = dropdownMenuState,
      alertDialogState = alertDialogState,
      cachePurged = cachePurged,
      dataDeleted = dataDeleted,
      rateAndReviewRequest = rateAndReviewRequest,
      navigationEventState = navigationEventState,
    )
  }
}