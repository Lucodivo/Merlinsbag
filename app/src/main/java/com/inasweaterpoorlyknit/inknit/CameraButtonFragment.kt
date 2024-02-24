package com.inasweaterpoorlyknit.inknit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.GnssAntennaInfo.Listener
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.inasweaterpoorlyknit.inknit.ui.theme.InknitTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass.
 * Use the [CameraButtonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraButtonFragment : Fragment() {

    private lateinit var cameraExecutor: ExecutorService

    private val imageCapture = ImageCapture.Builder()
        .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(allPermissionsGranted()) {
            startCamera()
        } else {
            requestPersmissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                InknitTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Screen(onTakePictureClick = ::takePhoto, onTakeVideoClick = ::takeVideo)
/*
                        PreviewView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            scaleType = PreviewView.ScaleType.FILL_START
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            post {
                                cameraProviderFuture.addListener(Runnable {
                                    val cameraProvider = cameraProviderFuture.get()
                                    bindPreview(
                                        cameraProvider,
                                        lifecycleOwner,
                                        this,
                                    )
                                }, ContextCompat.getMainExecutor(context))
                            }
                        }
*/
                    }
                }
            }
        }
    }

    private fun startCamera() {

    }

    private fun takePhoto() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureStarted() {
                super.onCaptureStarted()
            }
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
            }
            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }
            override fun onCaptureProcessProgressed(progress: Int) {
                super.onCaptureProcessProgressed(progress)
            }
            override fun onPostviewBitmapAvailable(bitmap: Bitmap) {
                super.onPostviewBitmapAvailable(bitmap)
            }
        })
    }

    private fun takeVideo() {

    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private fun requestPersmissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "InKnit"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}

@Composable
fun Screen(modifier: Modifier = Modifier,
           onTakePictureClick: () -> Unit = {},
           onTakeVideoClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Button(modifier = Modifier.fillMaxWidth(), onClick = onTakePictureClick) {
            Text(text = "Take Picture")
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onTakeVideoClick) {
            Text(text = "Take Video")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InknitTheme {
        Screen()
    }
}
