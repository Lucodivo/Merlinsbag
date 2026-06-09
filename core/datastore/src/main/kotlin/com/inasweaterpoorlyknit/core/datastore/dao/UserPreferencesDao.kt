package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography
import com.inasweaterpoorlyknit.core.model.proto.preference.UserPreferences
import kotlinx.coroutines.flow.Flow

class UserPreferencesDao(
    private val userPreferencesDataStore: DataStore<UserPreferences>
) {
  val userPreferences: Flow<UserPreferences> = userPreferencesDataStore.data

  suspend fun setHasCompletedOnboarding(hasCompletedOnboarding: Boolean) = userPreferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setHasCompletedOnboarding(hasCompletedOnboarding)
        .build()
  }

  suspend fun setDarkMode(darkMode: DarkMode) = userPreferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setDarkModeValue(darkMode.ordinal)
        .build()
  }

  suspend fun setColorPalette(colorPalette: ColorPalette) = userPreferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setColorPaletteValue(colorPalette.ordinal)
        .build()
  }

  suspend fun setHighContrast(highContrast: HighContrast) = userPreferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setHighContrastValue(highContrast.ordinal)
        .build()
  }

  suspend fun setTypography(typography: Typography) = userPreferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setTypographyValue(typography.ordinal)
        .build()
  }

  suspend fun setImageQuality(imageQuality: ImageQuality) = userPreferencesDataStore.updateData { currentPreferences ->
    currentPreferences.toBuilder()
        .setImageQualityValue(imageQuality.ordinal)
        .build()
  }
}