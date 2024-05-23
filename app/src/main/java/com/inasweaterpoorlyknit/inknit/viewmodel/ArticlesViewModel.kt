package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
) : ViewModel() {
  private lateinit var articleIds: List<String>

  val articleThumbnails: StateFlow<List<String>> = articleRepository.getAllArticlesWithImages()
    .onEach { articlesWithImages -> articleIds = articlesWithImages.map { it.articleId } }
    .map { articlesWithImages ->
      articlesWithImages.map { it.images[0].thumbUri }
    }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = emptyList()
  )

  fun onDelete(articleIndices: List<Int>) {
    viewModelScope.launch(Dispatchers.IO) { articleRepository.deleteArticles(articleIndices.map { articleIds[it] }) }
  }
}