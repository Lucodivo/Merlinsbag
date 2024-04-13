package com.inasweaterpoorlyknit.inknit

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val REQUEST_IMAGE_CAPTURE = 1;
val REQUEST_IMAGE_PICKER = 2;

class MainActivity : AppCompatActivity() {
    lateinit var preview: ImageView
    lateinit var graphicOverlay: GraphicOverlay
    lateinit var imageFromCameraButton: Button
    lateinit var imageFromAlbumButton: Button

    var resultImageUri: Uri? = null
    val subjectSegmenter = SubjectSegmentation.getClient(
        SubjectSegmenterOptions.Builder()
            .enableMultipleSubjects(
                SubjectSegmenterOptions.SubjectResultOptions.Builder()
                    .enableConfidenceMask()
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
        processDebugImage()
    }

    fun processDebugImage() {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(packageName)
            .appendPath("${R.raw.test_image_army_jacket}")
            .build()
        processImage(uri)
    }

    fun processImage(imageUri: Uri) {
        graphicOverlay.clear()
        preview.setImageURI(imageUri)
        val mlkitImage = InputImage.fromFilePath(this, imageUri)
        subjectSegmenter.process(mlkitImage).addOnSuccessListener { result: SubjectSegmentationResult ->
            graphicOverlay.setImageSourceInfo(mlkitImage.width, mlkitImage.height, false)
            graphicOverlay.add(SubjectSegmentationGraphic(result, mlkitImage.width, mlkitImage.height))
        }.addOnFailureListener {
            Toast.makeText(this, "Looking rough boys", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_IMAGE_CAPTURE -> resultImageUri?.let{
                    processImage(it)
                }
                REQUEST_IMAGE_PICKER -> data?.data?.let{
                    processImage(it)
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