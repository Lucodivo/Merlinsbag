package com.inasweaterpoorlyknit.inknit

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

// TODO: remove requireContext/Activity?
class AddArticleFragment: Fragment(R.layout.fragment_add_article) {
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
      data = Uri.fromParts("package", requireActivity().packageName, null)
    }
  )

  fun handleActivityResult(uri: Uri) = processImage(uri)

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
      pendingCameraImageUri = requireContext().createImageFileUri()
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
        AlertDialog.Builder(requireContext())
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
  var pendingCameraImageUri: Uri? = null
  val _getCameraImageLauncher = registerForActivityResult(TakePicture()){ pictureTaken ->
    if(pictureTaken) handleActivityResult(pendingCameraImageUri!!)
    else Log.i("TakePicture ActivityResultContract", "Picture not returned from camera")
  }
  fun selectCameraImage() = _cameraLauncher.launch(REQUIRED_PERMISSIONS)

  val _getContentLauncher = registerForActivityResult(GetContent()){ uri ->
    if(uri != null) handleActivityResult(uri)
    else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
  }
  fun selectAlbumImage() = _getContentLauncher.launch("image/*")


  fun decreaseThreshold() = lifecycleScope.launch(Dispatchers.Default) {
    segmentedImage.decreaseThreshold()
    drawSubject()
  }

  fun increaseThreshold() = lifecycleScope.launch(Dispatchers.Default) {
    segmentedImage.increaseThreshold()
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

  private fun drawSubject() {
    lifecycleScope.launch(Dispatchers.Main) {
      preview.setImageBitmap(segmentedImage.subjectBitmap)
    }
  }

  private fun processImage(uri: Uri) {
    lifecycleScope.launch(Dispatchers.Default) {
      try {
        segmentedImage.process(requireContext(), uri) { success ->
          if (success) drawSubject()
          else Log.e("processImage()", "ML Kit failed to process image")
        }
      } catch (e: IOException) { Log.e("processImage()", "ML Kit failed to open image - ${e.message}") }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    preview = view.findViewById(R.id.preview)
    imageFromCameraButton = view.findViewById(R.id.image_from_camera)
    imageFromAlbumButton = view.findViewById(R.id.image_from_album)
    decreaseThresholdButton = view.findViewById(R.id.decrease_threshold)
    increaseThresholdButton = view.findViewById(R.id.increase_threshold)
    prevButton = view.findViewById(R.id.prev)
    nextButton = view.findViewById(R.id.next)
    imageFromCameraButton.setOnClickListener{ selectCameraImage() }
    imageFromAlbumButton.setOnClickListener{ selectAlbumImage() }
    decreaseThresholdButton.setOnClickListener { decreaseThreshold() }
    increaseThresholdButton.setOnClickListener { increaseThreshold() }
    prevButton.setOnClickListener{ prevSubject() }
    nextButton.setOnClickListener{ nextSubject() }
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