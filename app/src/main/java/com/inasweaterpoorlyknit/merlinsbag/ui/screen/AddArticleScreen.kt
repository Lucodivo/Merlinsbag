package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.DevicePreviews
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopRotatableImage
import com.inasweaterpoorlyknit.merlinsbag.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.merlinsbag.ui.previewAssetBitmap
import com.inasweaterpoorlyknit.merlinsbag.ui.squareishArticle
import com.inasweaterpoorlyknit.merlinsbag.ui.state.animateClosestRotationAsState
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.AddArticleViewModel

const val IMAGE_URI_STRING_LIST_ARG = "imageUriStringArray"
const val ADD_ARTICLES_BASE = "add_articles_route"
const val ADD_ARTICLES_ROUTE = "$ADD_ARTICLES_BASE?$IMAGE_URI_STRING_LIST_ARG={$IMAGE_URI_STRING_LIST_ARG}"

fun NavController.navigateToAddArticle(
  uriStringArray: List<String>,
  navOptions: NavOptions? = null
){
  val route = "$ADD_ARTICLES_BASE?$IMAGE_URI_STRING_LIST_ARG=${uriStringArray.joinToString(",")}"
  navigate(route, navOptions)
}

@Composable
fun AddArticleImage(
  modifier: Modifier = Modifier,
  processedImage: Bitmap? = null,
  angle: Float = 0.0f,
){
  val rotateAnimateFloat by animateClosestRotationAsState(targetDegrees = angle)
  NoopRotatableImage(
    modifier = modifier,
    bitmap = processedImage,
    ccwRotaitonAngle = rotateAnimateFloat,
  )
}

// TODO: User custom layout instead of onSizeChanged()
//  Preventing multiple redraws and better compose previews
@Composable
fun AddArticleControls(
  windowSizeClass: WindowSizeClass,
  landscape: Boolean = true,
  processing: Boolean = true,
  onNarrowFocusClick: () -> Unit,
  onBroadenFocusClick: () -> Unit,
  onRotateCW: () -> Unit,
  onRotateCCW: () -> Unit,
  onDiscard: () -> Unit,
  onSave: () -> Unit,
){
  val compactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
  Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = Modifier.fillMaxSize()){
    Column(
      horizontalAlignment = Alignment.End,
      verticalArrangement = if(landscape) Arrangement.Center else Arrangement.Bottom,
      modifier = Modifier
        .fillMaxHeight()
      ){
      val buttonModifier = Modifier.padding(3.dp)
      if(landscape){
        Row(horizontalArrangement = Arrangement.SpaceBetween,
        ){
          Button(onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(NoopIcons.FocusNarrow, TODO_ICON_CONTENT_DESCRIPTION) }
          Button(onClick = onBroadenFocusClick, enabled = !processing, modifier = buttonModifier){ Icon(NoopIcons.FocusBroaden, TODO_ICON_CONTENT_DESCRIPTION) }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
        ){
          Button(onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier) { Icon(NoopIcons.RotateCCW, TODO_ICON_CONTENT_DESCRIPTION) }
          Button(onClick = onRotateCW, enabled = !processing, modifier = buttonModifier){ Icon(NoopIcons.RotateCW, TODO_ICON_CONTENT_DESCRIPTION) }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
        ){
          Button(onClick = onDiscard, enabled = !processing, modifier = buttonModifier){ Icon(NoopIcons.Delete, TODO_ICON_CONTENT_DESCRIPTION) }
          Button(onClick = onSave, enabled = !processing, modifier = buttonModifier) { Icon(NoopIcons.Check, TODO_ICON_CONTENT_DESCRIPTION) }
        }
      } else { // portrait
        val buttonRowModifier = if(compactWidth) Modifier.fillMaxWidth() else Modifier.wrapContentSize()
        val portraitButtonModifier = if(compactWidth) buttonModifier.weight(1f) else buttonModifier
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier){
          Button(onClick = onNarrowFocusClick, enabled = !processing, modifier = portraitButtonModifier){ Icon(NoopIcons.FocusNarrow, TODO_ICON_CONTENT_DESCRIPTION) }
          Button(onClick = onDiscard, enabled = !processing, modifier = portraitButtonModifier){ Icon(NoopIcons.Delete, TODO_ICON_CONTENT_DESCRIPTION) }
          Button(onClick = onBroadenFocusClick, enabled = !processing, modifier = portraitButtonModifier){ Icon(NoopIcons.FocusBroaden, TODO_ICON_CONTENT_DESCRIPTION) }
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
          Button(onClick = onRotateCCW, enabled = !processing, modifier = portraitButtonModifier) { Icon(NoopIcons.RotateCCW, TODO_ICON_CONTENT_DESCRIPTION) }
          Button(onClick = onSave, enabled = !processing, modifier = portraitButtonModifier) { Icon(NoopIcons.Check, TODO_ICON_CONTENT_DESCRIPTION) }
          Button(onClick = onRotateCW, enabled = !processing, modifier = portraitButtonModifier){ Icon(NoopIcons.RotateCW, TODO_ICON_CONTENT_DESCRIPTION) }
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
  onNarrowFocusClick: () -> Unit,
  onBroadenFocusClick: () -> Unit,
  onRotateCW: () -> Unit,
  onRotateCCW: () -> Unit,
  onDiscard: () -> Unit,
  onSave: () -> Unit,
) {
  val landscape: Boolean = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
  Box(
    contentAlignment = Alignment.BottomCenter,
    modifier = Modifier.fillMaxSize()
  ) {
    AddArticleImage(
      modifier = Modifier
        .padding(
          top = 16.dp,
          bottom = if(landscape) 16.dp else 80.dp,
          start = 16.dp,
          end = if(landscape) 150.dp else 16.dp),
      processedImage = processedImage,
      angle = imageRotation,
    )
    AddArticleControls(
      windowSizeClass = windowSizeClass,
      landscape = landscape,
      processing = processing,
      onNarrowFocusClick = onNarrowFocusClick,
      onBroadenFocusClick = onBroadenFocusClick,
      onRotateCW = onRotateCW,
      onRotateCCW = onRotateCCW,
      onDiscard = onDiscard,
      onSave = onSave,
    )
  }
}

@Composable
fun AddArticleRoute(
  navController: NavController,
  imageUriStringList: List<String>,
  windowSizeClass: WindowSizeClass,
){
  val addArticleViewModel =
    hiltViewModel<AddArticleViewModel, AddArticleViewModel.AddArticleViewModelFactory> { factory ->
      factory.create(imageUriStringList)
    }

  addArticleViewModel.finished.value.getContentIfNotHandled()?.let {
      navController.popBackStack()
  }

  addArticleViewModel.noSubjectFound.value.getContentIfNotHandled()?.let {
    Toast(msg = R.string.no_subject_found)
  }

  AddArticleScreen(
    windowSizeClass = windowSizeClass,
    processing = addArticleViewModel.processing.value,
    processedImage = addArticleViewModel.processedBitmap.value,
    imageRotation = addArticleViewModel.rotation.floatValue,
    onNarrowFocusClick = addArticleViewModel::onFocusClicked,
    onBroadenFocusClick = addArticleViewModel::onWidenClicked,
    onRotateCW = addArticleViewModel::onRotateCW,
    onRotateCCW = addArticleViewModel::onRotateCCW,
    onDiscard = addArticleViewModel::onDiscard,
    onSave = addArticleViewModel::onSave,
  )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@DevicePreviews
@Composable
fun PreviewAddArticleScreen(){
  NoopTheme{
    AddArticleScreen(
      windowSizeClass = currentWindowAdaptiveInfo(),
      processing = false,
      processedImage = previewAssetBitmap(filename = squareishArticle),
      imageRotation = 270.0f,
      onNarrowFocusClick = {}, onBroadenFocusClick = {}, onRotateCW = {}, onRotateCCW = {}, onDiscard = {}, onSave = {},
    )
  }
}