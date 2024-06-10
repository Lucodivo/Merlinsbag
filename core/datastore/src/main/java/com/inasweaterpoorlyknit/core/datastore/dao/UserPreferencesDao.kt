package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences as UserPreferencesDataStore
import com.inasweaterpoorlyknit.merlinsbag.DarkMode as DarkModeDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDao(
    private val preferencesDataStore: DataStore<UserPreferencesDataStore>
) {
  private fun DarkMode.toDataStore(): DarkModeDataStore = when(this) {
      DarkMode.SYSTEM -> DarkModeDataStore.System
      DarkMode.LIGHT -> DarkModeDataStore.Light
      DarkMode.DARK -> DarkModeDataStore.Dark
    }

  private fun DarkModeDataStore.fromDataStore(): DarkMode = when(this) {
    DarkModeDataStore.System -> DarkMode.SYSTEM
    DarkModeDataStore.Light -> DarkMode.LIGHT
    DarkModeDataStore.Dark -> DarkMode.DARK
    DarkModeDataStore.UNRECOGNIZED -> DarkMode.SYSTEM
  }


  val userPreferences: Flow<UserPreferences> = preferencesDataStore.data.map {
    UserPreferences(
      hasCompletedOnboarding = it.hasCompletedOnboarding,
      darkMode = it.darkMode.fromDataStore()
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
          .setDarkMode(darkMode.toDataStore())
          .build()
    }
  }
}