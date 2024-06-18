package com.inasweaterpoorlyknit.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.HighContrast
import com.inasweaterpoorlyknit.core.model.Typography
import com.inasweaterpoorlyknit.core.ui.theme.scheme.NoopColorSchemes

@Composable
fun NoopTheme(
    darkMode: DarkMode = DarkMode.SYSTEM,
    colorPalette: ColorPalette = ColorPalette.ROAD_WARRIOR,
    highContrast: HighContrast = HighContrast.OFF,
    typography: Typography = Typography.DEFAULT,
    content: @Composable () -> Unit,
) {
  val dark = when(darkMode){
    DarkMode.SYSTEM -> isSystemInDarkTheme()
    DarkMode.LIGHT -> false
    DarkMode.DARK -> true
  }
  val colorScheme = NoopColorSchemes.colorScheme(colorPalette, dark, highContrast)
  val view = LocalView.current
  if(!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = dark
    }
  }
  val composeTypography = when(typography) {
    Typography.DEFAULT -> typographyDefault
    Typography.MONTSERRAT -> typographyMontserrat
    Typography.JETBRAINS_MONO -> typographyJetBrainsMono
    Typography.CINZEL -> typographyCinzel
    Typography.CONCERT_ONE -> typographyConcertOne
    Typography.MACONDO -> typographyMacondo
    Typography.TINY_5 -> typographyTiny5
  }
  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
    typography = composeTypography,
  )
}

