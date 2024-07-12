package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.ArticleEnsembleCount
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleCount
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatisticsUiState(
    val ensembleCount: Int,
    val articleCount: Int,
    val articleImageCount: Int,
    val topEnsembles: List<EnsembleArticleCount>,
    val topArticleMostEnsemblesCount: Int,
    val topArticleMostImagesCount: LazyUriStrings,
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    ensembleRepository: EnsembleRepository,
    articleRepository: ArticleRepository,
): ViewModel() {

  companion object {
    const val TOP_ENSEMBLES_COUNT = 10
  }

  val statisticsUiState = combine(
    ensembleRepository.getCountEnsembles(),
    articleRepository.getCountArticles(),
    articleRepository.getCountArticleImages(),
    ensembleRepository.getMostPopularEnsembles(TOP_ENSEMBLES_COUNT),
    ensembleRepository.getMostPopularArticlesEnsembleCount(1),
    ensembleRepository.getMostPopularArticlesImageCount(1),
  ){
    StatisticsUiState(
      ensembleCount = it[0] as Int,
      articleCount = it[1] as Int,
      articleImageCount = it[2] as Int,
      topEnsembles = it[3] as List<EnsembleArticleCount>,
      topArticleMostEnsemblesCount = (it[4] as List<ArticleEnsembleCount>).firstOrNull()?.ensembleCount ?: 0,
      topArticleMostImagesCount = it[5] as LazyUriStrings,
    )
  }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = StatisticsUiState(
          ensembleCount = 0,
          articleCount = 0,
          articleImageCount = 0,
          topEnsembles = emptyList(),
          topArticleMostEnsemblesCount = 0,
          topArticleMostImagesCount = LazyUriStrings.Empty,
        ),
      )
}
