package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.inasweaterpoorlyknit.merlinsbag.ColorPalette as ColorPaletteDataStore
import com.inasweaterpoorlyknit.merlinsbag.DarkMode as DarkModeDataStore
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences as UserPreferencesDataStore

class UserPreferencesDao(
    private val preferencesDataStore: DataStore<UserPreferencesDataStore>
) {
  private fun DarkModeDataStore.fromDataStore() = DarkMode.entries[ordinal]
  private fun ColorPaletteDataStore.fromDataStore() = ColorPalette.entries[ordinal]

  val userPreferences: Flow<UserPreferences> = preferencesDataStore.data.map {
    UserPreferences(
      hasCompletedOnboarding = it.hasCompletedOnboarding,
      darkMode = it.darkMode.fromDataStore(),
      colorPalette = it.colorPalette.fromDataStore(),
    )
  }

  suspend fun setHasCompletedOnboarding(hasCompletedOnboarding: Boolean) {
    preferencesDataStore.updateData { currentPreferences ->
      currentPreferences.toBuilder()
          .setHasCompletedOnboarding(hasCompletedOnboarding)
          .build()
    }
  }

  suspend fun setDarkMode(darkMode: DarkMode) {
    preferencesDataStore.updateData { currentPreferences ->
      currentPreferences.toBuilder()
          .setDarkModeValue(darkMode.ordinal)
          .build()
    }
  }

  suspend fun setColorPalette(colorPalette: ColorPalette) {
    preferencesDataStore.updateData { currentPreferences ->
      currentPreferences.toBuilder()
          .setColorPaletteValue(colorPalette.ordinal)
          .build()
    }
  }
}