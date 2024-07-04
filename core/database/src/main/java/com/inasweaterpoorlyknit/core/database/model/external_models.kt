package com.inasweaterpoorlyknit.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Junction
import androidx.room.Relation

data class Ensemble(
  @ColumnInfo(name = "id") val id: String,
  @ColumnInfo(name = "title") val title: String,
)

data class ImageFilenames(
  @ColumnInfo("filename") val filename: String,
  @ColumnInfo("filename_thumb") val filenameThumb: String,
)

data class FullImageFilename(
  @ColumnInfo("filename") val filename: String,
)

data class ThumbnailFilename(
  @ColumnInfo("filename_thumb") val filenameThumb: String,
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

data class EnsembleCount(
    val title: String,
    val count: Long,
)

data class ArticleCount(
    val id: String,
    val count: Long,
)
