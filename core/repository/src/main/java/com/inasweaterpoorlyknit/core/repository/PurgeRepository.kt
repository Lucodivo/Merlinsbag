package com.inasweaterpoorlyknit.core.repository

import android.content.Context
import com.inasweaterpoorlyknit.core.database.dao.PurgeDao

class PurgeRepository(
    private val context: Context,
    private val purgeDao: PurgeDao,
) {
  fun purgeUserData() {
    purgeDao.purgeDatabase()
    context.filesDir.deleteRecursively()
    purgeCache()
  }
  fun purgeCache() {
    context.cacheDir.deleteRecursively()
  }
}
