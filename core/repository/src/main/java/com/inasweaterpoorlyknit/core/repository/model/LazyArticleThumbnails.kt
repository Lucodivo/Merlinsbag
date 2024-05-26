package com.inasweaterpoorlyknit.core.repository.model

import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails

class LazyArticleThumbnails(
  val directory: String,
  private var articleThumbnailPaths: List<ArticleWithThumbnails>
): LazyUriStrings {
  override val size get() = articleThumbnailPaths.size
  fun getArticleId(index: Int) = articleThumbnailPaths[index].articleId
  override fun getUriString(index: Int): String = "$directory${articleThumbnailPaths[index].thumbnailPaths[0].uri}"
  fun filter(keep: (ArticleWithThumbnails) -> Boolean) = LazyArticleThumbnails(directory, articleThumbnailPaths.filter(keep))
  fun articleIds() = articleThumbnailPaths.map { it.articleId }
}