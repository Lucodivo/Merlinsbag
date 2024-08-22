package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.combine
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleCount
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.StatisticsViewModel.ArticleWithMostEnsembles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface StatisticsUiState{
  data object Loading: StatisticsUiState
  data class Success(
      val ensembleCount: Int,
      val articleCount: Int,
      val articleImageCount: Int,
      val ensemblesWithMostArticles: List<EnsembleArticleCount>,
      val articleWithMostImagesUriStrings: List<String>,
      val articleWithMostEnsembles: ArticleWithMostEnsembles,
  ): StatisticsUiState
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    ensembleRepository: EnsembleRepository,
    articleRepository: ArticleRepository,
): ViewModel() {

  data class ArticleWithMostEnsembles(
    val count: Int,
    val uriStrings: List<String>,
  )

  companion object {
    const val TOP_ENSEMBLES_COUNT = 10
  }

  val uiState: StateFlow<StatisticsUiState> = combine(
    ensembleRepository.getCountEnsembles(),
    articleRepository.getCountArticles(),
    articleRepository.getCountArticleImages(),
    ensembleRepository.getMostPopularEnsembles(TOP_ENSEMBLES_COUNT),
    articleRepository.getMostPopularArticlesImageCount(1).map {
      if(it.isEmpty()) emptyList() else it.getUriStrings(0)
    },
    ensembleRepository.getMostPopularArticlesEnsembleCount(1).map {
      if(it.first.isEmpty() || it.second.isEmpty()) ArticleWithMostEnsembles(0, emptyList())
      else ArticleWithMostEnsembles(it.first[0], it.second.getUriStrings(0))
    },
  ) { ensembleCount, articleCount, articleImageCount, ensemblesWithMostArticles, articleWithMostImages, articleWithMostEnsembles ->
    StatisticsUiState.Success(
      ensembleCount = ensembleCount,
      articleCount = articleCount,
      articleImageCount = articleImageCount,
      ensemblesWithMostArticles = ensemblesWithMostArticles,
      articleWithMostImagesUriStrings = articleWithMostImages,
      articleWithMostEnsembles = articleWithMostEnsembles,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
    initialValue = StatisticsUiState.Loading,
  )
}