package com.inasweaterpoorlyknit.inknit.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.inasweaterpoorlyknit.inknit.InKnitApplication

class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
  data class ThumbnailDetails(
    val articleId: String,
    val thumbnailUri: Uri
  )


  private val inknitApplication: InKnitApplication
    get() = getApplication()

  val thumbnailUris: LiveData<List<Uri>>
    get() = inknitApplication.database.clothingArticleWithImagesDao()
              .getAllClothingArticlesWithImages().map { clothingArticlesWithImages ->
                clothingArticlesWithImages.map { it.images[0].thumbnailUri!! }
              }

  val imageUris: LiveData<List<ThumbnailDetails>>
    get() = inknitApplication.database.clothingArticleWithImagesDao()
      .getAllClothingArticlesWithImages().map { clothingArticlesWithImages ->
        clothingArticlesWithImages.map {
          ThumbnailDetails(
            articleId = it.clothingArticleEntity.id,
            thumbnailUri = it.images[0].thumbnailUri!!
          )
        }
      }
}