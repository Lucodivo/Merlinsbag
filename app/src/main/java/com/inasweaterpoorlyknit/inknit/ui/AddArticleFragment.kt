package com.inasweaterpoorlyknit.inknit.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.navigation.fragment.navArgs
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import com.inasweaterpoorlyknit.inknit.viewmodels.AddArticleViewModel
import dagger.hilt.android.AndroidEntryPoint

// TODO: This screen does not work in landscape AT ALL
@AndroidEntryPoint
class AddArticleFragment: Fragment() {
  private val args: AddArticleFragmentArgs by navArgs()
  private val viewModel: AddArticleViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    viewModel.setImage(args.uriString)

    viewModel.shouldClose.observe(viewLifecycleOwner){ event ->
      event.getContentIfNotHandled()?.let { shouldClose ->
        if(shouldClose) findNavController().popBackStack(R.id.mainMenuFragment, false)
      }
    }

    return ComposeView(requireContext()).apply {
      setContent {
        AddArticleScreen(
          processing = viewModel.processing.value,
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
}

@Preview
@Composable
fun AddArticleScreen(
  processing: Boolean = true,
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
      Box(contentAlignment = Alignment.Center, modifier = Modifier
        .weight(10f)
        .fillMaxSize()){
        if(processedImage != null){
          Image(bitmap = processedImage.asImageBitmap(),
            contentDescription = stringResource(id = R.string.processed_image),
            modifier = Modifier.rotate(imageRotation)
          )
        } else {
          CircularProgressIndicator()
        }
      }
      Column(modifier = Modifier
        .weight(2.0f)
        .fillMaxWidth()) {
        val buttonModifier = Modifier
          .weight(1f)
          .padding(3.dp)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
          Button(onClick = onPrevClick, enabled = !processing, modifier = buttonModifier){ Icon(Icons.Outlined.SwitchLeft, "Switch left") }
          Button(onClick = onWidenFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(Icons.Outlined.ZoomOutMap, "Narrow focus") }
          Button(onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(Icons.Outlined.ZoomInMap, "Broaden focus") }
          Button(onClick = onNextClick, enabled = !processing, modifier = buttonModifier) { Icon(Icons.Outlined.SwitchRight, "Switch right") }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
          Button(onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier) { Icon(Icons.Outlined.Rotate90DegreesCcw, "Rotate counter-clockwise") }
          Button(onClick = onSave, enabled = !processing, modifier = buttonModifier) { Icon(Icons.Outlined.Save, "Save") }
          Button(onClick = onRotateCW, enabled = !processing, modifier = buttonModifier){ Icon(Icons.Outlined.Rotate90DegreesCw, "Rotate counter-clockwise") }
        }
      }
    }
  }
}