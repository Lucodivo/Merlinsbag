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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.ui.DevicePreviews
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopRotatableImage
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
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
      Row(horizontalArrangement = Arrangement.SpaceBetween) {
        NoopIconButton(iconData = IconData(NoopIcons.FocusNarrow, stringResource(R.string.tighten_article_crop_region)), onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusBroaden, stringResource(R.string.loosen_article_crop_region)), onClick = onBroadenFocusClick, enabled = !processing, modifier = buttonModifier)
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        NoopIconButton(iconData = IconData(NoopIcons.RotateCCW, stringResource(R.string.rotate_ccw)), onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.RotateCW, stringResource(R.string.rotate_cw)), onClick = onRotateCW, enabled = !processing, modifier = buttonModifier)
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        NoopIconButton(iconData = IconData(NoopIcons.Delete, stringResource(R.string.delete)), onClick = onDiscard, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Check, stringResource(R.string.save)), onClick = onSave, enabled = !processing, modifier = buttonModifier)
      }
    } else { // portrait
      val buttonRowModifier = if(compactWidth) Modifier.fillMaxWidth() else Modifier.wrapContentSize()
      val portraitButtonModifier = if(compactWidth) buttonModifier.weight(1f) else buttonModifier
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.FocusNarrow, stringResource(R.string.tighten_article_crop_region)), onClick = onNarrowFocusClick, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Delete, stringResource(R.string.delete)), onClick = onDiscard, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusBroaden, stringResource(R.string.loosen_article_crop_region)), onClick = onBroadenFocusClick, enabled = !processing, modifier = portraitButtonModifier)
      }
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.RotateCCW, stringResource(R.string.rotate_ccw)), onClick = onRotateCCW, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Check, stringResource(R.string.save)), onClick = onSave, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.RotateCW, stringResource(R.string.rotate_cw)), onClick = onRotateCW, enabled = !processing, modifier = portraitButtonModifier)
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
    showDiscardAlertDialog: Boolean,
    onDismissDiscardDialog: () -> Unit,
    onConfirmDiscardDialog: () -> Unit,
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
  if(showDiscardAlertDialog) DiscardAlertDialog(onDismiss = onDismissDiscardDialog, onConfirm = onConfirmDiscardDialog)
}

@Composable
fun DiscardAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
      title = stringResource(id = R.string.discard_article),
      text = stringResource(id = R.string.are_you_sure),
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.discard),
      cancelText = stringResource(id = R.string.cancel),
    )

@Composable
fun AddArticleRoute(
    navController: NavController,
    imageUriStringList: List<String>,
    windowSizeClass: WindowSizeClass,
) {
  val addArticleViewModel = hiltViewModel<AddArticleViewModel, AddArticleViewModel.AddArticleViewModelFactory> { factory ->
    factory.create(imageUriStringList)
  }

  addArticleViewModel.userFacingError.value.getContentIfNotHandled()?.let { msg ->
    Toast(msg = msg)
  }

  addArticleViewModel.finished.value.getContentIfNotHandled()?.let {
    navController.popBackStack()
  }

  val systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues()

  var showDiscardAlertDialog by remember { mutableStateOf(false) }

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
    onDiscard = { showDiscardAlertDialog = true },
    onSave = addArticleViewModel::onSave,
    showDiscardAlertDialog = showDiscardAlertDialog,
    onDismissDiscardDialog = { showDiscardAlertDialog = false },
    onConfirmDiscardDialog = {
      addArticleViewModel.onDiscard()
      showDiscardAlertDialog = false
    },
  )
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilAddArticleScreen(showDiscardAlertDialog: Boolean = false) = NoopTheme(darkMode = DarkMode.DARK) {
  AddArticleScreen(
    systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues(),
    windowSizeClass = currentWindowAdaptiveInfo(),
    processing = false,
    processedImage = previewAssetBitmap(filename = composePreviewArticleAsset),
    imageRotation = 270.0f,
    onNarrowFocusClick = {}, onBroadenFocusClick = {}, onRotateCW = {}, onRotateCCW = {}, onDiscard = {}, onSave = {},
    showDiscardAlertDialog = showDiscardAlertDialog,
    onDismissDiscardDialog = {},
    onConfirmDiscardDialog = {},
  )
}

@DevicePreviews @Composable fun PreviewAddArticleScreen() = PreviewUtilAddArticleScreen()
@Preview @Composable fun PreviewAddArticleScreen_discardAlertDialog() = PreviewUtilAddArticleScreen(showDiscardAlertDialog = true)
//endregion