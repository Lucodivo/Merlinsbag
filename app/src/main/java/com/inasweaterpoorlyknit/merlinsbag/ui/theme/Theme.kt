package com.inasweaterpoorlyknit.merlinsbag.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun NoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme = when {
    // TODO: Samsung's One UI seems to not respect this whatsoever. Always a shade of blue...
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if(darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    darkTheme -> darkScheme
    else -> lightScheme
  }
  val view = LocalView.current
  if(!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    content = content
  )
}

