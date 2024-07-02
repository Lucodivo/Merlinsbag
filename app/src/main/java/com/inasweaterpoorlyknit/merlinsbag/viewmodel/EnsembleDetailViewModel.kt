package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThumbnailUiState(
    val ensembleArticleThumbnailUris: LazyUriStrings,
    val addArticleThumbnailUris: LazyUriStrings,
)

@HiltViewModel(assistedFactory = EnsembleDetailViewModel.EnsembleDetailViewModelFactory::class)
class EnsembleDetailViewModel @AssistedInject constructor(
    @Assisted private val ensembleId: String,
    private val ensemblesRepository: EnsembleRepository,
    private val articleRepository: ArticleRepository,
): ViewModel() {
  private lateinit var ensemble: Ensemble
  private lateinit var ensembleArticles: LazyArticleThumbnails
  private lateinit var addArticles: LazyArticleThumbnails
  private lateinit var ensembleArticleIds: Set<String>
  val titleChangeError = mutableStateOf(Event<Int>(null))

  fun onTitleChanged(newTitle: String) = viewModelScope.launch(Dispatchers.IO) {
    if(ensemblesRepository.isEnsembleTitleUnique(newTitle).first()){
      ensemblesRepository.updateEnsemble(ensemble.copy(title = newTitle))
    } else {
      titleChangeError.value = Event(R.string.ensemble_with_title_already_exists)
    }
  }

  fun removeEnsembleArticles(articleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.deleteArticlesFromEnsemble(ensemble.id, articleIndices.map { ensembleArticles.getArticleId(it) })
  }

  fun addEnsembleArticles(addArticleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.addArticlesToEnsemble(ensemble.id, addArticleIndices.map { addArticles.getArticleId(it) })
  }

  fun deleteEnsemble() = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.deleteEnsemble(ensemble.id)
  }

  @AssistedFactory
  interface EnsembleDetailViewModelFactory {
    fun create(ensembleId: String): EnsembleDetailViewModel
  }

  val ensembleTitle = ensemblesRepository.getEnsemble(ensembleId)
      .onEach { ensemble = it }
      .map { it?.title ?: "" } // Can be null after ensemble is deleted
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = "",
      )

  val ensembleUiState = combine(
    ensemblesRepository.getEnsembleArticleThumbnails(ensembleId).onEach { articlesWithImages ->
      ensembleArticles = articlesWithImages
      ensembleArticleIds = ensembleArticles.articleIds().toSet()
    },
    articleRepository.getAllArticlesWithThumbnails(),
  ) { ensembleThumbnailUris, allArticles ->
    addArticles = allArticles.filter { !ensembleArticleIds.contains(it.articleId) }
    ThumbnailUiState(
      ensembleArticleThumbnailUris = ensembleThumbnailUris,
      addArticleThumbnailUris = addArticles,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
    initialValue = ThumbnailUiState(
      ensembleArticleThumbnailUris = LazyUriStrings.Empty,
      addArticleThumbnailUris = LazyUriStrings.Empty,
    )
  )
}