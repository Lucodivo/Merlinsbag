package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

data class SettingsUIState (
    val clearCacheEnabled: Boolean,
    val highContrastEnabled: Boolean,
    val dropdownMenuState: SettingsDropdownMenuState,
    val alertDialogState: SettingsAlertDialogState,
    val darkMode: DarkMode,
    val colorPalette: ColorPalette,
    val highContrast: HighContrast,
    val imageQuality: ImageQuality,
    val typography: Typography,
)

sealed interface SettingsUIEffect {
  sealed interface NavigationDestination {
    data object TipsAndInfo: NavigationDestination
    data object Statistics: NavigationDestination
    data class Web(val url: String): NavigationDestination
  }
  data object CachePurged: SettingsUIEffect
  data object AllDataDeleted: SettingsUIEffect
  data object RateAndReviewRequest: SettingsUIEffect
  data class Navigation(val dest: NavigationDestination): SettingsUIEffect
}

sealed interface SettingsUIEvent {
  data object ClickClearCache: SettingsUIEvent
  data object ClickDeleteAllData: SettingsUIEvent
  data object DismissDeleteAllDataAlertDialog: SettingsUIEvent
  data object ConfirmDeleteAllDataAlertDialog: SettingsUIEvent
  data object ClickDarkMode: SettingsUIEvent
  data object DismissDarkMode: SettingsUIEvent
  data class SelectDarkMode(val darkMode: DarkMode): SettingsUIEvent
  data object ClickColorPalette: SettingsUIEvent
  data object DismissColorPalette: SettingsUIEvent
  data class SelectColorPalette(val colorPalette: ColorPalette): SettingsUIEvent
  data object ClickHighContrast: SettingsUIEvent
  data object DismissHighContrast: SettingsUIEvent
  data class SelectHighContrast(val highContrast: HighContrast): SettingsUIEvent
  data object ClickTypography: SettingsUIEvent
  data object DismissTypography: SettingsUIEvent
  data class SelectTypography(val typography: Typography): SettingsUIEvent
  data object ClickImageQuality: SettingsUIEvent
  data object DismissImageQualityDropdown: SettingsUIEvent
  data class SelectedImageQuality(val newImageQuality: ImageQuality): SettingsUIEvent
  data object DismissImageQualityAlertDialog: SettingsUIEvent
  data object ConfirmImageQualityAlertDialog: SettingsUIEvent
  data object ClickWelcome: SettingsUIEvent
  data object ClickRateAndReview: SettingsUIEvent
  data object ClickStatistics: SettingsUIEvent
  data object ClickTipsAndInfo: SettingsUIEvent
  data object ClickDemo: SettingsUIEvent
  data object ClickSource: SettingsUIEvent
  data object ClickDeveloper: SettingsUIEvent
  data object ClickEccohedra: SettingsUIEvent
  data object ClickPrivacyInformation: SettingsUIEvent
  data object UnableToDisplayInAppReview: SettingsUIEvent
}

object WebUrls {
  const val AUTHOR = "https://lucodivo.github.io/"
  const val SOURCE_CODE = "https://github.com/Lucodivo/Merlinsbag"
  const val ECCOHEDRA = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.learnopengl_androidport"
  const val MERLINSBAG = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.merlinsbag"
  const val PRIVACY_POLICY = "https://lucodivo.github.io/merlinsbag_android_privacy_policy.html"
  const val DEMO_VIDEO = "https://www.youtube.com/watch?v=uUQYMU2N4kA"
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeRepository: PurgeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {

  private data class LocallyManagedState (
      val clearCacheEnabled: Boolean = true,
      val dropdownMenuState: SettingsDropdownMenuState = SettingsDropdownMenuState.None,
      val alertDialogState: SettingsAlertDialogState = SettingsAlertDialogState.None,
  )
  private val locallyManagedState = MutableStateFlow(LocallyManagedState())

  private var cachedImageQuality: ImageQuality? = null
  private var selectedImageQuality: ImageQuality? = null
  private val _uiEvent = MutableSharedFlow<SettingsUIEvent>(extraBufferCapacity = 20)
  private val _uiEffect = MutableSharedFlow<SettingsUIEffect>(extraBufferCapacity = 20)

  val uiEffect: SharedFlow<SettingsUIEffect> = _uiEffect
  val uiState: StateFlow<SettingsUIState> = combine(
    userPreferencesRepository.userPreferences,
    locallyManagedState,
  ) { userPreferences, locallyManagedState ->
    // System dynamic color schemes do not currently support high contrast
    val highContrastEnabled = highContrastIsEnabled(userPreferences.colorPalette)
    cachedImageQuality = userPreferences.imageQuality
    SettingsUIState(
      clearCacheEnabled = locallyManagedState.clearCacheEnabled,
      dropdownMenuState = locallyManagedState.dropdownMenuState,
      alertDialogState = locallyManagedState.alertDialogState,
      highContrastEnabled = highContrastEnabled,
      darkMode = userPreferences.darkMode,
      colorPalette = userPreferences.colorPalette,
      highContrast = if(highContrastEnabled) userPreferences.highContrast else HighContrast.OFF,
      imageQuality = userPreferences.imageQuality,
      typography = userPreferences.typography,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
    initialValue = with(locallyManagedState.value){
      val defaultPreferences = UserPreferences()
      SettingsUIState(
        clearCacheEnabled = clearCacheEnabled,
        dropdownMenuState = dropdownMenuState,
        alertDialogState = alertDialogState,
        highContrastEnabled = highContrastIsEnabled(defaultPreferences.colorPalette),
        darkMode = defaultPreferences.darkMode,
        colorPalette = defaultPreferences.colorPalette,
        highContrast = defaultPreferences.highContrast,
        imageQuality = defaultPreferences.imageQuality,
        typography = defaultPreferences.typography,
      )
    },
  )

  fun onEvent(interaction: SettingsUIEvent) {
    if(!_uiEvent.tryEmit(interaction)) {
      error("SettingsViewModel: UI event buffer overflow.")
    }
  }

  init {
    viewModelScope.launch {
      _uiEvent.collect { interaction ->
        when(interaction) {
          SettingsUIEvent.ClickClearCache -> {
            // Disable clearing cache until view model is recreated
            locallyManagedState.value = locallyManagedState.value.copy(clearCacheEnabled = false)
            viewModelScope.launch(Dispatchers.IO) {
              purgeRepository.purgeCache()
              launchUiEffect(SettingsUIEffect.CachePurged)
            }
          }
          SettingsUIEvent.ClickColorPalette -> {
            locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.ColorPalette)
          }
          SettingsUIEvent.ClickDarkMode -> {
            locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.DarkMode)
          }
          SettingsUIEvent.ClickDeleteAllData -> {
            locallyManagedState.value = locallyManagedState.value.copy(alertDialogState = SettingsAlertDialogState.DeleteAllData)
          }
          SettingsUIEvent.ClickHighContrast -> {
            locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.HighContrast)
          }
          SettingsUIEvent.ClickImageQuality -> {
            locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.ImageQuality)
          }
          SettingsUIEvent.ClickTypography -> {
            locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.Typography)
          }
          SettingsUIEvent.ClickWelcome -> viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.setHasCompletedOnboarding(false)
          }
          SettingsUIEvent.ClickDemo -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.Web(WebUrls.DEMO_VIDEO)))
          SettingsUIEvent.ClickDeveloper -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.Web(WebUrls.AUTHOR)))
          SettingsUIEvent.ClickEccohedra -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.Web(WebUrls.ECCOHEDRA)))
          SettingsUIEvent.ClickPrivacyInformation -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.Web(WebUrls.PRIVACY_POLICY)))
          SettingsUIEvent.ClickRateAndReview -> launchUiEffect(SettingsUIEffect.RateAndReviewRequest)
          SettingsUIEvent.ClickSource -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.Web(WebUrls.SOURCE_CODE)))
          SettingsUIEvent.ClickStatistics -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.Statistics))
          SettingsUIEvent.ClickTipsAndInfo -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.TipsAndInfo))
          SettingsUIEvent.UnableToDisplayInAppReview -> launchUiEffect(SettingsUIEffect.Navigation(SettingsUIEffect.NavigationDestination.Web(WebUrls.MERLINSBAG)))
          SettingsUIEvent.ConfirmDeleteAllDataAlertDialog -> {
            dismissAlertDialog()
            viewModelScope.launch(Dispatchers.IO) {
              purgeRepository.purgeUserData()
              launchUiEffect(SettingsUIEffect.AllDataDeleted)
            }
          }
          SettingsUIEvent.ConfirmImageQualityAlertDialog -> {
            dismissAlertDialog()
            selectedImageQuality?.let { setImageQuality(it) }
            selectedImageQuality = null
          }
          SettingsUIEvent.DismissColorPalette -> dismissDropdownMenu()
          SettingsUIEvent.DismissDarkMode -> dismissDropdownMenu()
          SettingsUIEvent.DismissHighContrast -> dismissDropdownMenu()
          SettingsUIEvent.DismissImageQualityDropdown -> dismissDropdownMenu()
          SettingsUIEvent.DismissTypography -> dismissDropdownMenu()
          SettingsUIEvent.DismissImageQualityAlertDialog -> dismissAlertDialog()
          SettingsUIEvent.DismissDeleteAllDataAlertDialog -> dismissAlertDialog()
          is SettingsUIEvent.SelectColorPalette -> {
            dismissDropdownMenu()
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setColorPalette(interaction.colorPalette)
            }
          }
          is SettingsUIEvent.SelectDarkMode -> {
            dismissDropdownMenu()
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setDarkMode(interaction.darkMode)
            }
          }
          is SettingsUIEvent.SelectHighContrast -> {
            dismissDropdownMenu()
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setHighContrast(interaction.highContrast)
            }
          }
          is SettingsUIEvent.SelectTypography -> {
            dismissDropdownMenu()
            viewModelScope.launch(Dispatchers.IO) {
              userPreferencesRepository.setTypography(interaction.typography)
            }
          }
          is SettingsUIEvent.SelectedImageQuality -> {
            val newImageQuality = interaction.newImageQuality
            dismissDropdownMenu()
            cachedImageQuality?.let { oldImageQuality ->
              if(oldImageQuality != newImageQuality) {
                val imageQualityRaised = oldImageQuality.ordinal < newImageQuality.ordinal
                if(imageQualityRaised) {
                  selectedImageQuality = newImageQuality
                  locallyManagedState.value = locallyManagedState.value.copy(alertDialogState = SettingsAlertDialogState.ImageQuality)
                } else {
                  setImageQuality(newImageQuality)
                }
              }
            }
          }
        }
      }
    }
  }

  private fun highContrastIsEnabled(colorPalette: ColorPalette) = colorPalette != ColorPalette.SYSTEM_DYNAMIC
  private fun dismissDropdownMenu() {
    locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.None)
  }
  private fun dismissAlertDialog() {
    locallyManagedState.value = locallyManagedState.value.copy(alertDialogState = SettingsAlertDialogState.None)
  }
  private fun setImageQuality(imageQuality: ImageQuality) = viewModelScope.launch(Dispatchers.IO) {
    userPreferencesRepository.setImageQuality(imageQuality)
  }
  private fun launchUiEffect(uiEffect: SettingsUIEffect){
    if(!_uiEffect.tryEmit(uiEffect)) {
      error("SettingsViewModel: UI effect buffer overflow.")
    }
  }
}