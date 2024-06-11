package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.datastore.UserPreferencesSerializer
import com.inasweaterpoorlyknit.merlinsbag.UserPreferences

class PurgeDataStoreDao(
    private val preferencesDataStore: DataStore<UserPreferences>
) {
  suspend fun purgeDataStore() {
    preferencesDataStore.updateData {
      UserPreferencesSerializer.defaultUserPreferences
    }
  }
}