package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.EnsembleEntity
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.EnsembleRepository
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
  val ensembleArticleThumbnailUris: List<String>,
  val addArticleThumbnailUris: List<String>,
)

@HiltViewModel(assistedFactory = EnsembleDetailViewModel.EnsembleDetailViewModelFactory::class)
class EnsembleDetailViewModel @AssistedInject constructor(
  @Assisted private val ensembleId: String,
  private val ensemblesRepository: com.inasweaterpoorlyknit.core.repository.EnsembleRepository,
  private val articleRepository: com.inasweaterpoorlyknit.core.repository.ArticleRepository,
): ViewModel() {
  private lateinit var ensemble: EnsembleEntity
  private lateinit var ensembleArticles: List<ArticleWithImages>
  private lateinit var addArticles: List<ArticleWithImages>
  private lateinit var ensembleArticleIds: Set<String>

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

  fun addEnsembleArticles(addArticleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    ensemblesRepository.addEnsembleArticles(ensemble.id, addArticleIndices.map { addArticles[it].articleId })
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
    ensemblesRepository.getEnsembleArticleImages(ensembleId).onEach{ articlesWithImages ->
      ensembleArticles = articlesWithImages
      ensembleArticleIds = ensembleArticles.map { it.articleId }.toSet()
    }.listMap { it.images[0].thumbUri },
    articleRepository.getAllArticlesWithImages(),
  ) { ensembleThumbnailUris, allArticles ->
    addArticles = allArticles.filterNot { ensembleArticleIds.contains(it.articleId) }
    ThumbnailUiState(
      ensembleArticleThumbnailUris = ensembleThumbnailUris,
      addArticleThumbnailUris = addArticles.map { it.images[0].thumbUri }
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = ThumbnailUiState(
      ensembleArticleThumbnailUris = emptyList(),
      addArticleThumbnailUris = emptyList(),
    )
  )
}