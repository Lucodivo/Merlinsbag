package com.inasweaterpoorlyknit.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

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