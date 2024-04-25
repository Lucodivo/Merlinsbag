package com.inasweaterpoorlyknit.inknit.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.inknit.InKnitApplication

class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
  private val inknitApplication: InKnitApplication
    get() = getApplication()

  val thumbnailUris: LiveData<List<Uri>>
    get() = inknitApplication.database.clothingArticleWithImagesDao()
              .getAllClothingArticlesWithImagesLive().map {  clothingArticlesWithImages ->
                clothingArticlesWithImages.map { it.images[0].thumbnailUri!! }
              }

  val imageUris: LiveData<List<Uri>>
    get() = inknitApplication.database.clothingArticleWithImagesDao()
      .getAllClothingArticlesWithImagesLive().map {  clothingArticlesWithImages ->
        clothingArticlesWithImages.map { it.images[0].uri!! }
      }
}