package com.inasweaterpoorlyknit.inknit.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.core.database.repository.ArticleRepository
import com.inasweaterpoorlyknit.inknit.image.SegmentedImage
import com.inasweaterpoorlyknit.inknit.ui.timestampFileName
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Helps avoid events from being handled multiple times after reconfiguration
open class Event<out T>(private val content: T?) {
  var hasBeenHandled = false
    private set

  fun getContentIfNotHandled(): T? {
    return if (!hasBeenHandled) {
      hasBeenHandled = true
      content
    } else null
  }
}

@HiltViewModel(assistedFactory = AddArticleViewModel.AddArticleViewModelFactory::class)
class AddArticleViewModel @AssistedInject constructor(
  @Assisted("imageUriStrings") private val imageUriStrings: List<String>,
  private val application: Application,
  private val articleRepository: ArticleRepository,
) : ViewModel() {

  @AssistedFactory
  interface AddArticleViewModelFactory {
    fun create(
      @Assisted("imageUriStrings") uriImageStrings: List<String>
    ): AddArticleViewModel
  }

  private var processingImageIndex = -1
  private val rotations = arrayOf(0.0f, 90.0f, 180.0f, 270.0f)
  private var rotationIndex = 0

  val processing = mutableStateOf(true)
  val multipleSubjects = mutableStateOf(false)
  val processedBitmap = mutableStateOf<Bitmap?>(null)
  val rotation = mutableFloatStateOf(rotations[rotationIndex])
  val finished = mutableStateOf(Event<Unit>(null))
  val noSubjectFound = mutableStateOf(Event<Unit>(null))

  private val segmentedImage = SegmentedImage()

  private fun processNextImage() {
    processing.value = true
    multipleSubjects.value = false
    processedBitmap.value = null
    rotationIndex = 0
    rotation.floatValue = rotations[rotationIndex]

    processingImageIndex += 1
    if(processingImageIndex > imageUriStrings.lastIndex){
      finished.value = Event(Unit)
    } else {
      viewModelScope.launch(Dispatchers.Default) {
        Uri.parse(imageUriStrings[processingImageIndex])?.let { imageUri ->
          try {
            segmentedImage.process(application, imageUri) { success ->
              if(success){
                if(segmentedImage.subjectsFound) {
                  multipleSubjects.value = segmentedImage.subjectCount > 1
                  refreshProcessedBitmap()
                } else {
                  noSubjectFound.value = Event(Unit)
                  processNextImage()
                }
              } else{
                Log.e("processImage()", "ML Kit failed to process image")
              }
            }
          } catch (e: IOException) { Log.e("processImage()", "ML Kit failed to open image - ${e.message}") }
        }
      }
    }
  }

  init {
    processNextImage()
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

  fun onWidenClicked(){
    processing.value = true
    segmentedImage.decreaseThreshold()
    refreshProcessedBitmap()
  }
  fun onFocusClicked() {
    processing.value = true
    segmentedImage.increaseThreshold()
    refreshProcessedBitmap()
  }
  fun onPrevClicked(){
    processing.value = true
    segmentedImage.prevSubject()
    refreshProcessedBitmap()
  }
  fun onNextClicked(){
    processing.value = true
    segmentedImage.nextSubject()
    refreshProcessedBitmap()
  }

  fun onRotateCW(){
    rotationIndex = if(rotationIndex == rotations.lastIndex) 0 else rotationIndex + 1
    rotation.floatValue = rotations[rotationIndex]
  }
  fun onRotateCCW(){
    rotationIndex = if(rotationIndex == 0) rotations.lastIndex else rotationIndex - 1
    rotation.floatValue = rotations[rotationIndex]
  }

  fun onSave(){
    val rotationMatrix = Matrix()
    rotationMatrix.postRotate(rotation.floatValue)
    val fullBitmapToSave = Bitmap.createBitmap(
      segmentedImage.subjectBitmap,
      segmentedImage.subjectBoundingBox.minX,
      segmentedImage.subjectBoundingBox.minY,
      segmentedImage.subjectBoundingBox.width,
      segmentedImage.subjectBoundingBox.height,
      rotationMatrix,
      false // e.g. bilinear filtering of source
    )

    viewModelScope.launch(Dispatchers.IO) {
      var bitmapWidth = fullBitmapToSave.width
      var bitmapHeight = fullBitmapToSave.height
      while (bitmapWidth > 300 || bitmapHeight > 300) {
        bitmapWidth /= 2
        bitmapHeight /= 2
      }
      val thumbnailBitmapToSave = fullBitmapToSave.scale(bitmapWidth, bitmapHeight)

      val directory = File(application.filesDir, "articles").apply{ mkdirs() }
      val filenameBase = timestampFileName()
      val imageFilename = "${filenameBase}_full.webp"
      val thumbnailFilename = "${filenameBase}_thumb.webp"
      val imageFile = File(directory, imageFilename)
      val thumbnailFile = File(directory, thumbnailFilename)
      @Suppress("DEPRECATION")
      val compressionFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP

      // save article full bitmap
      FileOutputStream(imageFile).use { outStream ->
        // Compress the bitmap into a JPEG (you could also use PNG)
        fullBitmapToSave.compress(compressionFormat, 100, outStream)
        // Flush and close the output stream
        outStream.flush()
      }

      // save article thumbnail bitmap
      FileOutputStream(thumbnailFile).use { outStream ->
        // Compress the bitmap into a JPEG (you could also use PNG)
        thumbnailBitmapToSave.compress(compressionFormat, 100, outStream)
        // Flush and close the output stream
        outStream.flush()
      }
      articleRepository.insertArticle(
        imageUri = imageFile.toUri().toString(),
        thumbnailUri = thumbnailFile.toUri().toString()
      )
    }
    processNextImage()
  }
}