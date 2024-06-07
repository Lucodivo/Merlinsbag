package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.common.timestampFileName
import com.inasweaterpoorlyknit.core.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
  private val articleRepository: ArticleRepository,
  userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {

  private lateinit var lazyArticleImages: LazyArticleThumbnails
  var takePictureUri: Uri? = null

  val articleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { lazyArticleImages = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = LazyUriStrings.Empty,
      )

  val showOnboarding: Flow<Boolean> = userPreferencesRepository.hasCompletedOnboarding.map{ !it }

  val launchCamera = mutableStateOf(Event<Uri>(null))

  fun onDelete(articleIndices: List<Int>) = viewModelScope.launch(Dispatchers.IO) {
    val articleIds = List(articleIndices.size) { lazyArticleImages.getArticleId(articleIndices[it]) }
    articleRepository.deleteArticles(articleIds)
  }

  fun onTakePicture(context: Context) {
    val pictureFilename = "${timestampFileName()}.jpg"
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val contentResolver = context.contentResolver
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, pictureFilename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
      }
      takePictureUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
      takePictureUri?.let { launchCamera.value = Event(takePictureUri) }
    } else {
      val publicPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
      val publicPictureFile = File(publicPicturesDir, pictureFilename)
      takePictureUri = publicPictureFile.toUri()
      launchCamera.value = Event(takePictureUri)
    }
  }
}