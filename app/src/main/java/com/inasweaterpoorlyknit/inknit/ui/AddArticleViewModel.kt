package com.inasweaterpoorlyknit.inknit.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.inknit.InKnitApplication
import com.inasweaterpoorlyknit.inknit.image.SegmentedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class AddArticleViewModel(application: Application) : AndroidViewModel(application) {

  val processedBitmap = mutableStateOf<Bitmap?>(null)

  val segmentedImage = SegmentedImage()
  val inknitApplication: InKnitApplication
    get() = getApplication() as InKnitApplication

  fun processImage(uri: Uri){
    viewModelScope.launch(Dispatchers.Default) {
      try {
        segmentedImage.process(getApplication(), uri) { success ->
          if (success) refreshProcessedBitmap()
          else Log.e("processImage()", "ML Kit failed to process image")
        }
      } catch (e: IOException) { Log.e("processImage()", "ML Kit failed to open image - ${e.message}") }
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
  }

  fun onWidenClicked() = segmentedImage.decreaseThreshold().also { refreshProcessedBitmap() }
  fun onFocusClicked() = segmentedImage.increaseThreshold().also { refreshProcessedBitmap() }
  fun onPrevClicked() = segmentedImage.prevSubject().also { refreshProcessedBitmap() }
  fun onNextClicked() = segmentedImage.nextSubject().also { refreshProcessedBitmap() }
}