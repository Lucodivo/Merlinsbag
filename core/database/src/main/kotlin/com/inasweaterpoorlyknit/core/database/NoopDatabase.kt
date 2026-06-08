package com.inasweaterpoorlyknit.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.entity.ArticleEntity
import com.inasweaterpoorlyknit.core.database.entity.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.entity.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.entity.EnsembleEntity
import com.inasweaterpoorlyknit.core.database.entity.EnsembleFtsEntity

@Database(
  entities = [
    ArticleEntity::class,
    ArticleImageEntity::class,
    EnsembleEntity::class,
    EnsembleFtsEntity::class,
    EnsembleArticleEntity::class,
  ],
  version = 2,
  autoMigrations = [
    AutoMigration(from = 1, to = 2)
  ]
)
abstract class NoopDatabase : RoomDatabase() {
  abstract fun ArticleDao(): ArticleDao
  abstract fun EnsembleDao(): EnsembleDao
}