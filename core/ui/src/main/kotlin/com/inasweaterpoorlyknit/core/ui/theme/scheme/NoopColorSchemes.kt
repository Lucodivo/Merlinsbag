package com.inasweaterpoorlyknit.core.ui.theme.scheme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette.ColorPalette_SystemDynamic
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast.HighContrast_High
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast.HighContrast_Medium
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast.HighContrast_Off

object NoopColorSchemes {
  val colorPaletteSchemes = arrayOf(
    DefaultColorPaletteScheme, // Should never be hit but important. Allows ordinal of enums to line up.
    RoadWarriorColorPaletteScheme,
    RetroColorPaletteScheme,
    SkyColorSchemePalette,
    CrimsonColorPaletteScheme,
    NinetyFiveColorPaletteScheme,
  )

  @Composable
  fun colorScheme(
      colorPalette: ColorPalette,
      darkMode: Boolean,
      highContrast: HighContrast,
  ): ColorScheme {
    val context = LocalContext.current
    // Note: Samsung's One UI seems to not respect this whatsoever. Always a shade of blue...
    return if(colorPalette == ColorPalette_SystemDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if(darkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
      val colorPaletteSchema = colorPaletteSchemes[colorPalette.ordinal]
      if(darkMode) {
        when (highContrast) {
          HighContrast_Medium -> colorPaletteSchema.mediumContrastDark()
          HighContrast_High -> colorPaletteSchema.highContrastDark()
          HighContrast_Off,
          HighContrast.UNRECOGNIZED -> colorPaletteSchema.dark()
        }
      } else {
        when (highContrast) {
          HighContrast_Medium -> colorPaletteSchema.mediumContrastLight()
          HighContrast_High -> colorPaletteSchema.highContrastLight()
          HighContrast_Off,
          HighContrast.UNRECOGNIZED -> colorPaletteSchema.light()
        }
      }
    }
  }
}
