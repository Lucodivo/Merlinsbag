package com.inasweaterpoorlyknit.inknit.ui

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.inasweaterpoorlyknit.inknit.ui.theme.InknitTheme

class CameraFragment: Fragment() {

  val screenAspectRatio : Rational
    get() {
      val metrics = Resources.getSystem().displayMetrics
      val screenWidth = metrics.widthPixels
      val screenHeight = metrics.heightPixels
      return Rational(screenWidth, screenHeight)
    }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ComposeView(requireContext()).apply {
      setContent {
        InknitTheme {
          CameraPreview()
/*
          val previewView = PreviewView(context).apply {
            setBackgroundColor(0xFF0000FF.toInt())
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_START
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
          }
          cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
              .build()
              .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
              }

            val imageCaptureBuilder = ImageCapture.Builder()

            val imageCapture = imageCaptureBuilder
              .setResolutionSelector(
                ResolutionSelector.Builder()
                  .setResolutionStrategy(
                    ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY
                  ).build()
              )
              .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
              // Must unbind the use-cases before rebinding them
              cameraProvider.unbindAll()

              // Bind use cases to camera
              cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch(exc: Exception) {
              Log.e("InKnit", "Camera UseCase binding failed", exc)
            }
          }, ContextCompat.getMainExecutor(context))
*/
        }
      }
    }
  }

  @Composable
  fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
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

            cameraProvider.bindToLifecycle(
              lifecycleOwner, cameraSelector, preview
            )
          } catch (exc: Exception) {
            Log.e("InKnit", "Use case binding failed", exc)
          }
        }, ContextCompat.getMainExecutor(context))

        previewView
      })
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