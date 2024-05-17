package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "article_image",
  foreignKeys = [
    ForeignKey(
      entity = ArticleEntity::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("article_id"),
      deferred = true
    )]
)
data class ArticleImageEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  @ColumnInfo("article_id", index = true) val articleId: String,
  @ColumnInfo("uri") val uri: String,
  @ColumnInfo("thumb_uri") val thumbUri: String,
  // TODO: image rank
)

data class ArticleThumbnail(
  @ColumnInfo("article_id") val articleId: String,
  @ColumnInfo("thumb_uri") val thumbUri: String,
)

data class ArticleFull(
  @ColumnInfo("article_id") val articleId: String,
  @ColumnInfo("uri") val uri: String,
)

data class ArticleImage(
  @ColumnInfo("article_id") val articleId: String,
  @ColumnInfo("uri") val uri: String,
  @ColumnInfo("thumb_uri") val thumbUri: String,
)
