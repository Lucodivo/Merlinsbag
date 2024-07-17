package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticlesScreenEditMode
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
): ViewModel() {
  private lateinit var lazyArticleImagesCache: LazyArticleThumbnails

  var showDeleteArticlesAlert by mutableStateOf(false)
  var editMode by mutableStateOf(ArticlesScreenEditMode.DISABLED)
  val onBackEnabled get() = editMode != ArticlesScreenEditMode.DISABLED

  // TODO: No mutableStateSetOf ??
  val _selectedArticleIndices = mutableStateMapOf<Int, Unit>()
  val selectedArticleIndices = _selectedArticleIndices.keys

  var finish by mutableStateOf(Event<Unit>(null))
  var navigateToArticleDetail by mutableStateOf(Event<Int>(null))
  var navigateToCamera by mutableStateOf(Event<Unit>(null))
  var navigateToSettings by mutableStateOf(Event<Unit>(null))
  var navigateToAddArticle by mutableStateOf(Event<List<String>>(null))
  var launchPhotoAlbum by mutableStateOf(Event<Unit>(null))

  val articleThumbnails: StateFlow<LazyUriStrings?> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { lazyArticleImagesCache = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = null,
      )

  fun onClickArticle(index: Int){
    if(editMode == ArticlesScreenEditMode.ENABLED_SELECTED_ARTICLES) {
      if(_selectedArticleIndices.contains(index)) _selectedArticleIndices.remove(index)
      else _selectedArticleIndices[index] = Unit
    } else {
      navigateToArticleDetail = Event(index)
    }
  }

  fun onLongPressArticle(index: Int) {
    if(editMode != ArticlesScreenEditMode.ENABLED_SELECTED_ARTICLES) {
      editMode = ArticlesScreenEditMode.ENABLED_SELECTED_ARTICLES
      _selectedArticleIndices.clear()
    }
    if(_selectedArticleIndices.contains(index)) _selectedArticleIndices.remove(index)
    else _selectedArticleIndices[index] = Unit
  }

  fun onClickClearSelection() {
    _selectedArticleIndices.clear()
    editMode = ArticlesScreenEditMode.ENABLED_GENERAL
  }
  fun onClickSettings(){ navigateToSettings = Event(Unit) }
  fun onClickAddPhotoCamera(){ navigateToCamera = Event(Unit) }
  fun onClickAddPhotoAlbum(){ launchPhotoAlbum = Event(Unit) }
  fun onPhotoAlbumResults(uris: List<Uri>){
    if(uris.isNotEmpty()) navigateToAddArticle = Event(uris.map{ it.toString() })
  }

  fun onClickEdit(){ editMode = ArticlesScreenEditMode.ENABLED_GENERAL }
  fun onClickMinimizeButtonControl(){
    editMode = ArticlesScreenEditMode.DISABLED
    if(_selectedArticleIndices.isNotEmpty()) _selectedArticleIndices.clear()
  }

  fun onClickDelete(){ showDeleteArticlesAlert = true  }
  fun onDismissDeleteArticlesAlert() { showDeleteArticlesAlert = false }
  fun onConfirmDeleteArticlesAlert() {
    showDeleteArticlesAlert = false
    val articleIds = _selectedArticleIndices.keys.map { lazyArticleImagesCache.getArticleId(it) }
    _selectedArticleIndices.clear()
    viewModelScope.launch(Dispatchers.IO) { articleRepository.deleteArticles(articleIds) }
  }

  fun onBack() {
    if(editMode != ArticlesScreenEditMode.DISABLED) editMode = ArticlesScreenEditMode.DISABLED
    else { finish = Event(Unit) }
  }
}