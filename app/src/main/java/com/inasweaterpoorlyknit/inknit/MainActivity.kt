package com.inasweaterpoorlyknit.inknit

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var preview: ImageView
    lateinit var imageFromCameraButton: Button
    lateinit var imageFromAlbumButton: Button
    lateinit var decreaseThresholdButton: Button
    lateinit var increaseThresholdButton: Button
    lateinit var prevButton: Button
    lateinit var nextButton: Button
    val segmentedImage = SegmentedImage()

    val externalStoragePermissionRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

    val _appSettingsLauncher = registerForActivityResult(StartActivityForResult()){}
    fun openAppSettings() = _appSettingsLauncher.launch(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    )

    val _cameraLauncher = registerForActivityResult(RequestMultiplePermissions()){ permissions ->
        var permissionsGranted = true
        var userCheckedNeverAskAgain = false
        permissions.entries.forEach { entry ->
            if(!entry.value) {
                userCheckedNeverAskAgain = !shouldShowRequestPermissionRationale(entry.key)
                permissionsGranted = false
            }
        }
        if(permissionsGranted) {
            pendingCameraImageUri = createImageFileUri()
            pendingCameraImageUri?.let{ _getCameraImageLauncher.launch(pendingCameraImageUri) }
        } else {
            if(userCheckedNeverAskAgain) {
                val alertMessage = if(externalStoragePermissionRequired) {
                    "Camera and storage permission required to use camera in app. " +
                        "Enable camera permission in settings to use camera feature."
                } else {
                    "Camera permission required to use camera in app. " +
                        "Enable camera permission in settings to use camera feature."
                }
                AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage(alertMessage)
                    .setNegativeButton("No Thanks"){ _, _ -> }
                    .setPositiveButton("App Permissions"){ _, _ -> openAppSettings() }
                    .show()
            } else {
                toast("Camera permissions required")
            }
        }
    }

    fun handleActivityResult(uri: Uri) = processImage(uri)

    val _getContentLauncher = registerForActivityResult(GetContent()){ uri ->
        if(uri != null) handleActivityResult(uri)
        else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
    }
    fun selectAlbumImage() = _getContentLauncher.launch("image/*")

    var pendingCameraImageUri: Uri? = null
    val _getCameraImageLauncher = registerForActivityResult(TakePicture()){ pictureTaken ->
        if(pictureTaken) handleActivityResult(pendingCameraImageUri!!)
        else Log.i("TakePicture ActivityResultContract", "Picture not returned from camera")
    }
    fun selectCameraImage() = _cameraLauncher.launch(REQUIRED_PERMISSIONS)

    fun decreaseThreshold() = lifecycleScope.launch(Dispatchers.Default) {
        segmentedImage.decreaseTheshold()
        drawSubject()
    }

    fun increaseThreshold() = lifecycleScope.launch(Dispatchers.Default) {
        segmentedImage.increaseTheshold()
        drawSubject()
    }

    fun prevSubject() = lifecycleScope.launch(Dispatchers.Default) {
        segmentedImage.prevSubject()
        drawSubject()
    }

    fun nextSubject() = lifecycleScope.launch(Dispatchers.Default) {
        segmentedImage.nextSubject()
        drawSubject()
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
        imageFromCameraButton.setOnClickListener{ selectCameraImage() }
        imageFromAlbumButton.setOnClickListener{ selectAlbumImage() }
        decreaseThresholdButton.setOnClickListener { decreaseThreshold() }
        increaseThresholdButton.setOnClickListener { increaseThreshold() }
        prevButton.setOnClickListener{ prevSubject() }
        nextButton.setOnClickListener{ nextSubject() }
    }

    private fun drawSubject() {
        lifecycleScope.launch(Dispatchers.Main) {
            preview.setImageBitmap(segmentedImage.subjectBitmap)
        }
    }

    private fun processImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val inputImage = InputImage.fromFilePath(this@MainActivity, uri)
                segmentedImage.process(inputImage) { success ->
                    if (success) drawSubject()
                    else Log.e("processImage()", "ML Kit failed to process image")
                }
            } catch (e: IOException) { Log.e("processImage()", "ML Kit failed to open image - ${e.message}") }
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                arrayOf(permission.CAMERA)
            } else {
                arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
            }
    }
}
