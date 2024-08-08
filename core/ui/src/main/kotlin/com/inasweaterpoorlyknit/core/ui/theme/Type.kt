package com.inasweaterpoorlyknit.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.inasweaterpoorlyknit.core.ui.R

/*
  INSTRUCTIONS: ADDING NEW FONT
  Acquire .ttf font file
    - https://fonts.google.com/
  Place .ttf file in res/font
    - filename must be all lowercase and underscores (no hyphen/spaces)
  Create a new FontFamily using the resource id, as seen below
  Create a new Typography from the FontFamily, as seen below
  Edit user_preferences.proto to add a new enum for the new font family
  Use the same enum order in user_preferences.proto to edit com.inasweaterpoorlyknit.core.model.Typography enum
  Add option in SettingsScreen.kt
  Add option in NoopTheme.kt

  ** ENUM ORDER MATTERS AS ORDINAL VALUES ARE DEPENDED ON WHEN TRANSLATING BETWEEN ENUMS **
 */

private val fontFamilyMontserrat = FontFamily(Font(R.font.montserrat_regular))
private val fontFamilyJetBrainsMono = FontFamily(Font(R.font.jetbrains_mono_regular))
private val fontFamilyCinzel = FontFamily(Font(R.font.cinzel_regular))
private val fontFamilyConcertOne = FontFamily(Font(R.font.concert_one_regular))
private val fontFamilyMacondo = FontFamily(Font(R.font.macondo_regular))
private val fontFamilyTiny5 = FontFamily(Font(R.font.tiny5_regular))

val typographyDefault = Typography() // Default Material 3 typography values
val typographyMontserrat = typography(fontFamilyMontserrat)
val typographyJetBrainsMono = typography(fontFamilyJetBrainsMono)
val typographyCinzel = typography(fontFamilyCinzel)
val typographyConcertOne = typography(fontFamilyConcertOne)
val typographyTiny5 = typography(fontFamilyTiny5)
val typographyMacondo = typography(fontFamilyMacondo)

private fun typography(fontFamily: FontFamily) = Typography (
  displayLarge = typographyDefault.displayLarge.copy(fontFamily = fontFamily),
  displayMedium = typographyDefault.displayMedium.copy(fontFamily = fontFamily),
  displaySmall = typographyDefault.displaySmall.copy(fontFamily = fontFamily),
  headlineLarge = typographyDefault.headlineLarge.copy(fontFamily = fontFamily),
  headlineMedium = typographyDefault.headlineMedium.copy(fontFamily = fontFamily),
  headlineSmall = typographyDefault.headlineSmall.copy(fontFamily = fontFamily),
  titleLarge = typographyDefault.titleLarge.copy(fontFamily = fontFamily),
  titleMedium = typographyDefault.titleMedium.copy(fontFamily = fontFamily),
  titleSmall = typographyDefault.titleSmall.copy(fontFamily = fontFamily),
  bodyLarge = typographyDefault.bodyLarge.copy(fontFamily = fontFamily),
  bodyMedium = typographyDefault.bodyMedium.copy(fontFamily = fontFamily),
  bodySmall = typographyDefault.bodySmall.copy(fontFamily = fontFamily),
  labelLarge = typographyDefault.labelLarge.copy(fontFamily = fontFamily),
  labelMedium = typographyDefault.labelMedium.copy(fontFamily = fontFamily),
  labelSmall = typographyDefault.labelSmall.copy(fontFamily = fontFamily),
)
