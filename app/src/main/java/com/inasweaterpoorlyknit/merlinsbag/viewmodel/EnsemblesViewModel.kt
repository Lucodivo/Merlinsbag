package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.listMap
import com.inasweaterpoorlyknit.core.data.ArticleRepository
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
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

data class EnsemblesUiState(
    val ensembles: List<Pair<Ensemble, LazyUriStrings>>?,
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
    val ensemblesRepository: EnsembleRepository,
): ViewModel() {
  val showAddEnsembleDialog = MutableStateFlow(false)
  private lateinit var articleImages: LazyArticleThumbnails

  val ensemblesUiState: StateFlow<EnsemblesUiState> =
      combine(
        ensemblesRepository.getAllEnsembleArticleThumbnails().listMap { Pair(it.ensemble, it.thumbnails) },
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
          ensembles = null,
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
      val articleIds = saveEnsembleData.articleIndices.map { articleImages.getArticleId(it) }
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