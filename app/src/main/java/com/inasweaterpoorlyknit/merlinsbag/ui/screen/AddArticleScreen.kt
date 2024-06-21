package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.ui.DevicePreviews
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopRotatableImage
import com.inasweaterpoorlyknit.core.ui.composePreviewArticleAsset
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.core.ui.previewAssetBitmap
import com.inasweaterpoorlyknit.core.ui.state.animateClosestRotationAsState
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.AddArticleViewModel

const val IMAGE_URI_STRING_LIST_ARG = "imageUriStringArray"
const val ADD_ARTICLES_BASE = "add_articles_route"
const val ADD_ARTICLES_ROUTE = "$ADD_ARTICLES_BASE?$IMAGE_URI_STRING_LIST_ARG={$IMAGE_URI_STRING_LIST_ARG}"

fun NavController.navigateToAddArticle(
    uriStringArray: List<String>,
    navOptions: NavOptions? = null,
) {
  val route = "$ADD_ARTICLES_BASE?$IMAGE_URI_STRING_LIST_ARG=${uriStringArray.joinToString(",")}"
  navigate(route, navOptions)
}

@Composable
fun AddArticleImage(
    modifier: Modifier = Modifier,
    processedImage: Bitmap? = null,
    angle: Float = 0.0f,
) {
  val rotateAnimateFloat by animateClosestRotationAsState(targetDegrees = angle)
  NoopRotatableImage(
    modifier = modifier,
    bitmap = processedImage,
    ccwRotationAngle = rotateAnimateFloat,
  )
}

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
) {
  val compactWidth = windowSizeClass.compactWidth()
  Column(
    horizontalAlignment = Alignment.End,
    verticalArrangement = if(landscape) Arrangement.Center else Arrangement.Bottom,
  ) {
    val buttonModifier = Modifier.padding(3.dp)
    if(landscape) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
) {
        NoopIconButton(iconData = IconData(NoopIcons.FocusNarrow, TODO_ICON_CONTENT_DESCRIPTION), onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusBroaden, TODO_ICON_CONTENT_DESCRIPTION), onClick = onBroadenFocusClick, enabled = !processing, modifier = buttonModifier)
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        NoopIconButton(iconData = IconData(NoopIcons.RotateCCW, TODO_ICON_CONTENT_DESCRIPTION), onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.RotateCW, TODO_ICON_CONTENT_DESCRIPTION), onClick = onRotateCW, enabled = !processing, modifier = buttonModifier)
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        NoopIconButton(iconData = IconData(NoopIcons.Delete, TODO_ICON_CONTENT_DESCRIPTION), onClick = onDiscard, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Check, TODO_ICON_CONTENT_DESCRIPTION), onClick = onSave, enabled = !processing, modifier = buttonModifier)
      }
    } else { // portrait
      val buttonRowModifier = if(compactWidth) Modifier.fillMaxWidth() else Modifier.wrapContentSize()
      val portraitButtonModifier = if(compactWidth) buttonModifier.weight(1f) else buttonModifier
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.FocusNarrow, TODO_ICON_CONTENT_DESCRIPTION), onClick = onNarrowFocusClick, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Delete, TODO_ICON_CONTENT_DESCRIPTION), onClick = onDiscard, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusBroaden, TODO_ICON_CONTENT_DESCRIPTION), onClick = onBroadenFocusClick, enabled = !processing, modifier = portraitButtonModifier)
      }
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.RotateCCW, TODO_ICON_CONTENT_DESCRIPTION), onClick = onRotateCCW, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Check, TODO_ICON_CONTENT_DESCRIPTION), onClick = onSave, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.RotateCW, TODO_ICON_CONTENT_DESCRIPTION), onClick = onRotateCW, enabled = !processing, modifier = portraitButtonModifier)
      }
    }
  }
}

@Composable
fun AddArticleScreen(
    systemBarPaddingValues: PaddingValues,
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
  val landscape: Boolean = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
  val image: @Composable () -> Unit = {
    AddArticleImage(
      modifier = Modifier.padding(16.dp),
      processedImage = processedImage,
      angle = imageRotation,
    )
  }
  val controls: @Composable () -> Unit = {
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
  if(landscape){
    val layoutDir = LocalLayoutDirection.current
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(
        start = systemBarPaddingValues.calculateStartPadding(layoutDir),
        end = systemBarPaddingValues.calculateEndPadding(layoutDir)
      )
    ){
      Column(modifier = Modifier.weight(1f)) { image() }
      Column { controls() }
    }
  } else {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = systemBarPaddingValues.calculateBottomPadding())) {
      Row(modifier = Modifier.weight(1f)) { image() }
      Row { controls() }
    }
  }
}

@Composable
fun AddArticleRoute(
    navController: NavController,
    imageUriStringList: List<String>,
    windowSizeClass: WindowSizeClass,
) {
  val addArticleViewModel = hiltViewModel<AddArticleViewModel, AddArticleViewModel.AddArticleViewModelFactory> { factory ->
    factory.create(imageUriStringList)
  }

  addArticleViewModel.finished.value.getContentIfNotHandled()?.let {
    navController.popBackStack()
  }

  addArticleViewModel.noSubjectFound.value.getContentIfNotHandled()?.let {
    Toast(msg = R.string.no_subject_found)
  }

  val systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues()

  AddArticleScreen(
    systemBarPaddingValues = systemBarPaddingValues,
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

//region COMPOSABLE PREVIEWS
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@DevicePreviews
@Composable
fun PreviewAddArticleScreen() = NoopTheme(darkMode = DarkMode.DARK) {
  AddArticleScreen(
    systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues(),
    windowSizeClass = currentWindowAdaptiveInfo(),
    processing = false,
    processedImage = previewAssetBitmap(filename = composePreviewArticleAsset),
    imageRotation = 270.0f,
    onNarrowFocusClick = {}, onBroadenFocusClick = {}, onRotateCW = {}, onRotateCCW = {}, onDiscard = {}, onSave = {},
  )
}
//endregion