package com.inasweaterpoorlyknit.inknit.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.theme.AppIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import com.inasweaterpoorlyknit.inknit.viewmodels.AddArticleViewModel

@Preview
@Composable
fun AddArticleScreen(
  processing: Boolean = true,
  processedImage: Bitmap? = null,
  imageRotation: Float = 0.0f,
  onPrevClick: () -> Unit = {},
  onNextClick: () -> Unit = {},
  onNarrowFocusClick: () -> Unit = {},
  onBroadenFocusClick: () -> Unit = {},
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
          Button(onClick = onPrevClick, enabled = !processing, modifier = buttonModifier){ Icon(
            AppIcons.Previous, "Switch left") }
          Button(onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(
            AppIcons.FocusNarrow, "Narrow focus") }
          Button(onClick = onBroadenFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(
            AppIcons.FocusBroaden, "Broaden focus") }
          Button(onClick = onNextClick, enabled = !processing, modifier = buttonModifier) { Icon(
            AppIcons.Next, "Switch right") }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
          Button(onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier) { Icon(
            AppIcons.RotateCCW, "Rotate counter-clockwise") }
          Button(onClick = onSave, enabled = !processing, modifier = buttonModifier) { Icon(
            AppIcons.Save, "Save") }
          Button(onClick = onRotateCW, enabled = !processing, modifier = buttonModifier){ Icon(
            AppIcons.RotateCW, "Rotate counter-clockwise") }
        }
      }
    }
  }
}

@Composable
fun AddArticleRoute(
  navController: NavController,
  imageUriString: String,
){
  val addArticleViewModel =
    hiltViewModel<AddArticleViewModel, AddArticleViewModel.AddArticleViewModelFactory> { factory ->
      factory.create(imageUriString)
    }

  val shouldCloseEvent = addArticleViewModel.shouldClose.observeAsState()
  shouldCloseEvent.value?.getContentIfNotHandled()?.let { shouldClose ->
    if(shouldClose) navController.navigateToArticles()
  }

  AddArticleScreen(
    processing = addArticleViewModel.processing.value,
    processedImage = addArticleViewModel.processedBitmap.value,
    imageRotation = addArticleViewModel.rotation.floatValue,
    onNarrowFocusClick = { addArticleViewModel.onFocusClicked() },
    onBroadenFocusClick = { addArticleViewModel.onWidenClicked() },
    onPrevClick = { addArticleViewModel.onPrevClicked() },
    onNextClick = { addArticleViewModel.onNextClicked() },
    onRotateCW = { addArticleViewModel.onRotateCW() },
    onRotateCCW = { addArticleViewModel.onRotateCCW() },
    onSave = { addArticleViewModel.onSave() },
  )
}

fun NavController.navigateToAddArticle(uriString: String, navOptions: NavOptions? = null){
  val route = "${ADD_ARTICLES_BASE}?${IMAGE_URI_STRING_ARG}=$uriString"
  navigate(route, navOptions)
}

const val IMAGE_URI_STRING_ARG = "imageUriString"
const val ADD_ARTICLES_BASE = "add_articles_route"
const val ADD_ARTICLES_ROUTE = "$ADD_ARTICLES_BASE?$IMAGE_URI_STRING_ARG={$IMAGE_URI_STRING_ARG}"