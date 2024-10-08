package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.inasweaterpoorlyknit.core.ui.LandscapePreview
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.animate.animateClosestRotationAsState
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
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.AddArticleViewModel
import kotlinx.serialization.Serializable

val additionalCameraPermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_CAMERA_PERMISSIONS =
    if(additionalCameraPermissionsRequired) {
      arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    } else {
      arrayOf(permission.CAMERA)
    }

@Serializable
data class AddArticleRouteArgs(
    val uriStrings: List<String>,
    val articleId: String?,
)

fun NavController.navigateToAddArticle(
    uriStrings: List<String> = emptyList(),
    articleId: String? = null,
    navOptions: NavOptions? = null,
) = navigate(AddArticleRouteArgs(uriStrings = uriStrings, articleId = articleId), navOptions)

@Composable
fun AddArticleRoute(
    uriStrings: List<String>,
    articleId: String? = null,
    navigateBack: () -> Unit,
    windowSizeClass: WindowSizeClass,
) {
  val context = LocalContext.current
  val addArticleViewModel = hiltViewModel<AddArticleViewModel, AddArticleViewModel.AddArticleViewModelFactory> { factory ->
    factory.create(uriStrings, articleId)
  }
  val systemAppSettingsLauncher = rememberSystemAppSettingsLauncher()

  val attachArticleThumbnails by addArticleViewModel.attachArticleThumbnails.collectAsStateWithLifecycle()
  val systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues()

  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
    onResult = { success ->
      if(!success) navigateBack()
      addArticleViewModel.onCameraPictureTaken(success)
    }
  )

  val cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = addArticleViewModel::onCameraPermissionsGranted,
    onPermissionDenied = addArticleViewModel::onCameraPermissionsDenied,
    onNeverAskAgain = addArticleViewModel::onCameraPermissionsNeverAskAgain,
  )

  LaunchedEffect(addArticleViewModel.launchCameraPermissions) {
    addArticleViewModel.launchCameraPermissions.getContentIfNotHandled()?.also {
      cameraWithPermissionsCheckLauncher.launch(REQUIRED_CAMERA_PERMISSIONS)
    }
  }

  LaunchedEffect(addArticleViewModel.navigationEventState){
    addArticleViewModel.navigationEventState.getContentIfNotHandled()?.let {
      when(it){
        AddArticleViewModel.NavigationState.Back -> navigateBack()
        AddArticleViewModel.NavigationState.SystemAppSettings -> {
          systemAppSettingsLauncher.launch()
          navigateBack()
        }
        is AddArticleViewModel.NavigationState.TakePicture -> takePictureLauncher.launch(it.tmpPhotoUri)
      }
    }
  }

  LaunchedEffect(addArticleViewModel.errorState) {
    addArticleViewModel.errorState.getContentIfNotHandled()?.let { error ->
      when(error){
        AddArticleViewModel.ErrorState.PhotoLost -> context.toast(R.string.sorry_try_again)
        AddArticleViewModel.ErrorState.PermissionsDenied -> context.toast(R.string.camera_permission_required)
        is AddArticleViewModel.ErrorState.ProcessingError -> context.toast(msg = error.msgId)
      }
    }
  }

  AddArticleScreen(
    systemBarPaddingValues = systemBarPaddingValues,
    windowSizeClass = windowSizeClass,
    processing = addArticleViewModel.processing,
    processedImage = addArticleViewModel.processedBitmap,
    imageRotation = addArticleViewModel.rotation,
    articleAttachmentIndex = addArticleViewModel.attachArticleIndex,
    attachArticleThumbnails = attachArticleThumbnails,
    attachToArticleEnabled = addArticleViewModel.attachToArticleEnabled,
    onNarrowFocusClick = addArticleViewModel::onFocusClicked,
    onBroadenFocusClick = addArticleViewModel::onWidenClicked,
    onRotateCW = addArticleViewModel::onRotateCW,
    onRotateCCW = addArticleViewModel::onRotateCCW,
    onDiscard = addArticleViewModel::onClickDiscard,
    onSave = addArticleViewModel::onSave,
    dialogState = addArticleViewModel.dialogState,
    onDismissDiscardDialog = addArticleViewModel::onDismissDiscardDialog,
    onConfirmDiscardDialog = addArticleViewModel::onDiscard,
    onDismissCameraPermissionsDialog = addArticleViewModel::onDismissCameraPermissionsAlert,
    onConfirmCameraPermissionsDialog = addArticleViewModel::onConfirmCameraPermissionsAlert,
    onAttach = addArticleViewModel::onClickAttach,
    onDismissAttachDialog = addArticleViewModel::onDismissAttachDialog,
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
    attachToArticleEnabled: Boolean,
    articleAttachmentIndex: Int?,
    onBroadenFocusClick: () -> Unit,
    onRotateCW: () -> Unit,
    onRotateCCW: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    onAttach: () -> Unit,
    dialogState: AddArticleViewModel.DialogState,
    onDismissDiscardDialog: () -> Unit,
    onConfirmDiscardDialog: () -> Unit,
    onDismissCameraPermissionsDialog: () -> Unit,
    onConfirmCameraPermissionsDialog: () -> Unit,
    onDismissAttachDialog: () -> Unit,
    onNarrowFocusClick: () -> Unit,
    removeAttachedArticle: () -> Unit,
    attachToArticle: (Int) -> Unit,
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
      attachEnabled = attachToArticleEnabled,
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
  DiscardAlertDialog(
    visible = dialogState == AddArticleViewModel.DialogState.Discard,
    onDismiss = onDismissDiscardDialog,
    onConfirm = onConfirmDiscardDialog
  )
  CameraPermissionsAlertDialog(
    visible = dialogState == AddArticleViewModel.DialogState.CameraPermissions,
    onDismiss = onDismissCameraPermissionsDialog,
    onConfirm = onConfirmCameraPermissionsDialog,
  )

  val showAttachDialog = dialogState == AddArticleViewModel.DialogState.Attach
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
          val articleThumbnailUriString = attachArticleThumbnails.getUriStrings(articleIndex)
          Box(contentAlignment = Alignment.Center) {
            val selected = articleIndex == articleAttachmentIndex
            SelectableNoopImage(
              selectable = true,
              selected = selected,
              uriString = articleThumbnailUriString.first(), // TODO: animate between thumbnails?
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
    attachEnabled: Boolean,
    onBroadenFocusClick: () -> Unit,
    onRotateCW: () -> Unit,
    onRotateCCW: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    onAttach: () -> Unit,
    onNarrowFocusClick: () -> Unit,
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
        NoopIconButton(iconData = IconData(NoopIcons.FocusNarrow, stringResource(R.string.tighten_cutout_region)), onClick = onNarrowFocusClick, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusBroaden, stringResource(R.string.loosen_cutout_region)), onClick = onBroadenFocusClick, enabled = !processing, modifier = buttonModifier)
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        NoopIconButton(iconData = IconData(NoopIcons.RotateCCW, stringResource(R.string.rotate_ccw)), onClick = onRotateCCW, enabled = !processing, modifier = buttonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.RotateCW, stringResource(R.string.rotate_cw)), onClick = onRotateCW, enabled = !processing, modifier = buttonModifier)
      }
      if(attachEnabled){
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          NoopIconButton(iconData = IconData(NoopIcons.Delete, stringResource(R.string.delete)), onClick = onDiscard, enabled = !processing, modifier = buttonModifier)
          NoopIconButton(iconData = IconData(NoopIcons.Attachment, stringResource(R.string.attach_to)), onClick = onAttach, enabled = !processing, modifier = buttonModifier)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
          NoopIconButton(iconData = IconData(NoopIcons.Check, stringResource(R.string.save)), onClick = onSave, enabled = !processing, modifier = buttonModifier.fillMaxWidth())
        }
      } else {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          NoopIconButton(iconData = IconData(NoopIcons.Delete, stringResource(R.string.delete)), onClick = onDiscard, enabled = !processing, modifier = buttonModifier)
          NoopIconButton(iconData = IconData(NoopIcons.Check, stringResource(R.string.save)), onClick = onSave, enabled = !processing, modifier = buttonModifier.fillMaxWidth())
        }
      }
    } else { // portrait
      val buttonRowModifier = if(compactWidth) Modifier.fillMaxWidth() else Modifier.wrapContentSize()
      val portraitButtonModifier = buttonModifier.weight(1f)
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.RotateCCW, stringResource(R.string.rotate_ccw)), onClick = onRotateCCW, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusNarrow, stringResource(R.string.tighten_cutout_region)), onClick = onNarrowFocusClick, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.FocusBroaden, stringResource(R.string.loosen_cutout_region)), onClick = onBroadenFocusClick, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.RotateCW, stringResource(R.string.rotate_cw)), onClick = onRotateCW, enabled = !processing, modifier = portraitButtonModifier)
      }
      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = buttonRowModifier) {
        NoopIconButton(iconData = IconData(NoopIcons.Delete, stringResource(R.string.delete)), onClick = onDiscard, enabled = !processing, modifier = portraitButtonModifier)
        if(attachEnabled) NoopIconButton(iconData = IconData(NoopIcons.Attachment, stringResource(R.string.attach_to)), onClick = onAttach, enabled = !processing, modifier = portraitButtonModifier)
        NoopIconButton(iconData = IconData(NoopIcons.Check, stringResource(R.string.save)), onClick = onSave, enabled = !processing, modifier = buttonModifier.weight(if(attachEnabled) 2f else 3f))
      }
    }
  }
}

@Composable
fun CameraPermissionsAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
  visible = visible,
  title = stringResource(id = R.string.permission_alert_title),
  text = stringResource(
    if(additionalCameraPermissionsRequired) R.string.camera_permission_alert_justification_additional
    else R.string.camera_permission_alert_justification
  ),
  headerIcon = { Icon(imageVector = NoopIcons.Camera, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
  onDismiss = onDismiss,
  onConfirm = onConfirm,
  confirmText = stringResource(id = R.string.permission_alert_positive),
  cancelText = stringResource(id = R.string.permission_alert_negative),
)

@Composable
fun DiscardAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) = NoopSimpleAlertDialog(
      visible = visible,
      title = stringResource(id = R.string.discard_article),
      text = stringResource(id = R.string.are_you_sure),
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.discard),
      cancelText = stringResource(id = R.string.cancel),
    )

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilAddArticleScreen(
    dialogState: AddArticleViewModel.DialogState =  AddArticleViewModel.DialogState.None,
    attachToArticleEnabled: Boolean = true,
) = NoopTheme(darkMode = DarkMode.DARK) {
  AddArticleScreen(
    systemBarPaddingValues = WindowInsets.systemBars.asPaddingValues(),
    windowSizeClass = currentWindowAdaptiveInfo(),
    processing = false,
    processedImage = previewAssetBitmap(filename = composePreviewArticleAsset),
    imageRotation = 270.0f,
    attachArticleThumbnails = lazyRepeatedThumbnailResourceIdsAsStrings,
    attachToArticleEnabled = attachToArticleEnabled,
    articleAttachmentIndex = 2,
    dialogState = dialogState,
    onBroadenFocusClick = {},
    onRotateCW = {}, onRotateCCW = {}, onDiscard = {}, onSave = {}, onAttach = {}, onConfirmCameraPermissionsDialog = {}, onDismissCameraPermissionsDialog = {},
    onDismissDiscardDialog = {}, onConfirmDiscardDialog = {}, onDismissAttachDialog = {}, onNarrowFocusClick = {}, removeAttachedArticle = {}, attachToArticle = {}
  )
}

@DevicePreviews @Composable fun PreviewAddArticleScreen() = PreviewUtilAddArticleScreen()
@Preview @Composable fun PreviewAddArticleScreen_discardAlertDialog() = PreviewUtilAddArticleScreen(dialogState = AddArticleViewModel.DialogState.Discard)
@Preview @Composable fun PreviewAddArticleScreen_attachToDialog() = PreviewUtilAddArticleScreen(dialogState = AddArticleViewModel.DialogState.Attach)
@Preview @Composable fun PreviewAddArticleScreen_attachToArticleDisabled() = PreviewUtilAddArticleScreen(attachToArticleEnabled = false)
@LandscapePreview @Composable fun PreviewAddArticleScreen_attachToArticleDisabled_landscape() = PreviewUtilAddArticleScreen(attachToArticleEnabled = false)
@Preview @Composable fun PreviewCameraPermissionsAlertDialog() = NoopTheme { CameraPermissionsAlertDialog(visible = true, onConfirm = {}, onDismiss = {}) }
//endregion