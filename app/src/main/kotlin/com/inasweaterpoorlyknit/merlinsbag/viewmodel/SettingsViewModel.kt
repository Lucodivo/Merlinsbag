package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.inasweaterpoorlyknit.core.data.repository.PurgeRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.HighContrast
import com.inasweaterpoorlyknit.core.model.ImageQuality
import com.inasweaterpoorlyknit.core.model.Typography
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.merlinsbag.Constants.WebUrls
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIState.AlertDialogState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIState.DropdownMenuState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIEvent.*
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIEffect.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUIState (
    val clearCacheEnabled: Boolean,
    val highContrastEnabled: Boolean,
    val dropdownMenu: DropdownMenuState,
    val alertDialog: AlertDialogState,
    val darkMode: DarkMode,
    val colorPalette: ColorPalette,
    val highContrast: HighContrast,
    val imageQuality: ImageQuality,
    val typography: Typography,
){
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
}

sealed interface SettingsUIEffect {
  data object CachePurged: SettingsUIEffect
  data object AllDataDeleted: SettingsUIEffect
  data object RateAndReviewRequest: SettingsUIEffect
  data object NavigateToTipsAndInfo: SettingsUIEffect
  data object NavigateToStatistics: SettingsUIEffect
  data class NavigateToWeb(val url: String): SettingsUIEffect
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

@HiltViewModel
class SettingsViewModel @Inject constructor(settingsPresenter: SettingsUIStateManager)
  : MoleculeViewModel<SettingsUIEvent, SettingsUIState, SettingsUIEffect>(uiStateManager = settingsPresenter)

class SettingsUIStateManager @Inject constructor(
  val purgeRepository: PurgeRepository,
  val userPreferencesRepository: UserPreferencesRepository,
): ComposeUIStateManager<SettingsUIEvent, SettingsUIState, SettingsUIEffect> {
  override var cachedState = with(UserPreferences()) {
    SettingsUIState(
      clearCacheEnabled = true,
      highContrastEnabled = true,
      dropdownMenu = DropdownMenuState.None,
      alertDialog = AlertDialogState.None,
      darkMode = darkMode,
      colorPalette = colorPalette,
      highContrast = highContrast,
      imageQuality = imageQuality,
      typography = typography,
    )
  }

  private var selectedImageQuality: ImageQuality? = null

  @Composable
  override fun uiState(
      uiEvents: Flow<SettingsUIEvent>,
      launchUiEffect: (SettingsUIEffect) -> Unit
  ): SettingsUIState {
    var clearCacheEnabled by remember { mutableStateOf(cachedState.clearCacheEnabled) }
    var dropdownMenu by remember { mutableStateOf(cachedState.dropdownMenu) }
    var alertDialog by remember { mutableStateOf(cachedState.alertDialog) }
    val userPreferences by remember { userPreferencesRepository.userPreferences }.collectAsState(
      UserPreferences(
        darkMode = cachedState.darkMode,
        colorPalette = cachedState.colorPalette,
        highContrast = cachedState.highContrast,
        imageQuality = cachedState.imageQuality,
        typography = cachedState.typography,
      )
    )

    fun dismissDropdownMenu() { dropdownMenu = DropdownMenuState.None }
    fun dismissAlertDialog() { alertDialog = AlertDialogState.None }
    LaunchedEffect(Unit) {
      uiEvents.collect { uiEvent ->
        when(uiEvent) {
          ClickColorPalette -> dropdownMenu = DropdownMenuState.ColorPalette
          ClickDarkMode -> dropdownMenu = DropdownMenuState.DarkMode
          ClickHighContrast -> dropdownMenu = DropdownMenuState.HighContrast
          ClickImageQuality -> dropdownMenu = DropdownMenuState.ImageQuality
          ClickTypography -> dropdownMenu = DropdownMenuState.Typography
          ClickDeleteAllData -> alertDialog = AlertDialogState.DeleteAllData
          DismissColorPalette -> dismissDropdownMenu()
          DismissDarkMode -> dismissDropdownMenu()
          DismissHighContrast -> dismissDropdownMenu()
          DismissImageQualityDropdown -> dismissDropdownMenu()
          DismissTypography -> dismissDropdownMenu()
          DismissImageQualityAlertDialog -> dismissAlertDialog()
          DismissDeleteAllDataAlertDialog -> dismissAlertDialog()
          ClickWelcome -> launch(Dispatchers.IO) { userPreferencesRepository.setHasCompletedOnboarding(false) }
          ClickDemo -> launchUiEffect(NavigateToWeb(WebUrls.DEMO_VIDEO))
          ClickDeveloper -> launchUiEffect(NavigateToWeb(WebUrls.AUTHOR))
          ClickEccohedra -> launchUiEffect(NavigateToWeb(WebUrls.ECCOHEDRA))
          ClickPrivacyInformation -> launchUiEffect(NavigateToWeb(WebUrls.PRIVACY_POLICY))
          UnableToDisplayInAppReview -> launchUiEffect(NavigateToWeb(WebUrls.MERLINSBAG))
          ClickSource -> launchUiEffect(NavigateToWeb(WebUrls.SOURCE_CODE))
          ClickStatistics -> launchUiEffect(NavigateToStatistics)
          ClickTipsAndInfo -> launchUiEffect(NavigateToTipsAndInfo)
          ClickRateAndReview -> launchUiEffect(RateAndReviewRequest)
          ClickClearCache -> {
            // Disable clearing cache until state is recreated
            clearCacheEnabled = false
            launch(Dispatchers.IO) {
              purgeRepository.purgeCache()
              launchUiEffect(CachePurged)
            }
          }
          ConfirmDeleteAllDataAlertDialog -> {
            dismissAlertDialog()
            launch(Dispatchers.IO) {
              purgeRepository.purgeUserData()
              launchUiEffect(AllDataDeleted)
            }
          }
          ConfirmImageQualityAlertDialog -> {
            dismissAlertDialog()
            selectedImageQuality?.let {
              launch(Dispatchers.IO) {
                userPreferencesRepository.setImageQuality(it)
              }
            }
            selectedImageQuality = null
          }
          is SelectColorPalette -> {
            dismissDropdownMenu()
            launch(Dispatchers.IO) {
              userPreferencesRepository.setColorPalette(uiEvent.colorPalette)
            }
          }
          is SelectDarkMode -> {
            dismissDropdownMenu()
            launch(Dispatchers.IO) {
              userPreferencesRepository.setDarkMode(uiEvent.darkMode)
            }
          }
          is SelectHighContrast -> {
            dismissDropdownMenu()
            launch(Dispatchers.IO) {
              userPreferencesRepository.setHighContrast(uiEvent.highContrast)
            }
          }
          is SelectTypography -> {
            dismissDropdownMenu()
            launch(Dispatchers.IO) {
              userPreferencesRepository.setTypography(uiEvent.typography)
            }
          }
          is SelectedImageQuality -> {
            val newImageQuality = uiEvent.newImageQuality
            dismissDropdownMenu()
            if(userPreferences.imageQuality != newImageQuality) {
              val imageQualityRaised = userPreferences.imageQuality.ordinal < newImageQuality.ordinal
              if(imageQualityRaised) {
                selectedImageQuality = newImageQuality
                alertDialog = AlertDialogState.ImageQuality
              } else {
                launch(Dispatchers.IO) {
                  userPreferencesRepository.setImageQuality(newImageQuality)
                }
              }
            }
          }
        }
      }
    }

    val highContrastEnabled = userPreferences.colorPalette != ColorPalette.SYSTEM_DYNAMIC
    cachedState = SettingsUIState(
      clearCacheEnabled = clearCacheEnabled,
      dropdownMenu = dropdownMenu,
      alertDialog = alertDialog,
      highContrastEnabled = highContrastEnabled,
      darkMode = userPreferences.darkMode,
      colorPalette = userPreferences.colorPalette,
      typography = userPreferences.typography,
      imageQuality = userPreferences.imageQuality,
      highContrast = if(highContrastEnabled) userPreferences.highContrast else HighContrast.OFF,
    )
    return cachedState
  }
}