package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "ensemble")
data class EnsembleEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val title: String,
  @ColumnInfo(name = "created") val createdEpoch: Long = Date().time,
  @ColumnInfo(name = "modified") val modifiedEpoch: Long = createdEpoch,
)

// Ensemble & Article join table
@Entity(tableName = "ensemble_article",
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
  @ColumnInfo(name = "ensemble_id", index = true) val ensembleId: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "article_id", index = true) val articleId: String = UUID.randomUUID().toString(),
)