package com.inasweaterpoorlyknit.inknit.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.core.database.repository.ClothingArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
  private val clothingArticleRepository: ClothingArticleRepository,
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

  data class ClothingThumbnails(
    val articleId: String,
    val thumbnailUri: String,
  )

  val thumbnailDetails: LiveData<List<ClothingThumbnails>>
    get() = clothingArticleRepository.getAllClothingArticlesWithImages().map { clothingArticlesWithImages ->
        clothingArticlesWithImages.map {
          ClothingThumbnails(
            articleId = it.clothingArticleEntity.id,
            thumbnailUri = it.images[0].thumbnailUri
          )
        }
      }
}