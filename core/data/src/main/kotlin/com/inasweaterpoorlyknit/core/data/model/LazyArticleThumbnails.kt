package com.inasweaterpoorlyknit.core.data.model

import com.inasweaterpoorlyknit.core.database.model.ArticleThumbnails
import com.inasweaterpoorlyknit.core.model.LazyUriStrings

class LazyArticleThumbnails(
  private val directory: String,
  articleThumbnailPaths: List<ArticleThumbnails>
): LazyUriStrings {
  val paths = articleThumbnailPaths.toMutableList()
  override val size get() = paths.size
  fun getArticleId(index: Int) = paths[index].articleId
  override fun getUriStrings(index: Int): List<String> = paths[index].thumbnailPaths.map { "$directory${it.filenameThumb}" }
  fun filter(keep: (ArticleThumbnails) -> Boolean) =
      LazyArticleThumbnails(directory, paths.filter(keep).toMutableList())
  fun articleIds() = paths.map { it.articleId }

  companion object {
    val Empty = LazyArticleThumbnails(directory = "", articleThumbnailPaths = emptyList())
  }
}