package com.inasweaterpoorlyknit.inknit.ui

import android.Manifest.permission
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
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
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme

class AddArticleFragment: Fragment() {
  val viewModel: AddArticleViewModel by viewModels()


  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    arguments?.let {
      val safeArgs = AddArticleFragmentArgs.fromBundle(it)
      val uri = Uri.parse(safeArgs.uri)
      viewModel.processImage(uri)
    }
    return ComposeView(requireContext()).apply {
      setContent {
        AddArticleScreen(
          processedImage = viewModel.processedBitmap.value,
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
  // TODO: Remove when determined to be of no use
  // Get new camera photo from user and check necessary permissions
  var pendingCameraImageUri: Uri? = null
  fun handleImageUriResult(uri: Uri) = viewModel.processImage(uri)
  val _cameraLauncher = registerForActivityResult(TakePicture()){ pictureTaken ->
    if(pictureTaken) handleImageUriResult(pendingCameraImageUri!!)
    else Log.i("TakePicture ActivityResultContract", "Picture not returned from camera")
  }
  //endregion REGISTER FOR ACTIVITY RESULTS
}

@Preview
@Composable
fun AddArticleScreen(
  onPrevClick: () -> Unit = {},
  onNextClick: () -> Unit = {},
  onFocusClick: () -> Unit = {},
  onWidenClick: () -> Unit = {},
  processedImage: Bitmap? = null
) {
  InKnitTheme {
    Column {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(10f).fillMaxSize()){
        processedImage?.let {
          Image(bitmap = it.asImageBitmap(),
                contentDescription = stringResource(id = R.string.processed_image))
        }
      }
      listOf(
        listOf(
          ImageWithTextData(R.drawable.prev_reyda_donmez, R.string.left_arrow, onClick =  onPrevClick),
          ImageWithTextData(R.drawable.next_reyda_donmez, R.string.right_arrow, onClick =  onNextClick),
        ),
        listOf(
          ImageWithTextData(R.drawable.target_3_reyda_donmez, R.string.target, onClick =  onFocusClick),
          ImageWithTextData(R.drawable.expand_reyda_donmez, R.string.outward_pointing_arrows, onClick =  onWidenClick),
        ),
      ).also { ImageWithTextColumnsOfRows(
        buttonsTopToBottom = it,
        modifier = Modifier.weight(2.0f)
      )}
    }
  }
}