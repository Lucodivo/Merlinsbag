package com.inasweaterpoorlyknit.inknit.ui.screen

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.navigation.ScreenSuccess
import com.inasweaterpoorlyknit.inknit.ui.timestampFileName
import com.inasweaterpoorlyknit.inknit.viewmodels.CameraViewModel
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

const val TAG = "CameraScreen"
const val EXTERNAL_STORAGE_CAMERA_PIC_DIR = "Pictures/InKnit"

const val CAMERA_ROUTE= "camera_route"

fun NavController.navigateToCamera(navOptions: NavOptions? = null) = navigate(CAMERA_ROUTE, navOptions)

@Composable
fun CameraRoute(
  navController: NavController,
  modifier: Modifier = Modifier,
  imageSuccessfullyUsed: ScreenSuccess? = null,
  cameraViewModel: CameraViewModel = hiltViewModel(),
){
  // TODO: This should not be that hard
  //   Pain points were that recomposition was happening multiple times
  //   causing the backstack to be popped multiple times
  //   while also maintaining a desire to not draw anything on subsequent recompositions
  //   especially in terms of hiding system UI
  var finished by remember { mutableStateOf(false) }
  if(cameraViewModel.checkImageUsedSuccessfully(imageSuccessfullyUsed)){
    finished = true
    navController.popBackStack()
  }
  if (finished){ return }

  cameraViewModel.addArticle.value.getContentIfNotHandled()?.let { uriString ->
    navController.navigateToAddArticle(uriString)
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
  CameraScreen(
    imageCapture = imageCapture,
    onClick = ::takePhoto,
  )
}

@Composable
fun CameraPreview(
  modifier: Modifier = Modifier,
  cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
  scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FIT_CENTER,
  imageCapture: ImageCapture? = null,
) {
  val androidViewLayoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
  )
  if(LocalInspectionMode.current) { // !! Compose Preview only code !!
    val previewCameraBitmap = previewAssetBitmap("camera_compose_preview.webp")
    AndroidView(modifier = modifier, factory = { context ->
        ImageView(context).apply {
          setImageBitmap(previewCameraBitmap)
          layoutParams = androidViewLayoutParams
    }})
    return
  }
  val lifecycleOwner = LocalLifecycleOwner.current
  AndroidView(
    modifier = modifier,
    factory = { context ->
      val previewView = PreviewView(context).apply {
        this.scaleType = scaleType
        layoutParams = androidViewLayoutParams
        // Preview is incorrectly scaled in Compose on some devices without this
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
      }

      val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

      cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        // Preview
        val preview = Preview.Builder()
          .build()
          .also {
            it.setSurfaceProvider(previewView.surfaceProvider)
          }

        try {
          // Must unbind the use-cases before rebinding them.
          cameraProvider.unbindAll()
          cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
        } catch (exc: Exception) {
          Log.e("InKnit", "Use case binding failed", exc)
        }
      }, ContextCompat.getMainExecutor(context))

      previewView
    })
}

@ComposePreview
@Composable
fun CameraScreen(
  imageCapture: ImageCapture? = null,
  onClick: () -> Unit = {}
) {
  CameraPreview(imageCapture = imageCapture)
  TakePictureButton(onClick = onClick)
}

@Composable
fun TakePictureButton(
  onClick: () -> Unit = {},
){
  var captureActivated by remember{ mutableStateOf(false) }
  Box(contentAlignment = Alignment.BottomCenter,
    modifier = Modifier.fillMaxSize()){
    Button(
      colors = ButtonColors(containerColor = Color.White, contentColor = Color.White, disabledContainerColor = Color.Gray, disabledContentColor = Color.Gray),
      shape = CircleShape,
      enabled = !captureActivated,
      onClick = {
        captureActivated = true
        onClick()
      },
      modifier = Modifier.padding(20.dp)
    ){}
  }
}