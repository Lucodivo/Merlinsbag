package com.inasweaterpoorlyknit.inknit.ui

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionFilter
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.inasweaterpoorlyknit.inknit.common.toast
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

class CameraFragment: Fragment() {

  companion object {
    val TAG = CameraFragment::class.simpleName
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
  }

  var imageCapture: ImageCapture? = ImageCapture
    .Builder()
    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
  .setJpegQuality(100)
    .build()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ComposeView(requireContext()).apply {
      setContent {
        InKnitTheme {
          CameraScreen(
            imageCapture = imageCapture,
            onClick = ::takePhoto
          )
        }
      }
    }
  }

  private fun takePhoto() {
    // Get a stable reference of the modifiable image capture use case
    val imageCapture = imageCapture ?: return

    val context = requireContext()

    // Create time stamped name and MediaStore entry.
    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
      .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, name)
      put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
      if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/InKnit")
      }
    }

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions
      .Builder(context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues)
      .build()

    // Set up image capture listener, which is triggered after photo has
    // been taken
    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onError(exc: ImageCaptureException){ Log.e(TAG, "Photo capture failed: ${exc.message}", exc) }
        override fun onImageSaved(output: ImageCapture.OutputFileResults){
          Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
          val uriString = output.savedUri.toString()
          val action = CameraFragmentDirections.actionCameraFragmentToAddArticleFragment(uriString)
          findNavController().navigate(action)
        }
      }
    )
  }

  // onResume / onPause are used to expand the activity's vertical range and set the status bar transparent
  override fun onResume() {
    hideSystemUI()
    super.onResume()
  }

  override fun onPause() {
    showSystemUI()
    super.onPause()
  }
}

@Composable
fun CameraPreview(
  modifier: Modifier = Modifier,
  cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
  scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FIT_CENTER,
  imageCapture: ImageCapture? = null
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  AndroidView(
    modifier = modifier,
    factory = { context ->
      val previewView = PreviewView(context).apply {
        this.scaleType = scaleType
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
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
  Box(Modifier.clickable { onClick() }) {
    CameraPreview(imageCapture = imageCapture)
  }
}