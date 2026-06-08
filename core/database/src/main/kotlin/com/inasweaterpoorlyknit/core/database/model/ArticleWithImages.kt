package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.inasweaterpoorlyknit.core.database.entity.ArticleImageEntity

data class ArticleWithImages(
    @ColumnInfo("article_id") val articleId: String,
    @Relation(parentColumn = "article_id", entityColumn = "article_id", entity = ArticleImageEntity::class)
  val imagePaths: List<ImageFilenames>
)