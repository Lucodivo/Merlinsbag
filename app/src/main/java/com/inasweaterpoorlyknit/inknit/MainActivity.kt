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
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

const val REQUEST_IMAGE_CAPTURE = 1;
const val REQUEST_IMAGE_PICKER = 2;

val PLACEHOLDER_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

private const val THRESHHOLD_INCREMENTS = 0.1f
private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f

class MainActivity : AppCompatActivity() {
    lateinit var preview: ImageView
    lateinit var graphicOverlay: GraphicOverlay
    lateinit var imageFromCameraButton: Button
    lateinit var imageFromAlbumButton: Button
    lateinit var decreaseThresholdButton: Button
    lateinit var increaseThresholdButton: Button
    lateinit var prevButton: Button
    lateinit var nextButton: Button
    var subjectIndex: Int = 0
    var subjectSegmentationResult: SubjectSegmentationResult? = null

    var resultImageUri: Uri? = null
    var rawBitmap: Bitmap = PLACEHOLDER_BITMAP
    var subjectBitmap: Bitmap = PLACEHOLDER_BITMAP

    var confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD

    // TODO: Not currently taking advantage of the range of confidence given from the confidence
    //  mask. Bitmaps would have to be manually constructed to allow user to fluctuate the threshold
    //  confidence for each subject. It may also seems like it could be worth smoothing out the literal
    //  edges of each subject
    val subjectSegmenter = SubjectSegmentation.getClient(
        SubjectSegmenterOptions.Builder()
            .enableMultipleSubjects(
                SubjectSegmenterOptions.SubjectResultOptions.Builder()
                    .enableConfidenceMask()
                    .enableSubjectBitmap()
                    .build())
            .build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_still_image)
        preview = findViewById(R.id.preview)
        graphicOverlay = findViewById(R.id.graphic_overlay)
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
            confidenceThreshold -= THRESHHOLD_INCREMENTS
            confidenceThreshold = max(THRESHHOLD_INCREMENTS, confidenceThreshold)
            drawSubject()
        }
        increaseThresholdButton.setOnClickListener {
            confidenceThreshold += THRESHHOLD_INCREMENTS
            confidenceThreshold = min(0.9f, confidenceThreshold)
            drawSubject()
        }
        prevButton.setOnClickListener{
            subjectSegmentationResult?.let {
                subjectIndex = (subjectIndex - 1 + it.subjects.size) % it.subjects.size
                drawSubject()
            }
        }
        nextButton.setOnClickListener{
            subjectSegmentationResult?.let {
                subjectIndex = (subjectIndex + 1) % it.subjects.size
                drawSubject()
            }
        }
        imageFromAlbumButton.performClick()
    }

    private fun drawSubject() {
        subjectSegmentationResult?.let { segmentationResult ->
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
            preview.setImageBitmap(subjectBitmap)
        }
    }

    fun processCurrentImage() {
        subjectSegmentationResult = null
        subjectIndex = 0
        preview.setImageBitmap(rawBitmap)
        val mlkitImage = InputImage.fromBitmap(rawBitmap, 0)
        subjectSegmenter.process(mlkitImage).addOnSuccessListener { result: SubjectSegmentationResult ->
            subjectSegmentationResult = result
            drawSubject()
        }.addOnFailureListener {
            Toast.makeText(this, "Looking rough boys", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_IMAGE_CAPTURE -> resultImageUri?.let{ uri ->
                    rawBitmap = ImageDecoder.createSource(contentResolver, uri).decodeBitmap{_, _ ->}
                    processCurrentImage()
                }
                REQUEST_IMAGE_PICKER -> data?.data?.let{ uri ->
                    rawBitmap = ImageDecoder.createSource(contentResolver, uri).decodeBitmap{_, _ ->}
                    processCurrentImage()
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