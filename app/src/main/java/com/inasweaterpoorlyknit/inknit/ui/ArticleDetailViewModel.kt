package com.inasweaterpoorlyknit.inknit.ui

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.inasweaterpoorlyknit.inknit.InKnitApplication
import kotlinx.coroutines.Dispatchers

class ArticleDetailViewModel(val application: InKnitApplication): AndroidViewModel(application) {

  // TODO: Not currently working
  val imageUri = liveData(Dispatchers.IO){
    val uri = application.database.clothingArticleWithImagesDao().getClothingArticleImages("TODO: GET ID")
    emit(uri)
  }

}