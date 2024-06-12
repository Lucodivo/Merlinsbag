package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.data.repository.ArticleRepository
import com.inasweaterpoorlyknit.core.common.Event
import com.inasweaterpoorlyknit.merlinsbag.image.SegmentedImage
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
  val noSubjectFound = mutableStateOf(Event<Unit>(null))

  private val segmentedImage = SegmentedImage()

  init {
    processNextImage()
  }

  override fun onCleared() {
    super.onCleared()
    segmentedImage.cleanup()
  }

  private fun processNextImage() {
    processingImageIndex += 1
    if(processingImageIndex > imageUriStrings.lastIndex) {
      finished.value = Event(Unit)
    } else {
      processedBitmap.value = null
      rotationIndex = 0
      rotation.floatValue = rotations[rotationIndex]
      processing.value = true
      viewModelScope.launch(Dispatchers.Default) {
        Uri.parse(imageUriStrings[processingImageIndex])?.let { imageUri ->
          try {
            segmentedImage.process(application, imageUri) { success ->
              if(success) {
                if(segmentedImage.subjectsFound) {
                  refreshProcessedBitmap()
                } else {
                  noSubjectFound.value = Event(Unit)
                  processNextImage()
                }
              } else {
                Log.e("processImage()", "ML Kit failed to process image")
              }
            }
          } catch(e: IOException){
            Log.e("processImage()", "ML Kit failed to open image - ${e.message}")
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
    val rotationMatrix = Matrix()
    rotationMatrix.postRotate(rotation.floatValue)
    val bitmapToSave = Bitmap.createBitmap(
      segmentedImage.subjectBitmap,
      segmentedImage.subjectBoundingBox.minX,
      segmentedImage.subjectBoundingBox.minY,
      segmentedImage.subjectBoundingBox.width,
      segmentedImage.subjectBoundingBox.height,
      rotationMatrix,
      false // e.g. bilinear filtering of source
    )
    viewModelScope.launch(Dispatchers.IO) {
      articleRepository.insertArticle(bitmapToSave)
    }
    nextSubject()
  }
}