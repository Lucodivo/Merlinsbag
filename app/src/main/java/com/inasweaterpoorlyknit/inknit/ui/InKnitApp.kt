package com.inasweaterpoorlyknit.inknit.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.inasweaterpoorlyknit.inknit.navigation.InKnitNavHost
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import kotlinx.coroutines.CoroutineScope

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun InKnitApp(
    appState: InKnitAppState,
    modifier: Modifier = Modifier,
) {
    InKnitTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
        ) {
            CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
                InKnitNavHost(
                    appState = appState,
                    modifier = modifier,
                )
            }
        }
    }
}

@Stable
class InKnitAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
)

@Composable
fun rememberInKnitAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
): InKnitAppState {
  return remember(navController, windowSizeClass) {
      InKnitAppState(
          navController = navController,
          windowSizeClass = windowSizeClass,
      )
  }
}