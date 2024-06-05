package com.inasweaterpoorlyknit.core.database.dao

import com.inasweaterpoorlyknit.core.database.NoopDatabase

class PurgeDao(val database: NoopDatabase) {
  fun purgeDatabase() = database.clearAllTables()
}