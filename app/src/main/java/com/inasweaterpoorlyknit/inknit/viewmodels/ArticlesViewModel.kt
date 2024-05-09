package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
) : ViewModel() {

  val _showPermissionsAlert = MutableLiveData<Boolean>(false)
  val showPermissionsAlert: LiveData<Boolean>
    get() = _showPermissionsAlert

  val _openSettings = MutableLiveData<Event<Unit>>()
  val openSettings: LiveData<Event<Unit>>
    get() = _openSettings

  fun userCheckedNeverAskAgain() { _showPermissionsAlert.value = true }
  fun onPermissionsAlertPositive() {
    _openSettings.value = Event(Unit)
    _showPermissionsAlert.value = false
  }
  fun onPermissionsAlertNegative() { _showPermissionsAlert.value = false }
  fun onPermissionsAlertOutside() { _showPermissionsAlert.value = false }

  data class ArticleThumbnails(
    val articleId: String,
    val thumbnailUri: String,
  )

  val thumbnailDetails: LiveData<List<ArticleThumbnails>>
    get() = articleRepository.getAllArticlesWithImages().map { articlesWithImages ->
        articlesWithImages.map {
          ArticleThumbnails(
            articleId = it.articleEntity.id,
            thumbnailUri = it.images[0].thumbnailUri
          )
        }
      }
}