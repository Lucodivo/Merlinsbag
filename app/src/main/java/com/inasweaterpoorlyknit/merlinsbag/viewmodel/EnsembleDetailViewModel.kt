package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToArticleDetail
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

  @AssistedFactory
  interface EnsembleDetailViewModelFactory {
    fun create(ensembleId: String): EnsembleDetailViewModel
  }

  private lateinit var ensemble: Ensemble
  private lateinit var ensembleArticles: LazyArticleThumbnails
  private lateinit var addArticles: LazyArticleThumbnails
  private lateinit var ensembleArticleIds: Set<String>

  var editingTitle by mutableStateOf(false)
  var editMode by mutableStateOf(false)
  var showDeleteEnsembleDialog by mutableStateOf(false)
  var showAddArticlesDialog by mutableStateOf(false)
  val onBackEnabled get() = editMode

  // TODO: No mutableStateSetOf ??
  val _selectedEditArticleIndices = mutableStateMapOf<Int, Unit>()
  val _selectedAddArticleIndices = mutableStateMapOf<Int, Unit>()

  val selectedEditArticleIndices get() = _selectedEditArticleIndices.keys
  val selectedAddArticleIndices get() = _selectedAddArticleIndices.keys

  var titleChangeError by mutableStateOf(Event<Int>(null))
  var navigateToArticleDetail by mutableStateOf(Event<Pair<Int, String>>(null))
  var finished by mutableStateOf(Event<Unit>(null))

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

  fun onClickTitle() { editingTitle = true }

  fun onBack() { onClickMinimizeButtonControl() }
  fun onClickEdit() { editMode = true }
  fun onClickMinimizeButtonControl() {
    editMode = false
    if(_selectedEditArticleIndices.isNotEmpty()) _selectedEditArticleIndices.clear()
  }

  fun onClickArticle(index: Int) {
    if(editMode) {
      if(_selectedEditArticleIndices.containsKey(index)) _selectedEditArticleIndices.remove(index)
      else _selectedEditArticleIndices[index] = Unit
    } else {
      navigateToArticleDetail = Event(Pair(index, ensembleId))
    }
  }

  fun onClickArticleAddDialog(index: Int) {
    if(_selectedAddArticleIndices.containsKey(index)) _selectedAddArticleIndices.remove(index)
    else _selectedAddArticleIndices[index] = Unit
  }

  fun onClickCancelArticleSelection() { _selectedEditArticleIndices.clear() }

  fun onClickRemoveArticles() {
    val articleIds = _selectedEditArticleIndices.keys.map { ensembleArticles.getArticleId(it) }
    _selectedEditArticleIndices.clear()
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.deleteArticlesFromEnsemble(
        ensembleId = ensemble.id,
        articleIds = articleIds
      )
    }
  }

  fun onLongPressArticle(index: Int){
    if(!editMode) editMode = true
    if(_selectedEditArticleIndices.containsKey(index)) _selectedEditArticleIndices.remove(index)
    else _selectedEditArticleIndices[index] = Unit
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

  fun onDismissDeleteEnsembleDialog() { showDeleteEnsembleDialog = false }
  fun onClickPositiveDeleteEnsembleDialog() {
    viewModelScope.launch(Dispatchers.IO) {
      ensemblesRepository.deleteEnsemble(ensemble.id)
    }
    finished = Event(Unit)
    showDeleteEnsembleDialog = false
  }

  fun onClickAddArticles() { showAddArticlesDialog = true }
  fun onDismissAddArticlesDialog() {
    showAddArticlesDialog = false
    if(_selectedAddArticleIndices.isNotEmpty()) _selectedAddArticleIndices.clear()
  }

  fun onClickDeleteEnsemble() { showDeleteEnsembleDialog = true }
  fun onDismissEditTitle() { editingTitle = false }

  fun onClickConfirmAddArticles() {
    showAddArticlesDialog = false
    if(_selectedAddArticleIndices.isNotEmpty())
    {
      val articleIds = _selectedAddArticleIndices.keys.map { addArticles.getArticleId(it) }
      _selectedAddArticleIndices.clear()
      viewModelScope.launch(Dispatchers.IO) {
        ensemblesRepository.addArticlesToEnsemble(ensemble.id, articleIds)
      }
    }
  }
}