package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.core.common.profiling.Timer
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.ml.image.SegmentedImage
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

@HiltViewModel(assistedFactory = AddArticleViewModel.AddArticleViewModelFactory::class)
class AddArticleViewModel @AssistedInject constructor(
    @Assisted("imageUriStrings") private val imageUriStrings: List<String>,
    private val application: Application,
    private val articleRepository: ArticleRepository,
): ViewModel() {

  @AssistedFactory
  interface AddArticleViewModelFactory {
    fun create(
        @Assisted("imageUriStrings") uriImageStrings: List<String>,
    ): AddArticleViewModel
  }

  companion object {
    private val rotations = arrayOf(0.0f, 90.0f, 180.0f, 270.0f)
  }

  private var processingImageIndex = -1
  private var rotationIndex = 0

  val processing = mutableStateOf(true)
  val processedBitmap = mutableStateOf<Bitmap?>(null)
  val rotation = mutableFloatStateOf(rotations[rotationIndex])
  val finished = mutableStateOf(Event<Unit>(null))
  val userFacingError = mutableStateOf(Event<Int>(null))
  val attachArticleIndex = mutableStateOf<Int?>(null)

  private val segmentedImage = SegmentedImage()
  private var attachArticleThumbnailsCache: LazyArticleThumbnails? = null

  val attachArticleThumbnails: StateFlow<LazyUriStrings> = articleRepository.getAllArticlesWithThumbnails()
      .onEach { this.attachArticleThumbnailsCache = it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS),
        initialValue = LazyUriStrings.Empty
      )

  init {
    processNextImage()
  }

  fun removeAttachedArticle() { attachArticleIndex.value = null }
  fun addAttachedArticle(articleIndex: Int) { attachArticleIndex.value = articleIndex }

  override fun onCleared() {
    super.onCleared()
    segmentedImage.cleanup()
  }

  private fun processNextImage() {
    processingImageIndex += 1
    fun error(@StringRes msg: Int) {
      userFacingError.value = Event(msg)
      processNextImage()
    }
    if(processingImageIndex > imageUriStrings.lastIndex) {
      finished.value = Event(Unit)
    } else {
      processedBitmap.value = null
      rotationIndex = 0
      rotation.floatValue = rotations[rotationIndex]
      processing.value = true
      viewModelScope.launch(Dispatchers.Default) {
        val imageUri = Uri.parse(imageUriStrings[processingImageIndex])
        if(imageUri == null) {
          error(R.string.image_not_found)
        } else {
          try {
            segmentedImage.process(application, imageUri) { success ->
              when(success){
                SegmentedImage.ProcessSuccess.SUCCESS -> {
                  if(segmentedImage.subjectsFound) refreshProcessedBitmap()
                  else error(R.string.no_subject_found)
                }
                SegmentedImage.ProcessSuccess.FAILURE_MLKIT_MODULE_WAITING_TO_DOWNLOAD -> {
                  error(R.string.configuring_try_again_soon)
                }
                SegmentedImage.ProcessSuccess.FAILURE_IMAGE_NOT_FOUND -> {
                  error(R.string.image_not_found)
                }
                SegmentedImage.ProcessSuccess.FAILURE_IMAGE_NOT_RECOGNIZED -> {
                  error(R.string.image_not_recognized)
                }
              }
            }
          } catch(e: IOException) {
            Log.e("processImage()", "ML Kit failed to open image - ${e.message}")
            error(R.string.image_not_found)
          }
        }
      }
    }
  }

  private fun refreshProcessedBitmap() {
    // MutableState utilizes the function 'equals()' to determine if it should emit a value or not
    // In aim of better performance, segmentedImage.subjectBitmap is likely to be the same Bitmap
    // reference but edited in-place. This causes 'equals()' to return true because the reference
    // is the same. Setting the value of the processed Bitmap to anything else before setting it to
    // our desired value forces it to actually emit our desired value in all circumstances.
    processedBitmap.value = null
    processedBitmap.value = segmentedImage.subjectBitmap
    processing.value = false
  }

  fun onWidenClicked() {
    processing.value = true
    segmentedImage.decreaseThreshold()
    refreshProcessedBitmap()
  }

  fun onFocusClicked() {
    processing.value = true
    segmentedImage.increaseThreshold()
    refreshProcessedBitmap()
  }

  fun onRotateCW() {
    rotationIndex = if(rotationIndex == rotations.lastIndex) 0 else rotationIndex + 1
    rotation.floatValue = rotations[rotationIndex]
  }

  fun onRotateCCW() {
    rotationIndex = if(rotationIndex == 0) rotations.lastIndex else rotationIndex - 1
    rotation.floatValue = rotations[rotationIndex]
  }

  fun onDiscard() {
    nextSubject()
  }

  fun nextSubject() {
    if(segmentedImage.subjectIndex == (segmentedImage.subjectCount - 1)) {
      processNextImage()
    } else {
      segmentedImage.nextSubject()
      refreshProcessedBitmap()
    }
  }

  fun onSave() {
    val stopWatch = Timer()
    lateinit var bitmapToSave: Bitmap
    val rotationMatrix = Matrix()
    rotationMatrix.postRotate(rotation.floatValue)
    bitmapToSave = Bitmap.createBitmap(
      segmentedImage.subjectBitmap,
      segmentedImage.subjectBoundingBox.minX,
      segmentedImage.subjectBoundingBox.minY,
      segmentedImage.subjectBoundingBox.width,
      segmentedImage.subjectBoundingBox.height,
      rotationMatrix,
      false // e.g. bilinear filtering of source
    )
    val attachmentArticleIndex = attachArticleIndex.value
    val attachmentArticleId = if(attachmentArticleIndex != null){
      attachArticleThumbnailsCache?.getArticleId(attachmentArticleIndex)
    } else null
    viewModelScope.launch(Dispatchers.Default) {
      nextSubject()
      if(attachmentArticleId == null) articleRepository.insertArticle(bitmapToSave) else articleRepository.insertArticleImage(bitmapToSave, attachmentArticleId)
      stopWatch.logElapsed("AddArticleViewModel", "Save article time")
    }
  }
}