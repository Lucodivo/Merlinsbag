package com.inasweaterpoorlyknit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleFtsEntity

@Database(entities = [
  ArticleEntity::class,
  ArticleImageEntity::class,
  EnsembleEntity::class,
  EnsembleFtsEntity::class,
  EnsembleArticleEntity::class,],
  version = 1)
abstract class NoopDatabase : RoomDatabase() {
  abstract fun ArticleDao(): ArticleDao
  abstract fun EnsembleDao(): EnsembleDao
}