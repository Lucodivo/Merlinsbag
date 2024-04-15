package com.inasweaterpoorlyknit.inknit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import androidx.core.graphics.decodeBitmap
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
                        .enableSubjectBitmap()
                        .build())
                .build()
        )
    }

    fun process(bitmap: Bitmap, successCallback: (Boolean) -> Unit){
        rawBitmap = bitmap
        val mlkitImage = InputImage.fromBitmap(rawBitmap, 0)
        subjectSegmenter.process(mlkitImage).addOnSuccessListener { result: SubjectSegmentationResult ->
            segmentationResult = result
            prepareSubjectBitmap()
            successCallback(true)
        }.addOnFailureListener {
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
        subjectBitmap = Bitmap.createBitmap(
            rawBitmap,
            subject.startX,
            subject.startY,
            subject.width,
            subject.height
        ).copy(Bitmap.Config.ARGB_8888, true) // copied for mutability
        for(x in 0..<subject.width) {
            for(y in 0..<subject.height) {
                val pixelIndex = x + y*subject.width
                if(subject.confidenceMask!![pixelIndex] <= confidenceThreshold){
                    subjectBitmap[x, y] = 0
                }
            }
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

    fun processCurrentImage(uri: Uri) {
        val bitmap = ImageDecoder.createSource(contentResolver, uri).decodeBitmap{_, _ ->}
        preview.setImageBitmap(bitmap)
        segmentedImage.process(bitmap){ success ->
            if(success){ drawSubject() }
            else{ Toast.makeText(this, "Looking rough boys", Toast.LENGTH_SHORT).show() }
        }
    }

    // TODO: It is recommended to use registerActivityForResult()
    @Deprecated("We fucking did it boys")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_IMAGE_CAPTURE -> resultImageUri?.let{ uri ->
                    processCurrentImage(uri)
                }
                REQUEST_IMAGE_PICKER -> data?.data?.let{ uri ->
                    processCurrentImage(uri)
                }
                else -> Log.i("We fucking did it boys", "???") }
        } else {
            Log.e("We fucking did it boys", "Image capture result not returned")
        }
    }
}

fun createImageFileUri(context: Context): Uri? {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return try {
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        FileProvider.getUriForFile(context, "com.inasweaterpoorlyknit.inknit.fileprovider", file)
    } catch (ex: IOException) {
        Toast.makeText(context, "Error creating file", Toast.LENGTH_SHORT).show()
        null
    }
}