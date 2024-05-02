package com.inasweaterpoorlyknit.inknit.ui.screen

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.theme.AppIcons
import com.inasweaterpoorlyknit.inknit.viewmodels.AddArticleViewModel

const val IMAGE_URI_STRING_ARG = "imageUriString"
const val ADD_ARTICLES_BASE = "add_articles_route"
const val ADD_ARTICLES_ROUTE = "$ADD_ARTICLES_BASE?$IMAGE_URI_STRING_ARG={$IMAGE_URI_STRING_ARG}"

fun NavController.navigateToAddArticle(uriString: String, navOptions: NavOptions? = null){
  val route = "${ADD_ARTICLES_BASE}?${IMAGE_URI_STRING_ARG}=$uriString"
  navigate(route, navOptions)
}

fun pixelsToDp(pixels: Int) = (pixels / Resources.getSystem().displayMetrics.density).dp


@Composable
fun ArticleImage(
  modifier: Modifier = Modifier,
  processedImage: Bitmap? = null,
  imageRotation: Float = 0.0f,
){
  Box(contentAlignment = Alignment.Center,
    modifier = modifier.fillMaxSize()
  ){
    if(processedImage != null){
      // TODO: If I avoid copying this rotated Bitmap and simply can rotate the Compose Image
      //  I could not only avoid an unnecessary copy but also create very cheap rotation animations.
      val rotatedBitmap = Bitmap.createBitmap(
        processedImage,
        0, 0, processedImage.width, processedImage.height,
        Matrix().apply { postRotate(imageRotation) }, true)
      Image(
        bitmap = rotatedBitmap.asImageBitmap(),
        contentDescription = stringResource(id = R.string.processed_image),
      )
    } else {
      CircularProgressIndicator()
    }
  }
}

@Composable
fun AddArticleControls(
  windowSizeClass: WindowSizeClass,
  landscape: Boolean = true,
  processing: Boolean = true,
  onPrevClick: () -> Unit = {},
  onNextClick: () -> Unit = {},
  onNarrowFocusClick: () -> Unit = {},
  onBroadenFocusClick: () -> Unit = {},
  onRotateCW: () -> Unit = {},
  onRotateCCW: () -> Unit = {},
  onSave: () -> Unit = {},
){
  val compactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
  Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = Modifier.fillMaxSize()){
    var columnSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    Column(
      horizontalAlignment = Alignment.End,
      verticalArrangement = if(landscape) Arrangement.Center else Arrangement.Bottom,
      modifier = Modifier
        .fillMaxHeight()
        .onSizeChanged { columnSize = DpSize(pixelsToDp(it.width), pixelsToDp(it.height)) },
      ){
      var buttonModifier = Modifier.padding(3.dp)
      if(landscape){
        Row(horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.wrapContentSize()
        ){
          Button(onClick = onPrevClick, enabled = !processing, modifier = buttonModifier){ Icon(AppIcons.Previous, "Switch left") }
          Button(onClick = onNextClick, enabled = !processing, modifier = buttonModifier) { Icon(AppIcons.Next, "Switch right") }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.wrapContentSize()
        ){
          Button(onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(AppIcons.FocusNarrow, "Narrow focus") }
          Button(onClick = onBroadenFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(AppIcons.FocusBroaden, "Broaden focus") }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.wrapContentSize()
        ){
          Button(onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier) { Icon(AppIcons.RotateCCW, "Rotate counter-clockwise") }
          Button(onClick = onRotateCW, enabled = !processing, modifier = buttonModifier){ Icon(AppIcons.RotateCW, "Rotate counter-clockwise") }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.width(columnSize.width),
        ){
          Button(onClick = onSave, enabled = !processing, modifier = buttonModifier.weight(1f)) { Icon(AppIcons.Check, "Check") }
        }
      } else { // portrait
        Row(horizontalArrangement = Arrangement.SpaceBetween,
          modifier = if(compactWidth){ Modifier.fillMaxWidth() } else { Modifier.wrapContentSize() }
        ){
          if(compactWidth) { buttonModifier = buttonModifier.weight(1f) }
          Button(onClick = onPrevClick, enabled = !processing, modifier = buttonModifier){ Icon(AppIcons.Previous, "Switch left") }
          Button(onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(AppIcons.FocusNarrow, "Narrow focus") }
          Button(onClick = onBroadenFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(AppIcons.FocusBroaden, "Broaden focus") }
          Button(onClick = onNextClick, enabled = !processing, modifier = buttonModifier) { Icon(AppIcons.Next, "Switch right") }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.width(columnSize.width)) {
          Button(onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier.weight(1f)) { Icon(AppIcons.RotateCCW, "Rotate counter-clockwise") }
          Button(onClick = onSave, enabled = !processing, modifier = buttonModifier.weight(2f)) { Icon(AppIcons.Check, "Check") }
          Button(onClick = onRotateCW, enabled = !processing, modifier = buttonModifier.weight(1f)){ Icon(AppIcons.RotateCW, "Rotate counter-clockwise") }
        }
      }
    }
  }
}

@Composable
fun AddArticleScreen(
  windowSizeClass: WindowSizeClass,
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
  val landscape: Boolean = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
  Box(
    contentAlignment = Alignment.BottomCenter,
    modifier = Modifier.fillMaxSize()
  ) {
    ArticleImage(
      modifier = Modifier
        .padding(
          top = 16.dp,
          bottom = if(landscape) 16.dp else 80.dp,
          start = 16.dp,
          end = if(landscape) 150.dp else 16.dp),
      processedImage = processedImage,
      imageRotation = imageRotation,
    )
    AddArticleControls(
      windowSizeClass = windowSizeClass,
      landscape = landscape,
      processing = processing,
      onPrevClick = onPrevClick,
      onNextClick = onNextClick,
      onNarrowFocusClick = onNarrowFocusClick,
      onBroadenFocusClick = onBroadenFocusClick,
      onRotateCW = onRotateCW,
      onRotateCCW = onRotateCCW,
      onSave = onSave,
    )
  }
}

@Composable
fun AddArticleRoute(
  navController: NavController,
  imageUriString: String,
  windowSizeClass: WindowSizeClass,
){
  val addArticleViewModel =
    hiltViewModel<AddArticleViewModel, AddArticleViewModel.AddArticleViewModelFactory> { factory ->
      factory.create(imageUriString)
    }

  val finished = addArticleViewModel.finished.observeAsState()
  finished.value?.getContentIfNotHandled()?.let { finished ->
    if(finished) navController.navigateToArticles()
  }

  val noSubjectFound = addArticleViewModel.noSubjectFound.observeAsState()
  noSubjectFound.value?.getContentIfNotHandled()?.let { noSubjectFound ->
    if(noSubjectFound) {
      navController.popBackStack()
      Toast(msg = R.string.no_subject_found)
    }
  }

  AddArticleScreen(
    windowSizeClass = windowSizeClass,
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@DevicePreviews
@Composable
fun PreviewAddArticleScreen(){
  AddArticleScreen(
    windowSizeClass = currentWindowAdaptiveInfo(),
    processing = false,
    processedImage = previewAssetBitmap(filename = "add_article_compose_preview.webp"),
    imageRotation = 270.0f,
  )
}
