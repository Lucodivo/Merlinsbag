package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import com.inasweaterpoorlyknit.core.database.repository.EnsembleRepository
import com.inasweaterpoorlyknit.inknit.common.listMap
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
  lateinit var ensemble: EnsembleEntity

  fun onTitleChanged(newTitle: String) {
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.updateEnsemble(
        ensemble.copy(
          title = newTitle
        )
      )
    }
  }

  @AssistedFactory
  interface EnsembleDetailViewModelFactory {
    fun create(ensembleId: String): EnsembleDetailViewModel
  }

  val ensembleUiState = combine(
    ensemblesRepository.getEnsemble(ensembleId).onEach { ensemble = it },
    ensemblesRepository.getEnsembleArticleImages(ensembleId).listMap { it.images[0].thumbUri }
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