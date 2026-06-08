package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.inasweaterpoorlyknit.core.database.entity.ArticleImageEntity

data class EnsembleArticleWithThumbnails(
    @ColumnInfo("id") override val articleId: String,
    @Relation(
      parentColumn = "id",
      entityColumn = "article_id",
      entity = ArticleImageEntity::class
    )
    override val thumbnailPaths: List<ThumbnailFilename>
): ArticleThumbnails