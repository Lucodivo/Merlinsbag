package com.inasweaterpoorlyknit.core.data.model

import com.inasweaterpoorlyknit.core.database.model.ArticleThumbnails
import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages
import com.inasweaterpoorlyknit.core.database.model.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.LazyUriStrings

class LazyArticleFullImages(
  val directory: String,
  articleFullImagePaths: List<ArticleWithFullImages>
): LazyUriStrings {
  val paths = articleFullImagePaths.toMutableList()
  override val size get() = paths.size
  override fun getUriStrings(index: Int): List<String> =paths[index].fullImagePaths.map { "$directory${it.filename}" }
  companion object { val Empty = LazyArticleFullImages("", mutableListOf()) }
}

class LazyArticleThumbnails(
  val directory: String,
  articleThumbnailPaths: List<ArticleThumbnails>
): LazyUriStrings {
  val paths = articleThumbnailPaths.toMutableList()
  override val size get() = paths.size
  fun getArticleId(index: Int) = paths[index].articleId
  override fun getUriStrings(index: Int): List<String> = paths[index].thumbnailPaths.map { "$directory${it.filenameThumb}" }
  fun filter(keep: (ArticleThumbnails) -> Boolean) = LazyArticleThumbnails(directory, paths.filter(keep).toMutableList())
  fun articleIds() = paths.map { it.articleId }
}

class LazyEnsembleThumbnails(
    val ensemble: Ensemble,
    val thumbnails: LazyArticleThumbnails,
)

interface LazyFilenames {
  val lazyFullImageUris: LazyUriStrings
  val lazyThumbImageUris: LazyUriStrings
  val size: Int
  fun isEmpty() = size == 0
  fun isNotEmpty() = size != 0

  companion object {
    val Empty = object: LazyFilenames {
      override val lazyFullImageUris: LazyUriStrings = LazyUriStrings.Empty
      override val lazyThumbImageUris: LazyUriStrings = LazyUriStrings.Empty
      override val size: Int = 0
    }
  }
}

class LazyArticlesWithImages(
    val directory: String,
    articlesWithImages: List<ArticleWithImages>
): LazyFilenames {
  private val articleWithImages = articlesWithImages.toMutableList()
  fun getArticleId(index: Int) = articleWithImages[index].articleId

  override val size get() = articleWithImages.size
  override val lazyFullImageUris: LazyUriStrings = object: LazyUriStrings {
    override val size: Int get() = articlesWithImages.size
    override fun getUriStrings(index: Int): List<String> = articlesWithImages[index].imagePaths.map { "$directory${it.filename}" }
  }
  override val lazyThumbImageUris: LazyUriStrings = object: LazyUriStrings {
    override val size: Int get() = articlesWithImages.size
    override fun getUriStrings(index: Int): List<String> = articlesWithImages[index].imagePaths.map { "$directory${it.filenameThumb}" }
  }

  companion object {
    val Empty = LazyArticlesWithImages("", emptyList())
  }
}
