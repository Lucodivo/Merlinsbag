package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Junction
import androidx.room.Relation

data class Ensemble(
  @ColumnInfo(name = "id") val id: String,
  @ColumnInfo(name = "title") val title: String,
)

data class ImageFilenames(
  @ColumnInfo("uri") val uri: String,
  @ColumnInfo("thumb_uri") val uriThumb: String,
)

data class FullImageFilename(
  @ColumnInfo("uri") val uri: String,
)

data class ThumbnailFilename(
  @ColumnInfo("thumb_uri") val uri: String,
)

data class ArticleWithThumbnails(
  @ColumnInfo("article_id") val articleId: String,
  @Relation(parentColumn = "article_id", entityColumn = "article_id", entity = ArticleImageEntity::class)
  val thumbnailPaths: List<ThumbnailFilename>
)

data class ArticleWithImages(
  @ColumnInfo("article_id") val articleId: String,
  @Relation(parentColumn = "article_id", entityColumn = "article_id", entity = ArticleImageEntity::class)
  val imagePaths: List<ImageFilenames>
)

data class ArticleWithFullImages(
  @ColumnInfo("article_id") val articleId: String,
  @Relation(parentColumn = "article_id", entityColumn = "article_id", entity = ArticleImageEntity::class)
  val fullImagePaths: List<FullImageFilename>
)

data class EnsembleArticleThumbnails(
  @ColumnInfo(name = "ensemble_id") val ensembleId: String,
  @ColumnInfo(name = "ensemble_title") val ensembleTitle: String,
  @Relation(
    parentColumn = "ensemble_id",
    entityColumn = "article_id",
    associateBy = Junction(
      value = EnsembleArticleEntity::class,
      parentColumn = "ensemble_id",
      entityColumn = "article_id",
    ),
    entity = ArticleImageEntity::class,
  )
  val articles: List<ArticleWithThumbnails>,
)