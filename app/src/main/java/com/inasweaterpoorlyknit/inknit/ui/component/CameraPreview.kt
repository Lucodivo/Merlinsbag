package com.inasweaterpoorlyknit.inknit.ui.component

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.inasweaterpoorlyknit.inknit.ui.isComposePreview
import com.inasweaterpoorlyknit.inknit.ui.previewAssetBitmap

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
  val isComposePreview = isComposePreview
  val lifecycleOwner = LocalLifecycleOwner.current
  AndroidView(
    modifier = modifier,
    factory = { context ->
      if(isComposePreview) {
        val previewCameraBitmap = previewAssetBitmap("camera_compose_preview.webp", context)
        return@AndroidView ImageView(context).apply {
          setImageBitmap(previewCameraBitmap)
          layoutParams = androidViewLayoutParams
        }
      }

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
fun PreviewCameraPreview(){
  CameraPreview()
}