package com.inasweaterpoorlyknit.inknit.ui.screen

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.ui.LandscapePreview
import com.inasweaterpoorlyknit.inknit.ui.component.CameraPreview
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.ui.timestampFileName
import com.inasweaterpoorlyknit.inknit.viewmodel.CameraViewModel

const val TAG = "CameraScreen"
const val EXTERNAL_STORAGE_CAMERA_PIC_DIR = "Pictures/InKnit"

const val CAMERA_ROUTE= "camera_route"

fun NavController.navigateToCamera(navOptions: NavOptions? = null) = navigate(CAMERA_ROUTE, navOptions)

@Composable
fun CameraRoute(
  navController: NavController,
  modifier: Modifier = Modifier,
  cameraViewModel: CameraViewModel = hiltViewModel(),
){
  cameraViewModel.addArticle.value.getContentIfNotHandled()?.let { uriString ->
    navController.navigateToAddArticle(listOf(uriString))
  }

  val imageCapture = remember {
    ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
      .setFlashMode(ImageCapture.FLASH_MODE_OFF)
      .setJpegQuality(100)
      .build()
  }
  fun takePhoto() {
    // Get a stable reference of the modifiable image capture use case
    val context = navController.context

    // Create time stamped name and MediaStore entry.
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, timestampFileName())
      put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
      if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        put(MediaStore.Images.Media.RELATIVE_PATH, EXTERNAL_STORAGE_CAMERA_PIC_DIR)
      }
    }

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions.Builder(
      context.contentResolver,
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      contentValues
    ).build()

    // Set up image capture listener, which is triggered after photo has
    // been taken
    imageCapture.takePicture(outputOptions,
      ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onError(exc: ImageCaptureException) {
          Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
        }
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
          Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
          val uriString = output.savedUri.toString()
          cameraViewModel.newImageUri(uriString)
        }
      })
  }
  val landscape: Boolean = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
  CameraScreen(
    imageCapture = imageCapture,
    onClick = ::takePhoto,
    landscape = landscape,
  )
}

@Composable
fun CameraScreen(
  imageCapture: ImageCapture,
  onClick: () -> Unit,
  landscape: Boolean,
) {
  CameraPreview(imageCapture = imageCapture)
  CameraControls(landscape = landscape, onClick = onClick)
}

@Composable
fun CameraControls(
  landscape: Boolean,
  onClick: () -> Unit,
) {
  val alignment = if(landscape) Alignment.CenterEnd else Alignment.BottomCenter
  var captureActivated by remember{ mutableStateOf(false) }
  val padding = WindowInsets.safeDrawing.asPaddingValues().run {
    16.dp + if(landscape) calculateEndPadding(LocalLayoutDirection.current) else calculateBottomPadding()
  }
  Box(contentAlignment = alignment,
    modifier = Modifier
      .fillMaxSize()
      .padding(padding)
  ){
    Button(
      colors = ButtonColors(containerColor = Color.White, contentColor = Color.White, disabledContainerColor = Color.Gray, disabledContentColor = Color.Gray),
      shape = CircleShape,
      enabled = !captureActivated,
      onClick = {
        captureActivated = true
        onClick()
      },
      modifier = Modifier.size(50.dp)
    ){}
  }
}

@Preview
@Composable
fun PreviewCameraScreen(){
  val bogusPreviewImageCapture = ImageCapture.Builder().build()
  NoopTheme {
    CameraScreen(
      imageCapture = bogusPreviewImageCapture,
      onClick = {},
      landscape = false
    )
  }
}

@LandscapePreview
@Composable
fun PreviewCameraScreen_landscape(){
  val bogusPreviewImageCapture = ImageCapture.Builder().build()
  NoopTheme {
    CameraScreen(
      imageCapture = bogusPreviewImageCapture,
      onClick = {},
      landscape = true
    )
  }
}
