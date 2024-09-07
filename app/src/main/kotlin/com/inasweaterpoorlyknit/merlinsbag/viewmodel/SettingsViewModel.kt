package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
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

abstract class MoleculeViewModel<UIEvent, UIState, UIEffect>(
  initialState: UIState,
): ViewModel() {
  private val uiScope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

  private val _uiEvents = MutableSharedFlow<UIEvent>(extraBufferCapacity = 20)
  private val _uiEffects = MutableSharedFlow<UIEffect>(extraBufferCapacity = 20)

  private var cachedUiState: UIState = initialState

  val uiEffect: SharedFlow<UIEffect> = _uiEffects

  val uiState: StateFlow<UIState> by lazy(LazyThreadSafetyMode.NONE) {
    moleculeFlow(mode = RecompositionMode.ContextClock) {
      uiState(cachedUiState, _uiEvents, ::launchUiEffect)
    }.onEach { uiState ->
      cachedUiState = uiState
    }.stateIn(
      scope = uiScope,
      started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
      initialValue = cachedUiState,
    )
  }

  @Composable
  protected abstract fun uiState(
      initialState: UIState,
      uiEvents: Flow<UIEvent>,
      launchUiEffect: (UIEffect) -> Unit,
  ): UIState

  fun onUiEvent(uiEvent: UIEvent) {
    if(!_uiEvents.tryEmit(uiEvent)) {
      error("SettingsViewModel: UI event buffer overflow.")
    }
  }

  private fun launchUiEffect(uiEffect: UIEffect){
    if(!_uiEffects.tryEmit(uiEffect)) {
      error("SettingsViewModel: UI effect buffer overflow.")
    }
  }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val purgeRepository: PurgeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): MoleculeViewModel<SettingsUIEvent, SettingsUIState, SettingsUIEffect>(
  initialState = with(UserPreferences()){
    SettingsUIState(
      clearCacheEnabled = true,
      highContrastEnabled = true,
      dropdownMenuState = SettingsDropdownMenuState.None,
      alertDialogState = SettingsAlertDialogState.None,
      darkMode = darkMode,
      colorPalette = colorPalette,
      highContrast = highContrast,
      imageQuality = imageQuality,
      typography = typography,
    )
  }
) {
  @Composable
  override fun uiState(
      initialState: SettingsUIState,
      uiEvents: Flow<SettingsUIEvent>,
      launchUiEffect: (SettingsUIEffect) -> Unit,
  ): SettingsUIState =
    settingsUIState(
      initialState = initialState,
      uiEvents = uiEvents,
      launchUiEffect = launchUiEffect,
      purgeRepository = purgeRepository,
      userPreferencesRepository = userPreferencesRepository
    )
}

@Composable
fun settingsUIState(
    initialState: SettingsUIState,
    uiEvents: Flow<SettingsUIEvent>,
    launchUiEffect: (SettingsUIEffect) -> Unit,
    purgeRepository: PurgeRepository,
    userPreferencesRepository: UserPreferencesRepository,
): SettingsUIState {
  var clearCacheEnabled by remember { mutableStateOf(initialState.clearCacheEnabled) }
  var dropdownMenuState by remember { mutableStateOf(initialState.dropdownMenuState) }
  var alertDialogState by remember { mutableStateOf(initialState.alertDialogState) }
  val userPreferences by remember { userPreferencesRepository.userPreferences }.collectAsState(
    UserPreferences().copy(
      darkMode = initialState.darkMode,
      colorPalette = initialState.colorPalette,
      highContrast = initialState.highContrast,
      imageQuality = initialState.imageQuality,
      typography = initialState.typography,
    )
  )
  var selectedImageQuality by remember { mutableStateOf<ImageQuality?>(null) }

  fun dismissDropdownMenu() { dropdownMenuState = SettingsDropdownMenuState.None }
  fun dismissAlertDialog() { alertDialogState = SettingsAlertDialogState.None }

  LaunchedEffect(Unit) {
    fun setImageQuality(imageQuality: ImageQuality) = launch(Dispatchers.IO) {
      userPreferencesRepository.setImageQuality(imageQuality)
    }
    uiEvents.collect { uiEvent ->
      when(uiEvent) {
        SettingsUIEvent.ClickClearCache -> {
          // Disable clearing cache until view model is recreated
          clearCacheEnabled = false
          launch(Dispatchers.IO) {
            purgeRepository.purgeCache()
            launchUiEffect(SettingsUIEffect.CachePurged)
          }
        }
        SettingsUIEvent.ClickColorPalette -> dropdownMenuState = SettingsDropdownMenuState.ColorPalette
        SettingsUIEvent.ClickDarkMode -> dropdownMenuState = SettingsDropdownMenuState.DarkMode
        SettingsUIEvent.ClickDeleteAllData -> alertDialogState = SettingsAlertDialogState.DeleteAllData
        SettingsUIEvent.ClickHighContrast -> dropdownMenuState = SettingsDropdownMenuState.HighContrast
        SettingsUIEvent.ClickImageQuality -> dropdownMenuState = SettingsDropdownMenuState.ImageQuality
        SettingsUIEvent.ClickTypography -> dropdownMenuState = SettingsDropdownMenuState.Typography
        SettingsUIEvent.ClickWelcome -> launch(Dispatchers.IO) {
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
          launch(Dispatchers.IO) {
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
          launch(Dispatchers.IO) {
            userPreferencesRepository.setColorPalette(uiEvent.colorPalette)
          }
        }
        is SettingsUIEvent.SelectDarkMode -> {
          dismissDropdownMenu()
          launch(Dispatchers.IO) {
            userPreferencesRepository.setDarkMode(uiEvent.darkMode)
          }
        }
        is SettingsUIEvent.SelectHighContrast -> {
          dismissDropdownMenu()
          launch(Dispatchers.IO) {
            userPreferencesRepository.setHighContrast(uiEvent.highContrast)
          }
        }
        is SettingsUIEvent.SelectTypography -> {
          dismissDropdownMenu()
          launch(Dispatchers.IO) {
            userPreferencesRepository.setTypography(uiEvent.typography)
          }
        }
        is SettingsUIEvent.SelectedImageQuality -> {
          val newImageQuality = uiEvent.newImageQuality
          dismissDropdownMenu()
          if(userPreferences.imageQuality != newImageQuality) {
            val imageQualityRaised = userPreferences.imageQuality.ordinal < newImageQuality.ordinal
            if(imageQualityRaised) {
              selectedImageQuality = newImageQuality
              alertDialogState = SettingsAlertDialogState.ImageQuality
            } else {
              setImageQuality(newImageQuality)
            }
          }
        }
      }
    }
  }

  val highContrastEnabled = userPreferences.colorPalette != ColorPalette.SYSTEM_DYNAMIC
  return SettingsUIState(
    clearCacheEnabled = clearCacheEnabled,
    dropdownMenuState = dropdownMenuState,
    alertDialogState = alertDialogState,
    highContrastEnabled = highContrastEnabled,
    darkMode = userPreferences.darkMode,
    colorPalette = userPreferences.colorPalette,
    typography = userPreferences.typography,
    imageQuality = userPreferences.imageQuality,
    highContrast = if(highContrastEnabled) userPreferences.highContrast else HighContrast.OFF,
  )
}