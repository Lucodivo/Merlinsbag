package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.common.Event
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
) : ViewModel() {

  private lateinit var lazyArticleImages: LazyArticleThumbnails
  var takePictureUri: Uri? = null

  val articleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
    .onEach { lazyArticleImages = it }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(),
      initialValue = LazyUriStrings.Empty,
    )

   val launchCamera = mutableStateOf(Event<Uri>(null))

  fun onDelete(articleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    val articleIds = List(articleIndices.size){ lazyArticleImages.getArticleId(articleIndices[it]) }
    articleRepository.deleteArticles(articleIds)
  }

  fun onTakePicture(context: Context) {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, "${timestampFileName()}.jpg")
      put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
      put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }
    takePictureUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    takePictureUri?.let { launchCamera.value = Event(takePictureUri) }
  }
}