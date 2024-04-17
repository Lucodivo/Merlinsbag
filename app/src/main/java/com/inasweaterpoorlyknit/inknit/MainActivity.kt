package com.inasweaterpoorlyknit.inknit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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