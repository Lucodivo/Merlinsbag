package com.inasweaterpoorlyknit.core.data.model

import com.inasweaterpoorlyknit.core.database.model.ArticleWithImages
import com.inasweaterpoorlyknit.core.model.LazyUriStrings

class LazyArticlesWithImages(
    val directory: String,
    articlesWithImages: List<ArticleWithImages>
): LazyFilenames {
  val articleWithImages = articlesWithImages.toMutableList()
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
