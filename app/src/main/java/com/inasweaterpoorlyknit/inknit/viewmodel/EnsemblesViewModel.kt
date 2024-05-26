package com.inasweaterpoorlyknit.inknit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleThumbnail(
  val articleId: String,
  val thumbUri: String,
)

data class EnsemblesUiState(
  val ensembles: List<LazyEnsembleThumbnails>,
  val showAddEnsembleDialog: Boolean,
  val articleImages: LazyUriStrings,
)

data class SaveEnsembleData(
  val title: String,
  val articleIndices: List<Int>,
)

@HiltViewModel
class EnsemblesViewModel @Inject constructor(
  articleRepository: ArticleRepository,
  val ensemblesRepository: EnsembleRepository
): ViewModel() {
  val showAddEnsembleDialog = MutableStateFlow(false)
  private lateinit var articleImages: LazyArticleThumbnails

  val ensemblesUiState: StateFlow<EnsemblesUiState> =
    combine(
      ensemblesRepository.getAllEnsembleArticleThumbnails(),
      showAddEnsembleDialog,
      articleRepository.getAllArticlesWithThumbnails().onEach { articleImages = it },
    ) { ensembles, showDialog, articleImages ->
      EnsemblesUiState(
        ensembles = ensembles,
        showAddEnsembleDialog = showDialog,
        articleImages = articleImages,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = EnsemblesUiState(
        ensembles = emptyList(),
        showAddEnsembleDialog = false,
        articleImages = LazyUriStrings.Empty,
      ),
    )

  private fun closeDialog() { showAddEnsembleDialog.value = false }
  fun onClickAddEnsemble() { showAddEnsembleDialog.value = true }
  fun onClickCloseAddEnsembleDialog() = closeDialog()
  fun onClickSaveAddEnsembleDialog(saveEnsembleData: SaveEnsembleData) {
    closeDialog()
    if(saveEnsembleData.title.isNotEmpty() || saveEnsembleData.articleIndices.isNotEmpty()) {
      val articleIds = saveEnsembleData.articleIndices.map{ articleImages.getArticleId(it) }
      viewModelScope.launch(Dispatchers.IO) {
        ensemblesRepository.insertEnsemble(
          saveEnsembleData.title,
          articleIds,
        )
      }
    }
  }

  companion object {
    val MAX_ENSEMBLE_TITLE_LENGTH = 128
  }
}