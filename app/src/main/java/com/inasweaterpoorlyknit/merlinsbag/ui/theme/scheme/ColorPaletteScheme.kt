package com.inasweaterpoorlyknit.merlinsbag.ui.theme.scheme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.inasweaterpoorlyknit.merlinsbag.R

interface ColorPaletteScheme{
  fun nameStrRes(): Int
  fun light(): ColorScheme
  fun dark(): ColorScheme
  fun mediumContrastLight(): ColorScheme
  fun mediumContrastDark(): ColorScheme
  fun highContrastLight(): ColorScheme
  fun highContrastDark(): ColorScheme
}

object DefaultColorPaletteScheme: ColorPaletteScheme{
  override fun nameStrRes(): Int = R.string.system_dynamic
  override fun light(): ColorScheme = lightColorScheme()
  override fun dark(): ColorScheme = darkColorScheme()
  override fun mediumContrastLight(): ColorScheme = lightColorScheme()
  override fun mediumContrastDark(): ColorScheme = darkColorScheme()
  override fun highContrastLight(): ColorScheme = lightColorScheme()
  override fun highContrastDark(): ColorScheme = darkColorScheme()
}
