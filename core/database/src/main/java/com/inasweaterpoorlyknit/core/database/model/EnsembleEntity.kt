package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "ensemble")
data class EnsembleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "created") val created: Long,
    @ColumnInfo(name = "modified") val modified: Long,
)

@Entity(tableName = "ensemble_fts")
@Fts4(contentEntity = EnsembleEntity::class)
data class EnsembleFtsEntity(
    @ColumnInfo(name = "id") val ensembleId: String,
    @ColumnInfo(name = "title") val title: String,
)

// Ensemble & Article join table
@Entity(
  tableName = "ensemble_article",
  primaryKeys = ["article_id", "ensemble_id"],
  foreignKeys = [
    ForeignKey(
      entity = ArticleEntity::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("article_id"),
      onDelete = ForeignKey.CASCADE,
      deferred = true
    ),
    ForeignKey(
      entity = EnsembleEntity::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("ensemble_id"),
      onDelete = ForeignKey.CASCADE,
      deferred = true
    ),
  ]
)
data class EnsembleArticleEntity(
    @ColumnInfo(name = "ensemble_id", index = true) val ensembleId: String,
    @ColumnInfo(name = "article_id", index = true) val articleId: String,
)