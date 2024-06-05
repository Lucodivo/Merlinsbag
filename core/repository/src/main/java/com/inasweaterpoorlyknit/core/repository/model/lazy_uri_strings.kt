package com.inasweaterpoorlyknit.core.repository.model

import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails

// Images are stored as filenames in specified directories.
// This interface is used to reconstruct the absolute file path only as needed.
// There exists real user scenarios where we may pre-emptively retrieve 100s
// of image filenames but only 3-4 are ever actually accessed.
interface LazyUriStrings {
  companion object {
    val Empty = object : LazyUriStrings {
      override val size get() = 0
      override fun getUriString(index: Int): String = ""
    }
  }
  val size: Int
  fun isEmpty() = size == 0
  fun isNotEmpty() = size != 0
  fun getUriString(index: Int): String
}

class LazyArticleFullImages(
  val directory: String,
  private var articleFullImagePaths: List<ArticleWithFullImages>
): LazyUriStrings {
  override val size get() = articleFullImagePaths.size
  fun getArticleId(index: Int) = articleFullImagePaths[index].articleId
  override fun getUriString(index: Int): String = "$directory${articleFullImagePaths[index].fullImagePaths[0].filename}"
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
  val id: String,
  val title: String,
  val thumbnails: LazyArticleThumbnails,
)