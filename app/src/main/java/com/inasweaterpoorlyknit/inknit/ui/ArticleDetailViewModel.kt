package com.inasweaterpoorlyknit.inknit.ui

import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.inknit.InKnitApplication

class ArticleDetailViewModel(private val inknitApplication: InKnitApplication, private val clothingArticleId: String): AndroidViewModel(inknitApplication) {
  data class ArticleDetails(val imageUri: Uri)

  fun getArticleDetails(): LiveData<ArticleDetails?> {
    return inknitApplication.database.clothingArticleWithImagesDao()
      .getClothingArticleWithImagesLive(clothingArticleId).map {
        ArticleDetails(
          imageUri = it.images[0].uri!!
        )
    }
  }
}

class ArticleDetailViewModelFactory(
  private val application: InKnitApplication,
  private val userId: String
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ArticleDetailViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return ArticleDetailViewModel(application, userId) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}