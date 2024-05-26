package com.inasweaterpoorlyknit.core.repository.model

import com.inasweaterpoorlyknit.core.database.model.ArticleWithFullImages

class LazyArticleFullImages(
  val directory: String,
  private var articleFullImagePaths: List<ArticleWithFullImages>
): LazyUriStrings {
  override val size get() = articleFullImagePaths.size
  fun getArticleId(index: Int) = articleFullImagePaths[index].articleId
  override fun getUriString(index: Int): String = "$directory${articleFullImagePaths[index].fullImagePaths[0].uri}"
}