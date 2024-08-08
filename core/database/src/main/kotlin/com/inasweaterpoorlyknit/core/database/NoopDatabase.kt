package com.inasweaterpoorlyknit.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inasweaterpoorlyknit.core.database.dao.ArticleDao
import com.inasweaterpoorlyknit.core.database.dao.EnsembleDao
import com.inasweaterpoorlyknit.core.database.model.ArticleEntity
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import com.inasweaterpoorlyknit.core.database.model.EnsembleFtsEntity

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