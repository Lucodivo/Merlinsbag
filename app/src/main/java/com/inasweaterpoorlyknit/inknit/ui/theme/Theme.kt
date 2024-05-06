package com.inasweaterpoorlyknit.inknit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

@Composable
fun InKnitTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          if (darkTheme) dynamicDarkColorScheme(context)
          else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}