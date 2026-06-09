package com.inasweaterpoorlyknit.core.datastore.dao

import androidx.datastore.core.DataStore
import com.inasweaterpoorlyknit.core.model.UserPreferencesDefault
import com.inasweaterpoorlyknit.core.model.proto.preference.UserPreferences

class PurgeDataStoreDao(
    private val preferencesDataStore: DataStore<UserPreferences>
) {
  suspend fun purgeDataStore() = preferencesDataStore.updateData { UserPreferencesDefault }
}