package com.inasweaterpoorlyknit.merlinsbag.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
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

sealed interface SettingsNavigationState {
  data object TipsAndInfo: SettingsNavigationState
  data object Statistics: SettingsNavigationState
  data class Web(val url: String): SettingsNavigationState
}

data class SettingsUIState (
    val cachePurged: Event<Unit>,
    val dataDeleted: Event<Unit>,
    val rateAndReviewRequest: Event<Unit>,
    val navigationEventState: Event<SettingsNavigationState>,
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

interface SettingsUIStateChanger {
  fun onClickClearCache()
  fun onClickDeleteAllData()
  fun onDismissDeleteAllDataAlertDialog()
  fun onConfirmDeleteAllDataAlertDialog()
  fun onClickDarkMode()
  fun onDismissDarkMode()
  fun onSelectDarkMode(darkMode: DarkMode)
  fun onClickColorPalette()
  fun onDismissColorPalette()
  fun onSelectColorPalette(colorPalette: ColorPalette)
  fun onClickHighContrast()
  fun onDismissHighContrast()
  fun onSelectHighContrast(highContrast: HighContrast)
  fun onClickTypography()
  fun onDismissTypography()
  fun onSelectTypography(typography: Typography)
  fun onClickImageQuality()
  fun onDismissImageQualityDropdown()
  fun onSelectedImageQuality(newImageQuality: ImageQuality)
  fun onDismissImageQualityAlertDialog()
  fun onConfirmImageQualityAlertDialog()
  fun onClickWelcome()
  fun onClickRateAndReview()
  fun onClickStatistics()
  fun onClickTipsAndInfo()
  fun onClickDemo()
  fun onClickSource()
  fun onClickDeveloper()
  fun onClickEccohedra()
  fun onClickPrivacyInformation()
  fun onUnableToDisplayInAppReview()
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
): ViewModel(), SettingsUIStateChanger {

  private data class LocallyManagedState (
      val cachePurged: Event<Unit> = Event(null),
      val dataDeleted: Event<Unit> = Event(null),
      val rateAndReviewRequest: Event<Unit> = Event(null),
      val navigationEventState: Event<SettingsNavigationState> = Event(null),
      val clearCacheEnabled: Boolean = true,
      val dropdownMenuState: SettingsDropdownMenuState = SettingsDropdownMenuState.None,
      val alertDialogState: SettingsAlertDialogState = SettingsAlertDialogState.None,
  )
  private val locallyManagedState = MutableStateFlow(LocallyManagedState())

  private fun highContrastIsEnabled(colorPalette: ColorPalette) = colorPalette != ColorPalette.SYSTEM_DYNAMIC

  val uiState: StateFlow<SettingsUIState> = combine(
    userPreferencesRepository.userPreferences,
    locallyManagedState,
  ) { userPreferences, locallyManagedState ->
    // System dynamic color schemes do not currently support high contrast
    val highContrastEnabled = highContrastIsEnabled(userPreferences.colorPalette)
    cachedImageQuality = userPreferences.imageQuality
    SettingsUIState(
      cachePurged = locallyManagedState.cachePurged,
      dataDeleted = locallyManagedState.dataDeleted,
      rateAndReviewRequest = locallyManagedState.rateAndReviewRequest,
      navigationEventState = locallyManagedState.navigationEventState,
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
        cachePurged = cachePurged,
        dataDeleted = dataDeleted,
        rateAndReviewRequest = rateAndReviewRequest,
        navigationEventState = navigationEventState,
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

  private var cachedImageQuality: ImageQuality? = null
  private var selectedImageQuality: ImageQuality? = null

  private fun dismissDropdownMenu() {
    locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.None)
  }

  private fun dismissAlertDialog() {
    locallyManagedState.value = locallyManagedState.value.copy(alertDialogState = SettingsAlertDialogState.None)
  }

  private fun setImageQuality(imageQuality: ImageQuality) = viewModelScope.launch(Dispatchers.IO) {
    userPreferencesRepository.setImageQuality(imageQuality)
  }

  override fun onClickClearCache() {
    // Disable clearing cache until view model is recreated
    locallyManagedState.value = locallyManagedState.value.copy(clearCacheEnabled = false)
    viewModelScope.launch(Dispatchers.IO) {
      purgeRepository.purgeCache()
      locallyManagedState.value = locallyManagedState.value.copy(cachePurged = Event(Unit))
    }
  }

  override fun onClickDeleteAllData() {
    locallyManagedState.value = locallyManagedState.value.copy(alertDialogState = SettingsAlertDialogState.DeleteAllData)
  }

  override fun onDismissDeleteAllDataAlertDialog() = dismissAlertDialog()
  override fun onConfirmDeleteAllDataAlertDialog() {
    dismissAlertDialog()
    viewModelScope.launch(Dispatchers.IO) {
      purgeRepository.purgeUserData()
      locallyManagedState.value = locallyManagedState.value.copy(dataDeleted = Event(Unit))
    }
  }


  override fun onClickDarkMode() {
    locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.DarkMode)
  }

  override fun onDismissDarkMode() = dismissDropdownMenu()
  override fun onSelectDarkMode(darkMode: DarkMode) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setDarkMode(darkMode)
    }
  }

  override fun onClickColorPalette() {
    locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.ColorPalette)
  }

  override fun onDismissColorPalette() = dismissDropdownMenu()
  override fun onSelectColorPalette(colorPalette: ColorPalette) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setColorPalette(colorPalette)
    }
  }

  override fun onClickHighContrast() {
    locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.HighContrast)
  }

  override fun onDismissHighContrast() = dismissDropdownMenu()
  override fun onSelectHighContrast(highContrast: HighContrast) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setHighContrast(highContrast)
    }
  }

  override fun onClickTypography() {
    locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.Typography)
  }

  override fun onDismissTypography() = dismissDropdownMenu()
  override fun onSelectTypography(typography: Typography) {
    dismissDropdownMenu()
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setTypography(typography)
    }
  }

  override fun onClickImageQuality() {
    locallyManagedState.value = locallyManagedState.value.copy(dropdownMenuState = SettingsDropdownMenuState.ImageQuality)
  }

  override fun onDismissImageQualityDropdown() = dismissDropdownMenu()
  override fun onSelectedImageQuality(newImageQuality: ImageQuality) {
    dismissDropdownMenu()
    cachedImageQuality?.let { oldImageQuality ->
      if(oldImageQuality == newImageQuality) return
      val imageQualityRaised = oldImageQuality.ordinal < newImageQuality.ordinal
      if(imageQualityRaised) {
        selectedImageQuality = newImageQuality
        locallyManagedState.value = locallyManagedState.value.copy(alertDialogState = SettingsAlertDialogState.ImageQuality)
      } else {
        setImageQuality(newImageQuality)
      }
    }
  }

  override fun onDismissImageQualityAlertDialog() = dismissAlertDialog()
  override fun onConfirmImageQualityAlertDialog() {
    dismissAlertDialog()
    selectedImageQuality?.let { setImageQuality(it) }
    selectedImageQuality = null
  }

  override fun onClickWelcome() {
    viewModelScope.launch(Dispatchers.IO) {
      userPreferencesRepository.setHasCompletedOnboarding(false)
    }
  }

  override fun onClickRateAndReview() { locallyManagedState.value = locallyManagedState.value.copy(rateAndReviewRequest = Event(Unit)) }
  override fun onClickStatistics() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.Statistics)) }
  override fun onClickTipsAndInfo() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.TipsAndInfo)) }
  override fun onClickDemo() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.Web(WebUrls.DEMO_VIDEO))) }
  override fun onClickSource() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.Web(WebUrls.SOURCE_CODE))) }
  override fun onClickDeveloper() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.Web(WebUrls.AUTHOR))) }
  override fun onClickEccohedra() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.Web(WebUrls.ECCOHEDRA))) }
  override fun onClickPrivacyInformation() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.Web(WebUrls.PRIVACY_POLICY))) }
  override fun onUnableToDisplayInAppReview() { locallyManagedState.value = locallyManagedState.value.copy(navigationEventState = Event(SettingsNavigationState.Web(WebUrls.MERLINSBAG))) }
}