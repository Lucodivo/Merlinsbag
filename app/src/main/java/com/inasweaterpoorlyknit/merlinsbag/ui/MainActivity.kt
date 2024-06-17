package com.inasweaterpoorlyknit.merlinsbag.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.MainActivityUiState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

  val mainActivityViewModel: MainActivityViewModel by viewModels()

  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    val splashscreen = installSplashScreen()
    super.onCreate(savedInstanceState)

    var loading = true
    splashscreen.setKeepOnScreenCondition { loading }

    setContent {
      val appState = rememberNoopAppState(
        windowSizeClass = calculateWindowSizeClass(this)
      )
      val uiState by mainActivityViewModel.uiState.collectAsStateWithLifecycle()
      val userPreferences by mainActivityViewModel.userPreferences.collectAsStateWithLifecycle()
      if(loading && uiState !is MainActivityUiState.Loading) loading = false
      NoopTheme(
        darkMode = userPreferences.darkMode,
        colorPalette = userPreferences.colorPalette,
        highContrast = userPreferences.highContrast,
      ) {
        NoopApp(
          appState = appState,
          showOnboarding = !userPreferences.hasCompletedOnboarding,
        )
      }
    }
  }
}