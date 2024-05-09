package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// TODO: ArticleDetailUiState
@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
): ViewModel() {
  data class ArticleDetails(val imageUriString: String)
  fun getArticleDetails(articleId: String): LiveData<ArticleDetails?> {
    return articleRepository.getArticleWithImages(articleId).map {
        ArticleDetails(imageUriString = it.images[0].uri)
    }
  }
}