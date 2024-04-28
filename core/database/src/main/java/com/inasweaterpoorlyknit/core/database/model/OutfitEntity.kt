package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "outfits")
data class OutfitEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "created") val createdEpoch: Long = Date().time,
  @ColumnInfo(name = "modified") val modifiedEpoch: Long = createdEpoch,
)

// Outfits & Clothing Articles join table
@Entity(tableName = "outfit_articles",
  primaryKeys = ["clothing_article_id", "outfit_id"],
  foreignKeys = [
    ForeignKey(
      entity = ClothingArticleEntity::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("clothing_article_id"),
      deferred = true
    ),
    ForeignKey(
      entity = OutfitEntity::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("outfit_id"),
      deferred = true
    ),
  ]
)
data class OutfitArticlesEntity(
  @ColumnInfo(name = "clothing_article_id") val clothingArticleId: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "outfit_id") val outfitId: String = UUID.randomUUID().toString(),
)
