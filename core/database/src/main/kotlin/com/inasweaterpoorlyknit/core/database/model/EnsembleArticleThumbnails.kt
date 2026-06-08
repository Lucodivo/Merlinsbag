package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Junction
import androidx.room.Relation
import com.inasweaterpoorlyknit.core.database.entity.ArticleEntity
import com.inasweaterpoorlyknit.core.database.entity.EnsembleArticleEntity

data class EnsembleArticleThumbnails(
    @ColumnInfo(name = "ensemble_id") val ensembleId: String,
    @ColumnInfo(name = "ensemble_title") val ensembleTitle: String,
    @Relation(
    parentColumn = "ensemble_id",
    entityColumn = "id",
    associateBy = Junction(
        value = EnsembleArticleEntity::class,
        parentColumn = "ensemble_id",
        entityColumn = "article_id",
    ),
    entity = ArticleEntity::class,
  )
  val articles: List<EnsembleArticleWithThumbnails>,
)