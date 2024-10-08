package com.inasweaterpoorlyknit.core.ml.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.Subject
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

// PERFORMANCE TESTS
// Quick performance test on Samsung Galaxy A23 5G with 3060x4080 image of a single subject (denim jacket)
// Cold pass (first image processed)
//     Total Processing Time: 1.17s
//          Create InputImage: 399ms
//          Copy bitmap from InputImage to IntArray: 24ms
//          Image successfully processed by ML Kit: 687ms
//          Subject alpha masked to IntArray: 54ms
//          Subject alpha masked IntArray to bitmap: 10ms
// Hot pass (5th image processed)
//     Total Processing Time: 895.47ms
//          Create InputImage: 390ms
//          Copy bitmap from InputImage to IntArray: 8ms
//          Image successfully processed by ML Kit: 466ms
//          Subject alpha masked to IntArray: 27ms
//          Subject alpha masked IntArray to bitmap: 4ms
class SegmentedImage(private val subjectSegmenter: SubjectSegmenter) {

  data class BoundingBox(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int) {
    val width: Int
      get() = maxX - minX
    val height: Int
      get() = maxY - minY
  }

  enum class ProcessSuccess {
    SUCCESS,
    FAILURE_MLKIT_MODULE_WAITING_TO_DOWNLOAD,
    FAILURE_IMAGE_NOT_FOUND,
    FAILURE_IMAGE_NOT_RECOGNIZED,
  }

  companion object {
    private val PLACEHOLDER_INT_ARRAY = IntArray(1)
    private const val CONFIDENCE_THRESHOLD_INCREMENT = 0.1f
    private const val MIN_CONFIDENCE_THRESHOLD = CONFIDENCE_THRESHOLD_INCREMENT
    private const val MAX_CONFIDENCE_THRESHOLD = 0.9f // NOTE: 1.0f threshold produces major artifact-ing
    private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.6f
    private const val TRANSPARENT_DEBUG_COLOR = 0x00fe00fe
    private val PLACEHOLDER_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888, true)
    private val EMPTY_FLOAT_BUFFER = FloatBuffer.allocate(1)
    private val PLACEHOLDER_SUBJECT = Subject(EMPTY_FLOAT_BUFFER, PLACEHOLDER_BITMAP, 1, 1, 0, 0)
    private val PLACEHOLDER_RESULT = SubjectSegmentationResult(listOf(PLACEHOLDER_SUBJECT), EMPTY_FLOAT_BUFFER, PLACEHOLDER_BITMAP)
    private val PLACEHOLDER_BOUNDING_BOX = BoundingBox(0, 0, 1, 1)
  }

  private var rawImageColors = PLACEHOLDER_INT_ARRAY
  private var rawImageWidth = 1
  private var rawImageHeight = 1
  private var subjectColors = PLACEHOLDER_INT_ARRAY
  var subjectBitmap = PLACEHOLDER_BITMAP
    private set
  var subjectBoundingBox = PLACEHOLDER_BOUNDING_BOX
  private var confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
  var subjectIndex = 0
  private var segmentationResult = PLACEHOLDER_RESULT

  fun cleanup() {
    subjectSegmenter.close()
    subjectColors = PLACEHOLDER_INT_ARRAY
    subjectBitmap = PLACEHOLDER_BITMAP
    rawImageColors = PLACEHOLDER_INT_ARRAY
    subjectBoundingBox = PLACEHOLDER_BOUNDING_BOX
    segmentationResult = PLACEHOLDER_RESULT
    rawImageWidth = 1
    rawImageHeight = 1
  }

  fun process(context: Context, uri: Uri, successCallback: (ProcessSuccess) -> Unit) {
    Log.d("SegmentedImage", "Processing image: $uri")
    try {
      val mlkitInputImage = InputImage.fromFilePath(context, uri)
      process(mlkitInputImage, successCallback)
    } catch(e: Exception){
      successCallback(ProcessSuccess.FAILURE_IMAGE_NOT_FOUND)
    }
  }

  private fun process(mlkitInputImage: InputImage, successCallback: (ProcessSuccess) -> Unit) {
    subjectIndex = -1
    confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
    val bitmap = mlkitInputImage.bitmapInternal
    if(bitmap == null) {
      Log.e("SegmentedImage", "MLKit InputImage did not contain a bitmap")
      successCallback(ProcessSuccess.FAILURE_IMAGE_NOT_RECOGNIZED)
      return
    }

    val bitmapSize = bitmap.width * bitmap.height
    if(rawImageColors.size < bitmapSize) {
      rawImageColors = IntArray(bitmapSize)
    }
    // NOTE: Performance has been demonstrated to be drastically worse when getting individual pixels via bitmap.getPixel(x,y) as opposed to working from an array of values that have been copied
    // An in-depth analysis could be made (predictable memory access, kinder to the cache), but the simplest reason is that calling a function is more expensive than an array access
    // and this performance delta matters when there are 16 million pixels.
    bitmap.getPixels(rawImageColors, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    rawImageWidth = bitmap.width
    rawImageHeight = bitmap.height
    subjectSegmenter.process(mlkitInputImage)
        .addOnSuccessListener { result ->
          segmentationResult = result
          if(subjectCount > 0) {
            subjectIndex = 0
            prepareSubjectBitmap()
          }
          successCallback(ProcessSuccess.SUCCESS)
        }
        .addOnFailureListener { exception ->
          Log.e("SegmentedImage", "ML Kit failed to process image - ${exception.message}")
          successCallback(ProcessSuccess.FAILURE_MLKIT_MODULE_WAITING_TO_DOWNLOAD)
        }
  }

  val subjectCount
    get() = segmentationResult.subjects.size
  val subjectsFound
    get() = subjectCount > 0

  fun decreaseThreshold() {
    if(!subjectsFound || confidenceThreshold == MIN_CONFIDENCE_THRESHOLD) return
    confidenceThreshold -= CONFIDENCE_THRESHOLD_INCREMENT
    confidenceThreshold = max(MIN_CONFIDENCE_THRESHOLD, confidenceThreshold)
    prepareSubjectBitmap()
  }

  fun increaseThreshold() {
    if(!subjectsFound || confidenceThreshold == MAX_CONFIDENCE_THRESHOLD) return
    confidenceThreshold += CONFIDENCE_THRESHOLD_INCREMENT
    confidenceThreshold = min(MAX_CONFIDENCE_THRESHOLD, confidenceThreshold)
    prepareSubjectBitmap()
  }

  fun nextSubject() {
    val subjectCount = segmentationResult.subjects.size
    if(subjectCount <= 1) return
    confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
    subjectIndex = (subjectIndex + 1) % subjectCount
    val subject = segmentationResult.subjects[subjectIndex]
    val subjectColorsSize = subject.width * subject.height
    if(subjectColors.size < subjectColorsSize) {
      subjectColors = IntArray(subjectColorsSize)
    }
    prepareSubjectBitmap()
  }

  private fun prepareSubjectBitmap() {
    /*
        Useful numbers taken from processing a raw camera image file from a Samsung Galaxy A23 5G
        confidence mask size ~= subject.confidenceMask.capacity * sizeof(Float)
                             ~= 4,566,000 * 4 bytes = 18,264,000 bytes = 18.264 MB
        subject color size ~= subject.width * subject.height * sizeof(Int)
                           ~= 2283 * 2000 * 4 bytes = 18,264,000 byte  = 18.264 MB
        raw image size ~= rawImageWidth * rawImageHeight * sizeof(Int)
                       ~= 3060 * 4080 * 4 bytes = 49,939,200 bytes = 49.939 MB
        It's good to note that the raw image is not being entirely cycled through. It is only being accessed
        where subject passes the transparency threshold. Which is less than the size of the subject bounding box.
        regardless a minimum of 36.5 MB is required and far exceeding any available cache on any modern system.
        I don't know the exact cache sizes but they are in the ballpark of 64KiB L1, 256KiB L2, and 2 MiB.
        If this function is called multiple times on the same raw image, confidence mask, or subject,
        the cache would not do us any favors unless the image sizes were reduced 20 fold.
        That said...
        Since the fetching of memory in the inner loop is going to be our bottleneck, there are essentially
        free cycles in their for other things. Like post-processing type effects that does not itself access
        any large amounts of data. On the Samsung Galaxy A23 5G for the tested image above, it took ~32ms.
     */
    val subject = segmentationResult.subjects[subjectIndex]
    val subjectColorsSize = subject.width * subject.height
    if(subjectColors.size < subjectColorsSize) {
      subjectColors = IntArray(subjectColorsSize)
    }
    val subjectConfidenceMask = subject.confidenceMask!!
    val subjectBoundingBox = BoundingBox(Int.MAX_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)
    subjectConfidenceMask.rewind()
    for(y in 0..<subject.height) {
      val rawRowOffset = ((y + subject.startY) * rawImageWidth) + subject.startX
      val subjectRowOffset = y * subject.width
      for(x in 0..<subject.width) {
        if(subjectConfidenceMask.get() < confidenceThreshold) {
          subjectColors[subjectRowOffset + x] = TRANSPARENT_DEBUG_COLOR
        } else {
          if(x < subjectBoundingBox.minX) {
            subjectBoundingBox.minX = x
          } else if(x > subjectBoundingBox.maxX) {
            subjectBoundingBox.maxX = x
          }
          if(y < subjectBoundingBox.minY) {
            subjectBoundingBox.minY = y
          } else if(y > subjectBoundingBox.maxY) {
            subjectBoundingBox.maxY = y
          }
          subjectColors[subjectRowOffset + x] = rawImageColors[rawRowOffset + x]
        }
      }
    }
    this.subjectBoundingBox = subjectBoundingBox

    // NOTE: this try/catch essentially used as an if/else.
    // Unfortunately, determining whether the new bitmap will fit is not simple or well-defined.
    try {
      subjectBitmap.reconfigure(subject.width, subject.height, Bitmap.Config.ARGB_8888)
    } catch(e: IllegalArgumentException) {
      subjectBitmap = Bitmap.createBitmap(subject.width, subject.height, Bitmap.Config.ARGB_8888, true)
    }
    subjectBitmap.setPixels(subjectColors, 0, subject.width, 0, 0, subject.width, subject.height)
  }
}