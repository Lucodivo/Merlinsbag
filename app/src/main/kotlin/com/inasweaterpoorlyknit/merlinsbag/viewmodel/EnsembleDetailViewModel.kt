package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.data.repository.EnsembleRepository
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel.EditState.Disabled
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel.EditState.EnabledGeneral
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel.EditState.EnabledSelectedArticles
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

  enum class EditState {
    EnabledGeneral,
    EnabledSelectedArticles,
    Disabled
  }

  enum class DialogState {
    None,
    DeleteEnsemble,
    AddArticles,
  }

  sealed interface NavigationState {
    data object Finished: NavigationState
    data class ArticleDetail(val index: Int, val ensembleId: String): NavigationState
  }

  @AssistedFactory
  interface EnsembleDetailViewModelFactory {
    fun create(ensembleId: String): EnsembleDetailViewModel
  }

  private lateinit var ensemble: Ensemble
  private lateinit var ensembleArticles: LazyArticleThumbnails
  private lateinit var addArticles: LazyArticleThumbnails
  private lateinit var ensembleArticleIds: Set<String>

  var editingTitle by mutableStateOf(false)
  var editState by mutableStateOf(Disabled)
  var dialogState by mutableStateOf(DialogState.None)
  val onBackEnabled get() = editState != Disabled
  val selectedEditArticleIndices = mutableStateSetOf<Int>()
  val selectedAddArticleIndices = mutableStateSetOf<Int>()
  var titleChangeError by mutableStateOf(Event<Int>(null))
  var navigationEventState by mutableStateOf(Event<NavigationState>(null))

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

  private fun dismissDialog() {
    if(dialogState != DialogState.None) dialogState = DialogState.None
  }

  fun onClickTitle() { editingTitle = true }

  fun onBack() { onClickMinimizeButtonControl() }
  fun onClickEdit() { editState = EnabledGeneral }
  fun onClickMinimizeButtonControl() {
    editState = Disabled
    if(selectedEditArticleIndices.isNotEmpty()) selectedEditArticleIndices.clear()
  }

  fun onClickArticle(index: Int) {
    if(editState == EnabledSelectedArticles) {
      if(selectedEditArticleIndices.contains(index)) {
        selectedEditArticleIndices.remove(index)
        if(selectedEditArticleIndices.isEmpty()) editState = EnabledGeneral
      } else selectedEditArticleIndices.add(index)
    } else {
      navigationEventState = Event(NavigationState.ArticleDetail(index, ensembleId))
    }
  }

  fun onClickArticleAddDialog(index: Int) {
    if(selectedAddArticleIndices.contains(index)) {
      selectedAddArticleIndices.remove(index)
    } else selectedAddArticleIndices.add(index)
  }

  fun onClickCancelArticleSelection() { selectedEditArticleIndices.clear() }

  fun onClickRemoveArticles() {
    val articleIds = selectedEditArticleIndices.map { ensembleArticles.getArticleId(it) }
    selectedEditArticleIndices.clear()
    editState = EnabledGeneral
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.deleteArticlesFromEnsemble(
        ensembleId = ensemble.id,
        articleIds = articleIds
      )
    }
  }

  fun onLongPressArticle(index: Int){
    if(editState != EnabledSelectedArticles) editState = EnabledSelectedArticles
    if(selectedEditArticleIndices.contains(index)) {
      selectedEditArticleIndices.remove(index)
      if(selectedEditArticleIndices.isEmpty()) editState = EnabledGeneral
    } else selectedEditArticleIndices.add(index)
  }

  fun onTitleChanged(newTitle: String) {
    editingTitle = false
    viewModelScope.launch(Dispatchers.IO) {
      if(newTitle != ensemble.title){
        if(ensemblesRepository.isEnsembleTitleUnique(newTitle).first()){
          ensemblesRepository.updateEnsemble(ensemble.copy(title = newTitle))
        } else {
          titleChangeError = Event(R.string.ensemble_with_title_already_exists)
        }
      }
    }
  }

  fun onClickDeleteEnsemble() { dialogState = DialogState.DeleteEnsemble }
  fun onDismissDeleteEnsembleDialog() = dismissDialog()
  fun onClickPositiveDeleteEnsembleDialog() {
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.deleteEnsemble(ensemble.id)
    }
    navigationEventState = Event(NavigationState.Finished)
    dismissDialog()
  }

  fun onClickAddArticles() { dialogState = DialogState.AddArticles }
  fun onDismissAddArticlesDialog() {
    dismissDialog()
    if(selectedAddArticleIndices.isNotEmpty()) selectedAddArticleIndices.clear()
  }

  fun onDismissEditTitle() { editingTitle = false }

  fun onClickConfirmAddArticles() {
    dismissDialog()
    if(selectedAddArticleIndices.isNotEmpty())
    {
      val articleIds = selectedAddArticleIndices.map { addArticles.getArticleId(it) }
      selectedAddArticleIndices.clear()
      viewModelScope.launch(Dispatchers.IO) {
        ensemblesRepository.addArticlesToEnsemble(ensemble.id, articleIds)
      }
    }
  }
}