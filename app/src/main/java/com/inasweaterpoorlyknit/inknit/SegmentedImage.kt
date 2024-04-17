package com.inasweaterpoorlyknit.inknit

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.Subject
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

class SegmentedImage {
    private var rawImageColors = PLACEHOLDER_INT_ARRAY
    private var rawImageWidth = 1
    private var rawImageHeight = 1
    var subjectColors = PLACEHOLDER_INT_ARRAY
    var subjectBitmap: Bitmap = PLACEHOLDER_BITMAP
        private set
    var confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
    var subjectIndex: Int = 0
    var segmentationResult= PLACEHOLDER_RESULT

    companion object {
        private val PLACEHOLDER_INT_ARRAY = IntArray(1)
        private const val CONFIDENCE_THRESHOLD_INCREMENT = 0.1f
        private const val MIN_CONFIDENCE_THRESHOLD = CONFIDENCE_THRESHOLD_INCREMENT
        private const val MAX_CONFIDENCE_THRESHOLD = 0.9f // NOTE: 1.0f threshold produces major artifact-ing
        private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f
        private const val TRANSPARENT_DEBUG_COLOR = 0x00fe00fe
        private val PLACEHOLDER_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888, true)
        private val EMPTY_FLOAT_BUFFER = FloatBuffer.allocate(1)
        private val PLACEHOLDER_SUBJECT = Subject(EMPTY_FLOAT_BUFFER, PLACEHOLDER_BITMAP, 1, 1, 0, 0)
        private val PLACEHOLDER_RESULT = SubjectSegmentationResult(listOf(PLACEHOLDER_SUBJECT), EMPTY_FLOAT_BUFFER, PLACEHOLDER_BITMAP)

        private val subjectSegmenter = SubjectSegmentation.getClient(
            SubjectSegmenterOptions.Builder()
                .enableMultipleSubjects(
                    SubjectSegmenterOptions.SubjectResultOptions.Builder()
                        .enableConfidenceMask()
                        .build())
                .build()
        )
    }

    init {
        // warm up the ML Kit Model
        val mlkitImage = InputImage.fromBitmap(PLACEHOLDER_BITMAP, 0)
        subjectSegmenter.process(mlkitImage)
            .addOnSuccessListener { segmentationResult = it }
            .addOnFailureListener{ Log.e("SegmentedImage", "ML Kit failed to process placeholder bitmap image") }
    }

    fun process(mlkitInputImage: InputImage, successCallback: (Boolean) -> Unit) {
        mlkitInputImage.bitmapInternal?.also { bitmap ->
            val bitmapSize = bitmap.width * bitmap.height
            if(rawImageColors.size < bitmapSize){ rawImageColors = IntArray(bitmapSize) }
            bitmap.getPixels(rawImageColors, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            rawImageWidth = bitmap.width
            rawImageHeight = bitmap.height
            subjectIndex = 0
            confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
            subjectSegmenter.process(mlkitInputImage).addOnSuccessListener { result: SubjectSegmentationResult ->
                segmentationResult = result
                val subject = segmentationResult.subjects[subjectIndex]
                val subjectColorsSize = subject.width * subject.height
                if(subjectColors.size < subjectColorsSize){ subjectColors = IntArray(subjectColorsSize) }
                prepareSubjectBitmap()
                successCallback(true)
            }.addOnFailureListener {
                successCallback(false)
            }
        } ?: run {
            successCallback(false)
        }
    }

    fun decreaseTheshold() {
        if(confidenceThreshold == MIN_CONFIDENCE_THRESHOLD) return
        confidenceThreshold -= CONFIDENCE_THRESHOLD_INCREMENT
        confidenceThreshold = max(MIN_CONFIDENCE_THRESHOLD, confidenceThreshold)
        prepareSubjectBitmap()
    }

    fun increaseTheshold() {
        if(confidenceThreshold == MAX_CONFIDENCE_THRESHOLD) return
        confidenceThreshold += CONFIDENCE_THRESHOLD_INCREMENT
        confidenceThreshold = min(MAX_CONFIDENCE_THRESHOLD, confidenceThreshold)
        prepareSubjectBitmap()
    }

    fun prevSubject() {
        val subjectCount = segmentationResult.subjects.size
        if(subjectCount == 1) return
        confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
        subjectIndex = (subjectIndex - 1 + subjectCount) % subjectCount
        val subject = segmentationResult.subjects[subjectIndex]
        val subjectColorsSize = subject.width * subject.height
        if(subjectColors.size < subjectColorsSize){ subjectColors = IntArray(subjectColorsSize) }
        prepareSubjectBitmap()
    }

    fun nextSubject() {
        val subjectCount = segmentationResult.subjects.size
        if(subjectCount == 1) return
        confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
        subjectIndex = (subjectIndex + 1) % subjectCount
        val subject = segmentationResult.subjects[subjectIndex]
        val subjectColorsSize = subject.width * subject.height
        if(subjectColors.size < subjectColorsSize){ subjectColors = IntArray(subjectColorsSize) }
        prepareSubjectBitmap()
    }

    private fun prepareSubjectBitmap() {
        // Useful numbers taken from processing a raw camera image file from a Samsung Galaxy A23 5G
        // confidence mask size ~= subject.confidenceMask.capacity * sizeof(Float)
        //                      ~= 4,566,000 * 4 bytes = 18,264,000 bytes = 18.264 MB
        // subject color size ~= subject.width * subject.height * sizeof(Int)
        //                    ~= 2283 * 2000 * 4 bytes = 18,264,000 byte  = 18.264 MB
        // raw image size ~= rawImageWidth * rawImageHeight * sizeof(Int)
        //                ~= 3060 * 4080 * 4 bytes = 49,939,200 bytes = 49.939 MB
        // It's good to note that the raw image is not being entirely cycled through. It is only being accessed
        // where subject passes the transparency threshold. Which is less than the size of the subject bounding box.
        // regardless a minimum of 36.5 MB is required and far exceeding any available cache on any modern system.
        // I don't know the exact cache sizes but they are in the ballpark of 64KiB L1, 256KiB L2, and 2 MiB.
        // If this function is called multiple times on the same raw image, confidence mask, or subject,
        // the cache would not do us any favors unless the images were reduced 20 fold.
        // That said...
        // Since the fetching of memory in the inner loop is going to be our bottleneck, there are essentially
        // free cycles in their for other things. Like post-processing type effects that does not itself access
        // any large amounts of data.

        val subject = segmentationResult.subjects[subjectIndex]
        val subjectConfidenceMask = subject.confidenceMask!!
        subjectConfidenceMask.rewind()
        for(y in 0 ..< subject.height) {
            val rawRowOffset = ((y + subject.startY) * rawImageWidth) + subject.startX
            val subjectRowOffset = y * subject.width
            for(x in 0 ..< subject.width) {
                subjectColors[subjectRowOffset + x] =
                    if(subjectConfidenceMask.get() < confidenceThreshold) TRANSPARENT_DEBUG_COLOR
                    else rawImageColors[rawRowOffset + x]
            }
        }

        // NOTE: this try/catch essentially used as an if/else.
        // Unfortunately, determining whether the new bitmap will fit is not simple or well-defined.
        try{
            subjectBitmap.reconfigure(subject.width, subject.height, Bitmap.Config.ARGB_8888)
        } catch (e: IllegalArgumentException) {
            subjectBitmap = Bitmap.createBitmap(subject.width, subject.height, Bitmap.Config.ARGB_8888, true)
        }
        subjectBitmap.setPixels(subjectColors, 0, subject.width, 0, 0, subject.width, subject.height)
    }
}
