package com.inasweaterpoorlyknit.inknit.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.navigation.InKnitApp
import com.inasweaterpoorlyknit.inknit.navigation.rememberInKnitAppState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavigationActivity : AppCompatActivity(){
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val appState = rememberInKnitAppState(
        windowSizeClass = calculateWindowSizeClass(activity = this),
      )
      InKnitApp(appState = appState)
    }
  }
}