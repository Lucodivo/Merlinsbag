package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import com.inasweaterpoorlyknit.core.database.repository.EnsembleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EnsembleDetailUiState(
  val title: String,
  val articleThumbnailUris: List<String>,
)

@HiltViewModel(assistedFactory = EnsembleDetailViewModel.EnsembleDetailViewModelFactory::class)
class EnsembleDetailViewModel @AssistedInject constructor(
  @Assisted private val ensembleId: String,
  private val ensemblesRepository: EnsembleRepository
): ViewModel() {
  private lateinit var ensemble: EnsembleEntity
  private lateinit var ensembleArticles: List<ArticleWithImages>

  fun onTitleChanged(newTitle: String) {
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.updateEnsemble(
        ensemble.copy(
          title = newTitle
        )
      )
    }
  }

  fun removeEnsembleArticles(articleIds: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.deleteEnsembleArticles(ensemble.id, articleIds.map { ensembleArticles[it].articleId })
  }

  @AssistedFactory
  interface EnsembleDetailViewModelFactory {
    fun create(ensembleId: String): EnsembleDetailViewModel
  }

  val ensembleUiState = combine(
    ensemblesRepository.getEnsemble(ensembleId).onEach { ensemble = it },
    ensemblesRepository.getEnsembleArticleImages(ensembleId).onEach{ ensembleArticles = it }.listMap { it.images[0].thumbUri }
  ) { ensembleEntity, articleImages ->
    EnsembleDetailUiState(
      title = ensembleEntity.title,
      articleThumbnailUris = articleImages,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = EnsembleDetailUiState(title = "", articleThumbnailUris = emptyList())
  )
}