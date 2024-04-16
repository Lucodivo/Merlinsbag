package com.inasweaterpoorlyknit.inknit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.Subject
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.io.File
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

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
        private val PLACEHOLDER_INT_BUFFER = IntBuffer.allocate(1)
        private val PLACEHOLDER_INT_ARRAY = IntArray(1)
        private const val THRESHHOLD_INCREMENTS = 0.1f
        private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f
        private val PLACEHOLDER_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        private val EMPTY_FLOAT_BUFFER = FloatBuffer.allocate(1)
        private val PLACEHOLDER_SUBJECT = Subject(EMPTY_FLOAT_BUFFER, PLACEHOLDER_BITMAP, 1, 1, 0, 0, )
        private val PLACEHOLDER_RESULT = SubjectSegmentationResult(listOf(PLACEHOLDER_SUBJECT), EMPTY_FLOAT_BUFFER, PLACEHOLDER_BITMAP)

        // TODO: Not currently taking advantage of the range of confidence given from the confidence
        //  mask. Bitmaps would have to be manually constructed to allow user to fluctuate the threshold
        //  confidence for each subject. It may also seems like it could be worth smoothing out the literal
        //  edges of each subject
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
        confidenceThreshold -= THRESHHOLD_INCREMENTS
        confidenceThreshold = max(THRESHHOLD_INCREMENTS, confidenceThreshold)
        prepareSubjectBitmap()
    }

    fun increaseTheshold() {
        confidenceThreshold += THRESHHOLD_INCREMENTS
        confidenceThreshold = min(0.9f, confidenceThreshold)
        prepareSubjectBitmap()
    }

    fun prevSubject() {
        val subjectCount = segmentationResult.subjects.size
        subjectIndex = (subjectIndex - 1 + subjectCount) % subjectCount
        val subject = segmentationResult.subjects[subjectIndex]
        val subjectColorsSize = subject.width * subject.height
        if(subjectColors.size < subjectColorsSize){ subjectColors = IntArray(subjectColorsSize) }
        prepareSubjectBitmap()
    }

    fun nextSubject() {
        val subjectCount = segmentationResult.subjects.size
        subjectIndex = (subjectIndex + 1) % subjectCount
        val subject = segmentationResult.subjects[subjectIndex]
        val subjectColorsSize = subject.width * subject.height
        if(subjectColors.size < subjectColorsSize){ subjectColors = IntArray(subjectColorsSize) }
        prepareSubjectBitmap()
    }

    private fun prepareSubjectBitmap() {
        val alphaMaskZero = 0x00ffffff
        val alphaMaskOne = 0xff000000.toInt()
        val subject = segmentationResult.subjects[subjectIndex]
        val subjectConfidenceMask = subject.confidenceMask!!
        subjectConfidenceMask.rewind()
        for(y in 0 ..< subject.height) {
            val rawRowOffset = ((y + subject.startY) * rawImageWidth) + subject.startX
            val subjectRowOffset = y * subject.width
            for(x in 0 ..< subject.width) {
                subjectColors[subjectRowOffset + x] = if(subjectConfidenceMask.get() <= confidenceThreshold) {
                    rawImageColors[rawRowOffset + x] and alphaMaskZero
                } else {
                    rawImageColors[rawRowOffset + x] or alphaMaskOne
                }
            }
        }
        // NOTE: try/catch essentially used as an if/else. Unfortunately, determining whether the new bitmap will fit is not simple or well defined.
        try{
            subjectBitmap.reconfigure(subject.width, subject.height, Bitmap.Config.ARGB_8888)
        } catch (e: IllegalArgumentException) {
            subjectBitmap = Bitmap.createBitmap(subject.width, subject.height, Bitmap.Config.ARGB_8888, true)
        }
        subjectBitmap.setPixels(subjectColors, 0, subject.width, 0, 0, subject.width, subject.height)
    }
}

const val NANOSECONDS_PER_MICROSECOND = 1_000
const val NANOSECONDS_PER_MILLISECOND = 1_000_000
const val NANOSECONDS_PER_SECOND = 1_000_000_000
fun nanosecondsToString(nanoseconds: Long): String  {
    return when(nanoseconds) {
        in 0..<1_000 -> { "${nanoseconds}ns" }
        in 0..<1_000_000 -> {
            val microseconds = nanoseconds.toDouble() / NANOSECONDS_PER_MICROSECOND.toDouble()
            "${"%.2f".format(microseconds)}µs"
        }
        in 0..<1_000_000_000 -> {
            val milliseconds = nanoseconds.toDouble() / NANOSECONDS_PER_MILLISECOND.toDouble()
            "${"%.2f".format(milliseconds)}ms"
        }
        else -> {
            val seconds = nanoseconds.toDouble() / NANOSECONDS_PER_SECOND.toDouble()
            "${"%.2f".format(seconds)}s"
        }
    }
}

class Timer {
    var startNs = System.nanoTime()
    fun elapsedNs(): Long = System.nanoTime() - startNs
    fun logElapsed(tag: String) {
        val elapsedNs = elapsedNs()
        Log.i("InKnit", "$tag: ${nanosecondsToString(elapsedNs)}")
    }
    fun reset(){ startNs = System.nanoTime() }
}

class MainActivity : AppCompatActivity() {
    lateinit var preview: ImageView
    lateinit var imageFromCameraButton: Button
    lateinit var imageFromAlbumButton: Button
    lateinit var decreaseThresholdButton: Button
    lateinit var increaseThresholdButton: Button
    lateinit var prevButton: Button
    lateinit var nextButton: Button
    var resultImageUri: Uri? = null
    val segmentedImage = SegmentedImage()
    val timer = Timer()

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICKER = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_still_image)
        preview = findViewById(R.id.preview)
        imageFromCameraButton = findViewById(R.id.image_from_camera)
        imageFromAlbumButton = findViewById(R.id.image_from_album)
        decreaseThresholdButton = findViewById(R.id.decrease_threshold)
        increaseThresholdButton = findViewById(R.id.increase_threshold)
        prevButton = findViewById(R.id.prev)
        nextButton = findViewById(R.id.next)
        imageFromCameraButton.setOnClickListener{ _ ->
            createImageFileUri(this).let { imageUri ->
                resultImageUri = imageUri
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
        imageFromAlbumButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICKER)
        }
        decreaseThresholdButton.setOnClickListener {
            segmentedImage.decreaseTheshold()
            drawSubject()
        }
        increaseThresholdButton.setOnClickListener {
            segmentedImage.increaseTheshold()
            drawSubject()
        }
        prevButton.setOnClickListener{
            segmentedImage.prevSubject()
            drawSubject()
        }
        nextButton.setOnClickListener{
            segmentedImage.nextSubject()
            drawSubject()
        }
        imageFromAlbumButton.performClick()
    }

    private fun drawSubject(){
        timer.logElapsed("ML Kit Subject Segmentation Post-Processing")
        preview.setImageBitmap(segmentedImage.subjectBitmap)
    }

    fun processImage(uri: Uri) {
        try {
            val inputImage = InputImage.fromFilePath(this, uri)
            inputImage.byteBuffer
            segmentedImage.process(inputImage) { success ->
                if(success){ drawSubject() }
                else{ toast("ML Kit failed to process image") }
            }
        } catch (e: IOException) {
            toast("ML Kit failed to open image")
        }
    }

    // TODO: It is recommended to use registerActivityForResult()
    @Deprecated("We fucking did it boys")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_IMAGE_CAPTURE -> resultImageUri?.let { uri ->
                    timer.reset()
                    // TODO: move to separate thread
                    processImage(uri)
                }

                REQUEST_IMAGE_PICKER -> data?.data?.let { uri ->
                    timer.reset()
                    // TODO: move to separate thread
                    processImage(uri)
                }
                else -> toast("Failed to get image from request.")
            }
        } else { toast("Image capture result not returned") }
    }
}

fun createImageFileUri(context: Context): Uri? {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return try {
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        FileProvider.getUriForFile(context, "com.inasweaterpoorlyknit.inknit.fileprovider", file)
    } catch (ex: IOException) {
        context.toast("Error creating file")
        null
    }
}