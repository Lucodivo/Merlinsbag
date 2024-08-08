package com.inasweaterpoorlyknit.core.data.repository

import android.content.Context
import com.inasweaterpoorlyknit.core.database.dao.PurgeDatabaseDao
import com.inasweaterpoorlyknit.core.datastore.dao.PurgeDataStoreDao

class PurgeRepository(
    private val context: Context,
    private val purgeDatabaseDao: PurgeDatabaseDao,
    private val purgeDataStoreDao: PurgeDataStoreDao,
) {
  suspend fun purgeUserData() {
    purgeDatabaseDao.purgeDatabase()
    purgeDataStoreDao.purgeDataStore()
    context.filesDir.deleteRecursively()
    purgeCache()
  }
  fun purgeCache() {
    context.cacheDir.deleteRecursively()
  }
}
