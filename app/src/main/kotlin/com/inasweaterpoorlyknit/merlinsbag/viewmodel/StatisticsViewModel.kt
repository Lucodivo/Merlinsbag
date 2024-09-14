package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.inasweaterpoorlyknit.core.common.combine
import com.inasweaterpoorlyknit.core.common.multiIfLet
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleCount
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.StatisticsUIState.ArticleWithMostEnsembles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object StatisticsUIEffect
object StatisticsUIEvent

sealed interface StatisticsUIState{
  data class ArticleWithMostEnsembles(
      val count: Int,
      val uriStrings: List<String>,
  )

  data object Loading: StatisticsUIState
  data class Success(
      val ensembleCount: Int,
      val articleCount: Int,
      val articleImageCount: Int,
      val ensemblesWithMostArticles: List<EnsembleArticleCount>,
      val articleWithMostImagesUriStrings: List<String>,
      val articleWithMostEnsembles: ArticleWithMostEnsembles,
  ): StatisticsUIState
}

const val TOP_ENSEMBLES_COUNT = 10

@HiltViewModel
class StatisticsViewModel @Inject constructor(
  val statisticsPresenter: StatisticsUIStateManager
): MoleculeViewModel<StatisticsUIEvent, StatisticsUIState, StatisticsUIEffect>(uiStateManager = statisticsPresenter)

class StatisticsUIStateManager @Inject constructor(
    val articleRepository: ArticleRepository,
    val ensembleRepository: EnsembleRepository,
): ComposeUIStateManager<StatisticsUIEvent, StatisticsUIState, StatisticsUIEffect> {
  override var cachedState = StatisticsUIState.Loading

  @Composable
  override fun uiState(
      uiEvents: Flow<StatisticsUIEvent>,
      launchUiEffect: (StatisticsUIEffect) -> Unit
  ): StatisticsUIState {
    val ensembleCount by remember { ensembleRepository.getCountEnsembles () }.collectAsState(null)
    val articleCount by remember { articleRepository.getCountArticles() }.collectAsState(null)
    val articleImageCount by remember { articleRepository.getCountArticleImages() }.collectAsState(null)
    val mostPopularEnsembles by remember { ensembleRepository.getMostPopularEnsembles(TOP_ENSEMBLES_COUNT) }.collectAsState(null)
    val mostPopularArticleByImages by remember {
      articleRepository.getMostPopularArticlesImageCount(1)
          .map {
            if(it.isEmpty()) emptyList() else it.getUriStrings(0)
          }
    }.collectAsState(null)
    val mostPopularArticleByEnsembles by remember {
      ensembleRepository.getMostPopularArticlesEnsembleCount(1)
          .map {
            if(it.first.isEmpty() || it.second.isEmpty()) ArticleWithMostEnsembles(0, emptyList())
            else ArticleWithMostEnsembles(it.first[0], it.second.getUriStrings(0))
          }
    }.collectAsState(null)

    LaunchedEffect(Unit) {
      uiEvents.collect { /* actions */ }
    }

    return multiIfLet(ensembleCount, articleCount, articleImageCount, mostPopularEnsembles, mostPopularArticleByImages, mostPopularArticleByEnsembles){
      ensembleCount, articleCount, articleImageCount, mostPopularEnsembles, mostPopularArticleByImages, mostPopularArticleByEnsembles ->
      StatisticsUIState.Success(
        ensembleCount = ensembleCount,
        articleCount = articleCount,
        articleImageCount = articleImageCount,
        ensemblesWithMostArticles = mostPopularEnsembles,
        articleWithMostImagesUriStrings = mostPopularArticleByImages,
        articleWithMostEnsembles = mostPopularArticleByEnsembles,
      )
    } ?: StatisticsUIState.Loading
  }
}