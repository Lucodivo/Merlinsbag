package com.inasweaterpoorlyknit.inknit.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inasweaterpoorlyknit.inknit.InKnitApplication
import com.inasweaterpoorlyknit.inknit.common.timestampAsString
import com.inasweaterpoorlyknit.inknit.image.SegmentedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Helps avoid events from being handled multiple times after reconfiguration
open class Event<out T>(private val content: T) {
  var hasBeenHandled = false
    private set

  fun getContentIfNotHandled(): T? {
    return if (!hasBeenHandled) {
      hasBeenHandled = true
      content
    } else null
  }
}

class AddArticleViewModel(application: Application) : AndroidViewModel(application) {

  private val rotations = arrayOf(0.0f, 90.0f, 180.0f, 270.0f)
  private var rotationIndex = 0

  val processedBitmap = mutableStateOf<Bitmap?>(null)
  val rotation = mutableFloatStateOf(rotations[rotationIndex])
  private val _shouldClose = MutableLiveData<Event<Boolean>>()
  val shouldClose: LiveData<Event<Boolean>>
    get() = _shouldClose


  private val segmentedImage = SegmentedImage()
  private val inknitApplication: InKnitApplication
    get() = getApplication()

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
  fun onRotateCW(){
    rotationIndex = if(rotationIndex == rotations.lastIndex) 0 else rotationIndex + 1
    rotation.floatValue = rotations[rotationIndex]
  }
  fun onRotateCCW(){
    rotationIndex = if(rotationIndex == 0) rotations.lastIndex else rotationIndex - 1
    rotation.floatValue = rotations[rotationIndex]
  }

  fun onSave(){
    viewModelScope.launch(Dispatchers.IO) {
      // TODO: Properly adjust for rotation specification

      val fullBitmapToSave = Bitmap.createBitmap(
        segmentedImage.subjectBitmap,
        segmentedImage.subjectBoundingBox.minX,
        segmentedImage.subjectBoundingBox.minY,
        segmentedImage.subjectBoundingBox.width,
        segmentedImage.subjectBoundingBox.height,
      )

      var bitmapWidth = fullBitmapToSave.width
      var bitmapHeight = fullBitmapToSave.height
      while (bitmapWidth > 300 || bitmapHeight > 300) {
        bitmapWidth /= 2
        bitmapHeight /= 2
      }
      val thumbnailBitmapToSave = fullBitmapToSave.scale(bitmapWidth, bitmapHeight)

      val filenameBase = timestampAsString()
      val imageFilename = filenameBase + "_full.webp"
      val thumbnailFilename = filenameBase + "_thumb.webp"
      val imageFile = File(inknitApplication.filesDir, imageFilename)
      val thumbnailFile = File(inknitApplication.filesDir, thumbnailFilename)
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

      inknitApplication.database.clothingArticleWithImagesDao().insertClothingArticle(imageUri = imageFile.toUri(), thumbnailUri = thumbnailFile.toUri())
      viewModelScope.launch(Dispatchers.Main) {
        _shouldClose.value = Event(true)
      }
    }
  }
}