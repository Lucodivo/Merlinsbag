package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.inasweaterpoorlyknit.core.database.entity.ArticleImageEntity

data class ArticleEnsembleCount(
    val ensembleCount: Int,
    @ColumnInfo(name = "article_id") override val articleId: String,
    @Relation(
      parentColumn = "article_id",
      entityColumn = "article_id",
      entity = ArticleImageEntity::class
    )
    override val thumbnailPaths: List<ThumbnailFilename>
): ArticleThumbnails