package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.ArticleEnsembleCount
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleCount
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    ensembleRepository: EnsembleRepository,
    articleRepository: ArticleRepository,
): ViewModel() {

  companion object {
    const val TOP_ENSEMBLES_COUNT = 10
  }

  val ensembleCount: StateFlow<Int> = ensembleRepository.getCountEnsembles()
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = 0,
      )
  val articleCount: StateFlow<Int> = articleRepository.getCountArticles()
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = 0,
      )
  val articleImagesCount: StateFlow<Int> = articleRepository.getCountArticleImages()
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = 0,
      )
  val topEnsembles: StateFlow<List<EnsembleArticleCount>> = ensembleRepository.getMostPopularEnsembles(TOP_ENSEMBLES_COUNT)
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = emptyList(),
      )
  val topArticleMostImagesCount: StateFlow<List<String>> = articleRepository.getMostPopularArticlesImageCount(1).map {
    if(it.isEmpty()) emptyList() else it.getUriStrings(0)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
    initialValue = emptyList(),
  )
  val topArticleMostEnsemblesCount: StateFlow<Pair<Int, List<String>>> = ensembleRepository.getMostPopularArticlesEnsembleCount(1).map {
    if(it.first.isEmpty() || it.second.isEmpty()) Pair(0, emptyList()) else Pair(it.first[0], it.second.getUriStrings(0))
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
    initialValue = Pair(0, emptyList()),
  )
}
