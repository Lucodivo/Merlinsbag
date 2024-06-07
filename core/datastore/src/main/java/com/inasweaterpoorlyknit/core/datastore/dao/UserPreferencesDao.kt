package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDao(
    private val preferencesDataStore: DataStore<UserPreferences>
) {
  private val userPreferences: Flow<UserPreferences> = preferencesDataStore.data

  val hasCompletedOnboarding: Flow<Boolean> = userPreferences.map { it.hasCompletedOnboarding }

  suspend fun setHasCompletedOnboarding(hasCompletedOnboarding: Boolean) {
    preferencesDataStore.updateData { currentPreferences ->
      currentPreferences.toBuilder()
          .setHasCompletedOnboarding(hasCompletedOnboarding)
          .build()
    }
  }
}