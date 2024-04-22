package com.inasweaterpoorlyknit.inknit.ui

import android.Manifest.permission
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.PLACEHOLDER_BITMAP
import com.inasweaterpoorlyknit.inknit.common.createImageFileUri
import com.inasweaterpoorlyknit.inknit.common.toast
import com.inasweaterpoorlyknit.inknit.ui.theme.InknitTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: remove requireContext/Activity?
class AddArticleFragment2: Fragment() {
  val viewModel: AddArticleViewModel by viewModels()

  var pendingCameraImageUri: Uri? = null

  fun selectCameraImage() = _cameraWithPermissionsCheckLauncher.launch(REQUIRED_PERMISSIONS)
  fun selectAlbumImage() = _photoAlbumLauncher.launch("image/*")
  fun handleImageUriResult(uri: Uri) = viewModel.processImage(uri)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setContent {
        AddArticleScreen(
          processedImage = viewModel.processedBitmap.value,
          onAlbumClick = ::selectAlbumImage,
          onCameraClick = ::selectCameraImage,
          onFocusClick = { viewModel.onFocusClicked() },
          onWidenClick = { viewModel.onWidenClicked() },
          onPrevClick = { viewModel.onPrevClicked() },
          onNextClick = { viewModel.onNextClicked() }
        )
      }
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

  //region REGISTER FOR ACTIVITY RESULTS
  // Go to settings
  val _appSettingsLauncher = registerForActivityResult(StartActivityForResult()){}

  // Get new camera photo from user and check necessary permissions
  val _cameraLauncher = registerForActivityResult(TakePicture()){ pictureTaken ->
    if(pictureTaken) handleImageUriResult(pendingCameraImageUri!!)
    else Log.i("TakePicture ActivityResultContract", "Picture not returned from camera")
  }
  val _cameraWithPermissionsCheckLauncher = registerForActivityResult(RequestMultiplePermissions()){ permissions ->
    fun openAppSettings() = _appSettingsLauncher.launch(
      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", requireActivity().packageName, null)
      }
    )

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
      pendingCameraImageUri?.let{ _cameraLauncher.launch(pendingCameraImageUri) }
    } else {
      if(userCheckedNeverAskAgain) {
        val externalStoragePermissionRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
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

  // Get image already saved on phone
  val _photoAlbumLauncher = registerForActivityResult(GetContent()){ uri ->
    if(uri != null) handleImageUriResult(uri)
    else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
  }
  //endregion REGISTER FOR ACTIVITY RESULTS
}

@Preview
@Composable
fun AddArticleScreen(
  onAlbumClick: () -> Unit = {},
  onCameraClick: () -> Unit = {},
  onPrevClick: () -> Unit = {},
  onNextClick: () -> Unit = {},
  onFocusClick: () -> Unit = {},
  onWidenClick: () -> Unit = {},
  processedImage: Bitmap = PLACEHOLDER_BITMAP
) {
  InknitTheme {
    Column {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(10f).fillMaxSize()){
        Image(bitmap = processedImage.asImageBitmap(),
          contentDescription = stringResource(id = R.string.processed_image),
        )
      }
      listOf(
        listOf(
          ImageWithTextData(R.drawable.panel_reyda_donmez, R.string.four_x_four_panel, onClick =  onAlbumClick),
          ImageWithTextData(R.drawable.prev_reyda_donmez, R.string.left_arrow, onClick =  onPrevClick),
          ImageWithTextData(R.drawable.next_reyda_donmez, R.string.right_arrow, onClick =  onNextClick),
        ),
        listOf(
          ImageWithTextData(R.drawable.camera_reyda_donmez, R.string.camera, onClick = onCameraClick),
          ImageWithTextData(R.drawable.target_2_reyda_donmez, R.string.archery_target, onClick =  onFocusClick),
          ImageWithTextData(R.drawable.expand_reyda_donmez, R.string.outward_pointing_arrows, onClick =  onWidenClick),
        ),
      ).also { ImageWithTextColumnsOfRows(
        buttonsTopToBottom = it,
        modifier = Modifier.weight(2.0f)
      )}
    }
  }
}