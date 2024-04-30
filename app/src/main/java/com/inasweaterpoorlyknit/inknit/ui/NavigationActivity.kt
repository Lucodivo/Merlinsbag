package com.inasweaterpoorlyknit.inknit.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NavigationActivity : AppCompatActivity(){
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val appState = rememberInKnitAppState(
        windowSizeClass = calculateWindowSizeClass(this)
      )
      InKnitApp(appState = appState)
    }
  }
}