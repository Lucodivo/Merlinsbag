package com.inasweaterpoorlyknit.inknit.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.inknit.common.PLACEHOLDER_BITMAP
import com.inasweaterpoorlyknit.inknit.image.SegmentedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class AddArticleViewModel(application: Application) : AndroidViewModel(application) {

  val processedBitmap = mutableStateOf(PLACEHOLDER_BITMAP)

  val segmentedImage = SegmentedImage()

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

  private fun refreshProcessedBitmap() { processedBitmap.value = segmentedImage.subjectBitmap }

  fun onWidenClicked() = segmentedImage.decreaseThreshold().also { refreshProcessedBitmap() }
  fun onFocusClicked() = segmentedImage.increaseThreshold().also { refreshProcessedBitmap() }
  fun onPrevClicked() = segmentedImage.prevSubject().also { refreshProcessedBitmap() }
  fun onNextClicked() = segmentedImage.nextSubject().also { refreshProcessedBitmap() }
}