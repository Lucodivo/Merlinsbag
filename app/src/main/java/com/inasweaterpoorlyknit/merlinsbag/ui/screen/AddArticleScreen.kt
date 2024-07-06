package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.ARTICLE_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.DevicePreviews
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomSheetDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopRotatableImage
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.core.ui.composePreviewArticleAsset
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
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

  val attachArticleThumbnails = addArticleViewModel.attachArticleThumbnails.collectAsStateWithLifecycle()

  val systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues()

  var showDiscardAlertDialog by remember { mutableStateOf(false) }
  var showAttachDialog by remember { mutableStateOf(false) }

  AddArticleScreen(
    systemBarPaddingValues = systemBarPaddingValues,
    windowSizeClass = windowSizeClass,
    processing = addArticleViewModel.processing.value,
    processedImage = addArticleViewModel.processedBitmap.value,
    imageRotation = addArticleViewModel.rotation.floatValue,
    articleAttachmentIndex = addArticleViewModel.attachArticleIndex.value,
    attachArticleThumbnails = attachArticleThumbnails.value,
    onNarrowFocusClick = addArticleViewModel::onFocusClicked,
    onBroadenFocusClick = addArticleViewModel::onWidenClicked,
    onRotateCW = addArticleViewModel::onRotateCW,
    onRotateCCW = addArticleViewModel::onRotateCCW,
    onDiscard = { showDiscardAlertDialog = true },
    onSave = addArticleViewModel::onSave,
    onAttach = { showAttachDialog = true },
    showDiscardAlertDialog = showDiscardAlertDialog,
    onDismissDiscardDialog = { showDiscardAlertDialog = false },
    onConfirmDiscardDialog = {
      addArticleViewModel.onDiscard()
      showDiscardAlertDialog = false
    },
    showAttachDialog = showAttachDialog,
    onDismissAttachDialog = {
      showAttachDialog = false
    },
    removeAttachedArticle = addArticleViewModel::removeAttachedArticle,
    attachToArticle = addArticleViewModel::addAttachedArticle,
  )
}

@Composable
fun AddArticleScreen(
    systemBarPaddingValues: PaddingValues,
    windowSizeClass: WindowSizeClass,
    processing: Boolean,
    processedImage: Bitmap?,
    imageRotation: Float,
    attachArticleThumbnails: LazyUriStrings,
    attachToArticle: (Int) -> Unit,
    articleAttachmentIndex: Int?,
    onBroadenFocusClick: () -> Unit,
    onRotateCW: () -> Unit,
    onRotateCCW: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    onAttach: () -> Unit,
    showAttachDialog: Boolean,
    showDiscardAlertDialog: Boolean,
    onDismissDiscardDialog: () -> Unit,
    onConfirmDiscardDialog: () -> Unit,
    onDismissAttachDialog: () -> Unit,
    onNarrowFocusClick: () -> Unit,
    removeAttachedArticle: () -> Unit,
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
      onAttach = onAttach,
    )
  }
  if(landscape) {
    val layoutDir = LocalLayoutDirection.current
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(
        start = systemBarPaddingValues.calculateStartPadding(layoutDir),
        end = systemBarPaddingValues.calculateEndPadding(layoutDir)
      )
    ) {
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

  BackHandler(enabled = showAttachDialog) { onDismissAttachDialog() }
  NoopBottomSheetDialog(
    visible = showAttachDialog,
    title = stringResource(id = R.string.attach_to),
    onClose = onDismissAttachDialog,
    positiveButtonText = stringResource(id = R.string.save),
    onPositive = onSave,
  ) {
    if(attachArticleThumbnails.isNotEmpty()) {
      Text(text = stringResource(R.string.article), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
      LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.height(110.dp)
      ) {
        val padding = 10.dp
        items(count = attachArticleThumbnails.size) { articleIndex ->
          val articleThumbnailUriString = attachArticleThumbnails.getUriString(articleIndex)
          Box(contentAlignment = Alignment.Center) {
            val selected = articleIndex == articleAttachmentIndex
            SelectableNoopImage(
              selectable = true,
              selected = selected,
              uriString = articleThumbnailUriString,
              contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
              modifier = Modifier
                  .padding(padding)
                  .clickable {
                    if(selected) {
                      removeAttachedArticle()
                    } else {
                      attachToArticle(articleIndex)
                    }
                  }
            )
            if(selected) {
              Icon(
                imageVector = NoopIcons.SelectedIndicator,
                contentDescription = stringResource(com.inasweaterpoorlyknit.core.ui.R.string.selected),
                modifier = Modifier.align(Alignment.BottomEnd),
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    } else {
      Text(
        text = stringResource(R.string.no_articles_available),
        modifier = Modifier.padding(10.dp),
      )
    }
    Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateBottomPadding()))
  }
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
    onAttach: () -> Unit,
) {
  val compactWidth = windowSizeClass.compactWidth()
  Column(
    horizontalAlignment = Alignment.End,
    verticalArrangement = if(landscape) Arrangement.Center else Arrangement.Bottom,
    modifier = if(compactWidth) Modifier.fillMaxWidth() else Modifier.width(IntrinsicSize.Max)
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
        NoopIconButton(iconData = IconData(NoopIcons.Attachment, stringResource(R.string.attach_to)), onClick = onAttach, enabled = !processing, modifier = buttonModifier)
      }
      Row(horizontalArrangement = Arrangement.SpaceBetween) {
        NoopIconButton(iconData = IconData(NoopIcons.Check, stringResource(R.string.save)), onClick = onSave, enabled = !processing, modifier = buttonModifier.fillMaxWidth())
      }
    } else { // portrait
      val buttonRowModifier = if(compactWidth) Modifier.fillMaxWidth() else Modifier.wrapContentSize()
      val portraitButtonModifier = buttonModifier.weight(1f)
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.RotateCCW, stringResource(R.string.rotate_ccw)), onClick = onRotateCCW, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusNarrow, stringResource(R.string.tighten_article_crop_region)), onClick = onNarrowFocusClick, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusBroaden, stringResource(R.string.loosen_article_crop_region)), onClick = onBroadenFocusClick, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.RotateCW, stringResource(R.string.rotate_cw)), onClick = onRotateCW, enabled = !processing, modifier = portraitButtonModifier)
      }
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.Delete, stringResource(R.string.delete)), onClick = onDiscard, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Attachment, stringResource(R.string.attach_to)), onClick = onAttach, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Check, stringResource(R.string.save)), onClick = onSave, enabled = !processing, modifier = portraitButtonModifier)
      }
    }
  }
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

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilAddArticleScreen(showDiscardAlertDialog: Boolean = false, showAttachDialog: Boolean = false) = NoopTheme(darkMode = DarkMode.DARK) {
  AddArticleScreen(
    systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues(),
    windowSizeClass = currentWindowAdaptiveInfo(),
    processing = false,
    processedImage = previewAssetBitmap(filename = composePreviewArticleAsset),
    imageRotation = 270.0f,
    showDiscardAlertDialog = showDiscardAlertDialog,
    showAttachDialog = showAttachDialog,
    attachArticleThumbnails = lazyRepeatedThumbnailResourceIdsAsStrings,
    articleAttachmentIndex = 2,
    onNarrowFocusClick = {}, onBroadenFocusClick = {}, onRotateCW = {}, onRotateCCW = {}, onDiscard = {}, onSave = {},
    onDismissDiscardDialog = {}, onConfirmDiscardDialog = {}, onAttach = {}, onDismissAttachDialog = {}, attachToArticle = {}, removeAttachedArticle = {}
  )
}

@DevicePreviews @Composable fun PreviewAddArticleScreen() = PreviewUtilAddArticleScreen()
@Preview @Composable fun PreviewAddArticleScreen_discardAlertDialog() = PreviewUtilAddArticleScreen(showDiscardAlertDialog = true)
@Preview @Composable fun PreviewAddArticleScreen_attachToDialog() = PreviewUtilAddArticleScreen(showAttachDialog = true)
//endregion