package com.inasweaterpoorlyknit.inknit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.graphics.set
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.Subject
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.io.File
import java.io.IOException
import java.nio.FloatBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

class SegmentedImage {
    private var rawBitmap: Bitmap = PLACEHOLDER_BITMAP
    val originalBitmap get() = rawBitmap
    var subjectBitmap: Bitmap = PLACEHOLDER_BITMAP
        private set
    var confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
    var subjectIndex: Int = 0
    var segmentationResult= PLACEHOLDER_RESULT
    val subjectCount get() = segmentationResult.subjects.size

    companion object {
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
        val mlkitImage = InputImage.fromBitmap(rawBitmap, 0)
        subjectSegmenter.process(mlkitImage)
            .addOnSuccessListener { segmentationResult = it }
            .addOnFailureListener{ Log.e("SegmentedImage", "ML Kit failed to process placeholder bitmap image") }
    }

    fun process(mlkitInputImage: InputImage, successCallback: (Boolean) -> Unit) {
        mlkitInputImage.bitmapInternal?.also { bitmap ->
            rawBitmap = if(bitmap.config == Bitmap.Config.ARGB_8888 && bitmap.isMutable){
                bitmap
            } else {
                bitmap.copy(Bitmap.Config.ARGB_8888, true)
            }
            subjectSegmenter.process(mlkitInputImage).addOnSuccessListener { result: SubjectSegmentationResult ->
                segmentationResult = result
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
        subjectIndex = (subjectIndex - 1 + subjectCount) % subjectCount
        prepareSubjectBitmap()
    }

    fun nextSubject() {
        subjectIndex = (subjectIndex + 1) % subjectCount
        prepareSubjectBitmap()
    }

    private fun prepareSubjectBitmap() {
        val subject = segmentationResult.subjects[subjectIndex]
        val hasAlpha = true
        subjectBitmap = Bitmap.createBitmap(subject.width, subject.height, Bitmap.Config.ARGB_8888, hasAlpha)
        val alphaMaskZero = 0x00ffffff
        val alphaMaskOne = 0xff000000.toInt()
        if(subjectBitmap.isMutable) {
            for (x in 0..<subject.width) {
                for (y in 0..<subject.height) {
                    val subjectPixelIndex = x + y * subject.width
                    subjectBitmap.setPixel(x, y, if (subject.confidenceMask!![subjectPixelIndex] <= confidenceThreshold) {
                        rawBitmap.getPixel(x + subject.startX, subject.startY + y) and alphaMaskZero
                    } else {
                        rawBitmap.getPixel(x + subject.startX, subject.startY + y) or alphaMaskOne
                    })
                }
            }
        } else {
            Log.e("SegmentedImage", "Subject bitmap is not mutable")
        }
    }
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

    private fun drawSubject() = preview.setImageBitmap(segmentedImage.subjectBitmap)

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
/*
        contentResolver.openInputStream(uri)?.also { imageInputStream ->
            segmentedImage.process(imageInputStream){ success ->
                if(success){ drawSubject() }
                else{ toast("ML Kit failed to process image") }
            }
        } ?: run {
            toast("Failed to open image")
        }
*/
    }

    // TODO: It is recommended to use registerActivityForResult()
    @Deprecated("We fucking did it boys")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_IMAGE_CAPTURE -> resultImageUri?.let { uri ->
                    processImage(uri)
                }

                REQUEST_IMAGE_PICKER -> data?.data?.let { uri ->
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