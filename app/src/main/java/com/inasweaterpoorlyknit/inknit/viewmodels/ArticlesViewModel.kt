package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ArticleThumbnails(
  val articleId: String,
  val thumbnailUri: String,
)

data class ArticlesUiState(
  val showPermissionsAlert: Boolean = false,
  val openSettings: Event<Unit> = Event<Unit>(null),
  val thumbnailUris: List<ArticleThumbnails> = emptyList(),
)

@HiltViewModel
class ArticlesViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
) : ViewModel() {

  private val _showPermissionsAlert = MutableStateFlow(false)
  private val _openSettings = MutableStateFlow(Event<Unit>(null))
  private val articleThumbnails = articleRepository.getAllArticlesWithImages().map { articlesWithImages ->
      articlesWithImages.map {
        ArticleThumbnails(
          articleId = it.articleId,
          thumbnailUri = it.images[0].thumbUri
        )
      }
    }
  val articlesUiState = combine(
    _showPermissionsAlert,
    _openSettings,
    articleThumbnails,
  ){ showPermissionsAlert, openSettings, articleThumbnails ->
    ArticlesUiState(
      showPermissionsAlert = showPermissionsAlert,
      openSettings = openSettings,
      thumbnailUris = articleThumbnails,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(),
    initialValue = ArticlesUiState()
  )

  fun userCheckedNeverAskAgain() { _showPermissionsAlert.value = true }
  fun onPermissionsAlertPositive() {
    _openSettings.value = Event(Unit)
    _showPermissionsAlert.value = false
  }
  fun onPermissionsAlertNegative() { _showPermissionsAlert.value = false }
  fun onPermissionsAlertOutside() { _showPermissionsAlert.value = false }
}