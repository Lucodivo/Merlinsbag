package com.inasweaterpoorlyknit.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette.ColorPalette_RoadWarrior
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode.DarkMode_Dark
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode.DarkMode_Light
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode.DarkMode_System
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast.HighContrast_Off
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_Cinzel
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_ConcertOne
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_Default
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_JetBrainsMono
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_Macondo
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_Montserrat
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography.Typography_Tiny5
import com.inasweaterpoorlyknit.core.ui.theme.scheme.NoopColorSchemes

@Composable
fun NoopTheme(
    darkMode: DarkMode = DarkMode_System,
    colorPalette: ColorPalette = ColorPalette_RoadWarrior,
    highContrast: HighContrast = HighContrast_Off,
    typography: Typography = Typography_Default,
    content: @Composable () -> Unit,
) {
  val dark = when(darkMode){
    DarkMode_Light -> false
    DarkMode_Dark -> true
    DarkMode_System,
    DarkMode.UNRECOGNIZED -> isSystemInDarkTheme()
  }
  val colorScheme = NoopColorSchemes.colorScheme(colorPalette, dark, highContrast)
  val composeTypography = when(typography) {
    Typography_Montserrat -> typographyMontserrat
    Typography_JetBrainsMono -> typographyJetBrainsMono
    Typography_Cinzel -> typographyCinzel
    Typography_ConcertOne -> typographyConcertOne
    Typography_Macondo -> typographyMacondo
    Typography_Tiny5 -> typographyTiny5
    Typography_Default,
    Typography.UNRECOGNIZED -> typographyDefault
  }
  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
    typography = composeTypography,
  )
}

