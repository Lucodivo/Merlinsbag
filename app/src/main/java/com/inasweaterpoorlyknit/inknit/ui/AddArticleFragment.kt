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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Rotate90DegreesCcw
import androidx.compose.material.icons.outlined.Rotate90DegreesCw
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SwitchLeft
import androidx.compose.material.icons.outlined.SwitchRight
import androidx.compose.material.icons.outlined.ZoomInMap
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import com.inasweaterpoorlyknit.inknit.viewmodels.AddArticleViewModel

class AddArticleFragment: Fragment() {
  val viewModel: AddArticleViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    arguments?.let {
      val safeArgs = AddArticleFragmentArgs.fromBundle(it)
      val uri = Uri.parse(safeArgs.uri)
      viewModel.processImage(uri)
    }
    viewModel.shouldClose.observe(viewLifecycleOwner){ event ->
      event.getContentIfNotHandled()?.let { shouldClose ->
        if(shouldClose) findNavController().popBackStack(R.id.mainMenuFragment, false)
      }
    }
    return ComposeView(requireContext()).apply {
      setContent {
        AddArticleScreen(
          processedImage = viewModel.processedBitmap.value,
          imageRotation = viewModel.rotation.floatValue,
          onNarrowFocusClick = { viewModel.onFocusClicked() },
          onWidenFocusClick = { viewModel.onWidenClicked() },
          onPrevClick = { viewModel.onPrevClicked() },
          onNextClick = { viewModel.onNextClicked() },
          onRotateCW = { viewModel.onRotateCW() },
          onRotateCCW = { viewModel.onRotateCCW() },
          onSave = { viewModel.onSave() },
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
  processedImage: Bitmap? = null,
  imageRotation: Float = 0.0f,
  onPrevClick: () -> Unit = {},
  onNextClick: () -> Unit = {},
  onNarrowFocusClick: () -> Unit = {},
  onWidenFocusClick: () -> Unit = {},
  onRotateCW: () -> Unit = {},
  onRotateCCW: () -> Unit = {},
  onSave: () -> Unit = {},
) {
  InKnitTheme {
    Column {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(10f).fillMaxSize()){
        processedImage?.let {
          Image(bitmap = it.asImageBitmap(),
                contentDescription = stringResource(id = R.string.processed_image),
                modifier = Modifier.rotate(imageRotation)
          )
        }
      }
      Column(modifier = Modifier.weight(2.0f).fillMaxWidth()) {
        val buttonModifier = Modifier.weight(1f).padding(3.dp)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
          Button(onClick = onPrevClick, modifier = buttonModifier){ Icon(Icons.Outlined.SwitchLeft, "Switch left") }
          Button(onClick = onWidenFocusClick, modifier = buttonModifier){ Icon(Icons.Outlined.ZoomOutMap, "Narrow focus") }
          Button(onClick = onNarrowFocusClick, modifier = buttonModifier){ Icon(Icons.Outlined.ZoomInMap, "Broaden focus") }
          Button(onClick = onNextClick, modifier = buttonModifier) { Icon(Icons.Outlined.SwitchRight, "Switch right") }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
          Button(onClick = onRotateCCW, modifier = buttonModifier) { Icon(Icons.Outlined.Rotate90DegreesCcw, "Rotate counter-clockwise") }
          Button(onClick = onSave, modifier = buttonModifier) { Icon(Icons.Outlined.Save, "Save") }
          Button(onClick = onRotateCW, modifier = buttonModifier){ Icon(Icons.Outlined.Rotate90DegreesCw, "Rotate counter-clockwise") }
        }
      }
    }
  }
}