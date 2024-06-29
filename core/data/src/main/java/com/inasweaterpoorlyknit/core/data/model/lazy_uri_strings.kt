package com.inasweaterpoorlyknit.core.data.model

import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.LazyUriStrings

class LazyArticleFullImages(
  val directory: String,
  articleFullImagePaths: List<ArticleWithFullImages>
): LazyUriStrings {
  val paths = articleFullImagePaths.toMutableList()
  override val size get() = paths.size
  fun getArticleId(index: Int) = paths[index].articleId
  override fun getUriString(index: Int): String = "$directory${paths[index].fullImagePaths[0].filename}"
  override fun removeAt(removedIndex: Int) { paths.removeAt(removedIndex) }
  companion object { val Empty = LazyArticleFullImages("", mutableListOf()) }
}

class LazyArticleThumbnails(
  val directory: String,
  articleThumbnailPaths: List<ArticleWithThumbnails>
): LazyUriStrings {
  val paths = articleThumbnailPaths.toMutableList()
  override val size get() = paths.size
  fun getArticleId(index: Int) = paths[index].articleId
  override fun getUriString(index: Int): String = "$directory${paths[index].thumbnailPaths[0].filenameThumb}"
  fun filter(keep: (ArticleWithThumbnails) -> Boolean) = LazyArticleThumbnails(directory, paths.filter(keep).toMutableList())
  fun articleIds() = paths.map { it.articleId }
  override fun removeAt(removedIndex: Int) { paths.removeAt(removedIndex) }
}

class LazyEnsembleThumbnails(
    val ensemble: Ensemble,
    val thumbnails: LazyArticleThumbnails,
)