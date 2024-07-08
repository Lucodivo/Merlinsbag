package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.EnsembleCount
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatisticsUiState(
  val ensembleCount: Int,
  val articleCount: Int,
  val topEnsembles: List<EnsembleCount>,
  val topArticleDecorationCount: Long,
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    ensembleRepository: EnsembleRepository,
    articleRepository: ArticleRepository,
): ViewModel() {

  companion object {
    const val topEnsembleCount = 5
  }

  val statisticsUiState = combine(
    ensembleRepository.getCountEnsembles(),
    articleRepository.getCountArticles(),
    ensembleRepository.getMostPopularEnsembles(topEnsembleCount),
    ensembleRepository.getMostPopularArticles(1),
  ){ ensembleCount, articleCount, mostPopularEnsembles, topArticleDecorationCount ->
    StatisticsUiState(
      ensembleCount = ensembleCount,
      articleCount = articleCount,
      topEnsembles = mostPopularEnsembles,
      topArticleDecorationCount = topArticleDecorationCount.firstOrNull()?.count ?: 0,
    )
  }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = StatisticsUiState(
          ensembleCount = 0,
          articleCount = 0,
          topEnsembles = emptyList(),
          topArticleDecorationCount = 0,
        ),
      )
}
