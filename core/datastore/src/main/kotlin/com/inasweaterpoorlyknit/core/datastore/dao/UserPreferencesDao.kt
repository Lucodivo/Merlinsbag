package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.inasweaterpoorlyknit.core.model.proto.preference.ColorPalette
import com.inasweaterpoorlyknit.core.model.proto.preference.DarkMode
import com.inasweaterpoorlyknit.core.model.proto.preference.HighContrast
import com.inasweaterpoorlyknit.core.model.proto.preference.ImageQuality
import com.inasweaterpoorlyknit.core.model.proto.preference.Typography
import com.inasweaterpoorlyknit.core.model.proto.preference.UserPreferences as UserPreferencesDataStore

class UserPreferencesDao(
    private val preferencesDataStore: DataStore<UserPreferencesDataStore>
) {
  val userPreferences: Flow<UserPreferences> = preferencesDataStore.data.map {
    UserPreferences(
      hasCompletedOnboarding = it.hasCompletedOnboarding,
      darkMode = it.darkMode,
      colorPalette = it.colorPalette,
      highContrast = it.highContrast,
      typography = it.typography,
      imageQuality = it.imageQuality,
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