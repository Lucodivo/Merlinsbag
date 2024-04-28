package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "clothing_article_images",
  indices = [Index(value = ["clothing_article_id"])],
  foreignKeys = [
    ForeignKey(
      entity = ClothingArticleEntity::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("clothing_article_id"),
      deferred = true
    )]
)
data class ClothingArticleImageEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "clothing_article_id") val clothingArticleId: String,
  @ColumnInfo(name = "uri") val uri: String,
  @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String,
  // TODO: image rank
)
