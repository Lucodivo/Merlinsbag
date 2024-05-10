package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.model.ArticleImage
import com.inasweaterpoorlyknit.core.database.repository.EnsembleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EnsembleDetailUiState(
  val title: String,
  val articles: List<ArticleImage>,
)

@HiltViewModel(assistedFactory = EnsembleDetailViewModel.EnsembleDetailViewModelFactory::class)
class EnsembleDetailViewModel @AssistedInject constructor(
  @Assisted private val ensembleId: String,
  private val ensemblesRepository: EnsembleRepository
): ViewModel() {

  @AssistedFactory
  interface EnsembleDetailViewModelFactory {
    fun create(ensembleId: String): EnsembleDetailViewModel
  }

  val ensembleUiState = combine(
    ensemblesRepository.getEnsemble(ensembleId),
    ensemblesRepository.getAllEnsembleArticleImages(ensembleId)
  ) { ensembleEntity, articleImages ->
    EnsembleDetailUiState(
      title = ensembleEntity.title,
      articles = articleImages,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = EnsembleDetailUiState(title = "", articles = emptyList())
  )
}
