package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.ArticleImage
import com.inasweaterpoorlyknit.core.database.model.toExternalModel
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.database.repository.EnsembleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Ensemble(
  val id: String,
  val name: String,
  val articles: List<ArticleImage>
)

data class EnsemblesUiState(
  val ensembles: List<Ensemble> = emptyList(),
  val showAddEnsembleDialog: Boolean = false,
  val addEnsembleDialogArticles: List<ArticleWithImages> = emptyList(),
)

data class SaveEnsembleData(
  val title: String,
  val articleIds: List<String>,
)

@HiltViewModel
class EnsemblesViewModel @Inject constructor(
  val articleRepository: ArticleRepository,
  val ensemblesRepository: EnsembleRepository
): ViewModel() {

  val showAddEnsembleDialog = MutableStateFlow(false)
  val ensemblesUiState: StateFlow<EnsemblesUiState> =
    combine(
      ensemblesRepository.getAllEnsembleArticleImages().map { allEnsembleArticleImages ->
        allEnsembleArticleImages.map {
          Ensemble(
            id = it.ensemble.id,
            name = it.ensemble.title,
            articles = it.articles.map { article ->
              ArticleImage(
                articleId = article.articleId,
                uri = article.uri,
                thumbUri = article.thumbUri,
              )
            }
          )
        }
      },
      showAddEnsembleDialog,
      articleRepository.getAllArticlesWithImages(),
    ) { ensembles, showDialog, articleImages ->
      EnsemblesUiState(
        ensembles = ensembles,
        showAddEnsembleDialog = showDialog,
        addEnsembleDialogArticles = articleImages,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = EnsemblesUiState()
    )

  private fun closeDialog() { showAddEnsembleDialog.value = false }
  fun onClickAddEnsemble() { showAddEnsembleDialog.value = true }
  fun onClickCloseAddEnsembleDialog() = closeDialog()
  fun onClickSaveAddEnsembleDialog(saveEnsembleData: SaveEnsembleData) {
    closeDialog()
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.insertEnsemble(
        saveEnsembleData.title,
        saveEnsembleData.articleIds,
      )
    }
  }
}