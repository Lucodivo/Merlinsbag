package com.inasweaterpoorlyknit.merlinsbag.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import dagger.hilt.android.AndroidEntryPoint

private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

@AndroidEntryPoint
class MainActivity : ComponentActivity(){
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      // TODO: Properly handle edge to edge in app
      //  https://developer.android.com/develop/ui/compose/layouts/insets
/*
      val darkTheme = isSystemInDarkTheme()
      DisposableEffect(darkTheme) {
        enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
          ) { darkTheme },
          navigationBarStyle = SystemBarStyle.auto(
            lightScrim,
            darkScrim,
          ) { darkTheme },
        )
        onDispose {}
      }
*/

      val appState = rememberNoopAppState(
        windowSizeClass = calculateWindowSizeClass(this)
      )
      NoopTheme {
        NoopApp(appState = appState)
      }
    }
  }
}