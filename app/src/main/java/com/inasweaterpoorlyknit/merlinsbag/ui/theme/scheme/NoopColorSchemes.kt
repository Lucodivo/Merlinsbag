package com.inasweaterpoorlyknit.merlinsbag.ui.theme.scheme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.HighContrast

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
    // TODO: Samsung's One UI seems to not respect this whatsoever. Always a shade of blue...
    return if(colorPalette == ColorPalette.SYSTEM_DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if(darkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
      val colorPaletteSchema = colorPaletteSchemes[colorPalette.ordinal]
      if(darkMode) {
        when(highContrast) {
          HighContrast.OFF -> colorPaletteSchema.dark()
          HighContrast.MEDIUM -> colorPaletteSchema.mediumContrastDark()
          HighContrast.HIGH -> colorPaletteSchema.highContrastDark()
        }
      } else {
        when(highContrast) {
          HighContrast.OFF -> colorPaletteSchema.light()
          HighContrast.MEDIUM -> colorPaletteSchema.mediumContrastLight()
          HighContrast.HIGH -> colorPaletteSchema.highContrastLight()
        }
      }
    }
  }
}
