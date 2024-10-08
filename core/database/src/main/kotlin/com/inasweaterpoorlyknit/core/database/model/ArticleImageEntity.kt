package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inasweaterpoorlyknit.core.database.dao.generateId

@Entity(tableName = "article_image",
  foreignKeys = [
    ForeignKey(
      entity = ArticleEntity::class,
      parentColumns = arrayOf("id"),
      childColumns = arrayOf("article_id"),
      onDelete = ForeignKey.CASCADE,
      deferred = true
    )],
  indices = [Index(value = ["filename_thumb"])],
)
data class ArticleImageEntity(
    @PrimaryKey
    @ColumnInfo("id") val id: String = generateId(),
    @ColumnInfo("article_id", index = true) val articleId: String,
    @ColumnInfo("filename") val filename: String,
    @ColumnInfo("filename_thumb") val filenameThumb: String,
)