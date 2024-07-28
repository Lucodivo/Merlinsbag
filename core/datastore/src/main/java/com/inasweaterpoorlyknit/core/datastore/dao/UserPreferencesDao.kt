package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.HighContrast
import com.inasweaterpoorlyknit.core.model.ImageQuality
import com.inasweaterpoorlyknit.core.model.Typography
import com.inasweaterpoorlyknit.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.inasweaterpoorlyknit.merlinsbag.ColorPalette as ColorPaletteDataStore
import com.inasweaterpoorlyknit.merlinsbag.DarkMode as DarkModeDataStore
import com.inasweaterpoorlyknit.merlinsbag.HighContrast as HighContrastDataStore
import com.inasweaterpoorlyknit.merlinsbag.ImageQuality as ImageQualityDataStore
import com.inasweaterpoorlyknit.merlinsbag.Typography as TypographyDataStore
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences as UserPreferencesDataStore

class UserPreferencesDao(
    private val preferencesDataStore: DataStore<UserPreferencesDataStore>
) {
  private fun DarkModeDataStore.fromDataStore() = DarkMode.entries[ordinal]
  private fun ColorPaletteDataStore.fromDataStore() = ColorPalette.entries[ordinal]
  private fun HighContrastDataStore.fromDataStore() = HighContrast.entries[ordinal]
  private fun TypographyDataStore.fromDataStore() = Typography.entries[ordinal]
  private fun ImageQualityDataStore.fromDataStore() = ImageQuality.entries[ordinal]

  val userPreferences: Flow<UserPreferences> = preferencesDataStore.data.map {
    UserPreferences(
      hasCompletedOnboarding = it.hasCompletedOnboarding,
      darkMode = it.darkMode.fromDataStore(),
      colorPalette = it.colorPalette.fromDataStore(),
      highContrast = it.highContrast.fromDataStore(),
      typography = it.typography.fromDataStore(),
      imageQuality = it.imageQuality.fromDataStore(),
    )
  }

  suspend fun setHasCompletedOnboarding(hasCompletedOnboarding: Boolean) = preferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setHasCompletedOnboarding(hasCompletedOnboarding)
        .build()
  }

  suspend fun setDarkMode(darkMode: DarkMode) = preferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setDarkModeValue(darkMode.ordinal)
        .build()
  }

  suspend fun setColorPalette(colorPalette: ColorPalette) = preferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setColorPaletteValue(colorPalette.ordinal)
        .build()
  }

  suspend fun setHighContrast(highContrast: HighContrast) = preferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setHighContrastValue(highContrast.ordinal)
        .build()
  }

  suspend fun setTypography(typography: Typography) = preferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setTypographyValue(typography.ordinal)
        .build()
  }

  suspend fun setImageQuality(imageQuality: ImageQuality) = preferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setImageQualityValue(imageQuality.ordinal)
        .build()
  }
}