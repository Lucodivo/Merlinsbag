package com.inasweaterpoorlyknit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImagesDao
import com.inasweaterpoorlyknit.core.database.dao.ArticleEnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleEnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity

@Database(entities = [
  ArticleEntity::class,
  ArticleImageEntity::class,
  EnsembleEntity::class,
  ArticleEnsembleEntity::class,],
  version = 1)
abstract class InKnitDatabase : RoomDatabase() {
  abstract fun ArticleWithImagesDao(): ArticleWithImagesDao
  abstract fun ArticleEnsembleDao(): ArticleEnsembleDao
}
