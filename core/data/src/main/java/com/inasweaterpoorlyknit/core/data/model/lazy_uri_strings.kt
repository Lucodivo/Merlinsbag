package com.inasweaterpoorlyknit.core.data.model

import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.LazyUriStrings

class LazyArticleFullImages(
  val directory: String,
  private var articleFullImagePaths: List<ArticleWithFullImages>
): LazyUriStrings {
  override val size get() = articleFullImagePaths.size
  fun getArticleId(index: Int) = articleFullImagePaths[index].articleId
  override fun getUriString(index: Int): String = "$directory${articleFullImagePaths[index].fullImagePaths[0].filename}"
  companion object { val Empty = LazyArticleFullImages("", emptyList()) }
}

class LazyArticleThumbnails(
  val directory: String,
  private var articleThumbnailPaths: List<ArticleWithThumbnails>
): LazyUriStrings {
  override val size get() = articleThumbnailPaths.size
  fun getArticleId(index: Int) = articleThumbnailPaths[index].articleId
  override fun getUriString(index: Int): String = "$directory${articleThumbnailPaths[index].thumbnailPaths[0].filenameThumb}"
  fun filter(keep: (ArticleWithThumbnails) -> Boolean) = LazyArticleThumbnails(directory, articleThumbnailPaths.filter(keep))
  fun articleIds() = articleThumbnailPaths.map { it.articleId }
}

class LazyEnsembleThumbnails(
    val ensemble: Ensemble,
    val thumbnails: LazyArticleThumbnails,
)