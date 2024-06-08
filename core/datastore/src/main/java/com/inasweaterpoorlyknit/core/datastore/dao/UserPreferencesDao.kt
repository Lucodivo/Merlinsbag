package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences as UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDao(
    private val preferencesDataStore: DataStore<UserPreferencesDataStore>
) {
  val userPreferences: Flow<UserPreferences> = preferencesDataStore.data.map {
    UserPreferences(
      hasCompletedOnboarding = it.hasCompletedOnboarding
    )
  }

  suspend fun setHasCompletedOnboarding(hasCompletedOnboarding: Boolean) {
    preferencesDataStore.updateData { currentPreferences ->
      currentPreferences.toBuilder()
          .setHasCompletedOnboarding(hasCompletedOnboarding)
          .build()
    }
  }
}