package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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

  fun onTitleChanged(newTitle: String) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.updateEnsemble(ensemble.copy(title = newTitle))
  }

  fun removeEnsembleArticles(articleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.deleteEnsembleArticles(ensemble.id, articleIndices.map { ensembleArticles.getArticleId(it) })
  }

  fun addEnsembleArticles(addArticleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.addEnsembleArticles(ensemble.id, addArticleIndices.map { addArticles.getArticleId(it) })
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
      .map { it.title }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
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
    started = SharingStarted.WhileSubscribed(),
    initialValue = ThumbnailUiState(
      ensembleArticleThumbnailUris = LazyUriStrings.Empty,
      addArticleThumbnailUris = LazyUriStrings.Empty,
    )
  )
}