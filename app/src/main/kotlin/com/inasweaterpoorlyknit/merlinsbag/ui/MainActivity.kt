package com.inasweaterpoorlyknit.merlinsbag.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.inasweaterpoorlyknit.core.ml.encourageInstallSubjectSegmentationModel
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToAddArticle
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToCamera
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.MainActivityViewModel.LoadState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

  val mainActivityViewModel: MainActivityViewModel by viewModels()

  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    val splashscreen = installSplashScreen()
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    mainActivityViewModel.processIntent(intent)

    var loading = true
    splashscreen.setKeepOnScreenCondition { loading }

    val context = this
    lifecycleScope.launch { encourageInstallSubjectSegmentationModel(context = context) }

    setContent {
      val appState = rememberNoopAppState(
        windowSizeClass = calculateWindowSizeClass(this)
      )
      val userPreferences by mainActivityViewModel.userPreferences.collectAsStateWithLifecycle()
      if(loading && mainActivityViewModel.uiState == LoadState.Loading) loading = false

      if(!loading && userPreferences.hasCompletedOnboarding) {
        LaunchedEffect(mainActivityViewModel.intentImageUriArgs) {
          mainActivityViewModel.intentImageUriArgs.getContentIfNotHandled()?.let {
            appState.navController.navigateToAddArticle(it)
          }
        }
        LaunchedEffect(mainActivityViewModel.navigateToCamera) {
          mainActivityViewModel.navigateToCamera.getContentIfNotHandled()?.let {
            appState.navController.navigateToCamera()
          }
        }
      }

      NoopTheme(
        darkMode = userPreferences.darkMode,
        colorPalette = userPreferences.colorPalette,
        highContrast = userPreferences.highContrast,
        typography = userPreferences.typography,
      ) {
        NoopApp(
          appState = appState,
          showOnboarding = !userPreferences.hasCompletedOnboarding,
        )
      }
    }
  }
}