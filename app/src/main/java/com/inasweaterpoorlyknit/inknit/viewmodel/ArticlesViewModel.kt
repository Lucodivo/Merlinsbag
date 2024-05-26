package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
) : ViewModel() {
  private lateinit var lazyArticleImages: LazyArticleThumbnails

  val articleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
    .onEach { lazyArticleImages = it }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = LazyUriStrings.Empty,
    )

  fun onDelete(articleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    val articleIds = List(articleIndices.size){ lazyArticleImages.getArticleId(articleIndices[it]) }
    articleRepository.deleteArticles(articleIds)
  }

}