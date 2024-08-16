package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.CameraViewModel
import kotlinx.serialization.Serializable

val additionalCameraPermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_CAMERA_PERMISSIONS =
    if(additionalCameraPermissionsRequired) {
      arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    } else {
      arrayOf(permission.CAMERA)
    }

@Serializable
data class CameraRouteArgs(
  val articleId: String?,
)

fun NavController.navigateToCamera(
    articleId: String? = null,
    navOptions: NavOptions? = null
) = navigate(CameraRouteArgs(articleId = articleId), navOptions)

@Composable
fun CameraRoute(
    articleId: String? = null,
    navigateToAddArticle: (uriStrings: List<String>, articleId: String?) -> Unit,
    navigateBack: () -> Unit,
) {
  val context = LocalContext.current
  val cameraViewModel = hiltViewModel<CameraViewModel, CameraViewModel.CameraViewModelFactory> { factory ->
    factory.create(articleId)
  }

  val systemAppSettingsLauncher = rememberSystemAppSettingsLauncher()

  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
    onResult = cameraViewModel::onPictureTaken
  )

  val cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = cameraViewModel::onCameraPermissionsGranted,
    onPermissionDenied = cameraViewModel::onCameraPermissionsDenied,
    onNeverAskAgain = cameraViewModel::onNeverAskAgain,
  )

  LaunchedEffect(Unit) { // Note: We only ever want to
    cameraViewModel.uiState.launchCameraPermissions.getContentIfNotHandled()?.also {
      cameraWithPermissionsCheckLauncher.launch(REQUIRED_CAMERA_PERMISSIONS)
    }
  }

  LaunchedEffect(cameraViewModel.uiState.navigationEventState){
    cameraViewModel.uiState.navigationEventState.getContentIfNotHandled()?.let {
      when(it){
        CameraViewModel.NavigationState.Back -> navigateBack()
        CameraViewModel.NavigationState.SystemAppSettings -> {
          systemAppSettingsLauncher.launch()
          navigateBack()
        }
        is CameraViewModel.NavigationState.TakePicture -> takePictureLauncher.launch(it.tmpPhotoUri)
        is CameraViewModel.NavigationState.AddArticle -> navigateToAddArticle(it.uriStrings, it.articleId)
      }
    }
  }

  LaunchedEffect(cameraViewModel.uiState.errorState) {
    cameraViewModel.uiState.errorState.getContentIfNotHandled()?.let { error ->
      when(error){
        CameraViewModel.ErrorState.PhotoLost -> context.toast(R.string.sorry_try_again) // TODO: Test to ensure this works
        CameraViewModel.ErrorState.PermissionsDenied -> context.toast(R.string.camera_permission_required)
      }
    }
  }

  CameraScreen(
    showPermissionsAlert = cameraViewModel.uiState.showPermissionsAlert,
    onDismissPermissionsAlert = cameraViewModel::onDismissPermissionsAlert,
    onConfirmPermissionsAlert = cameraViewModel::onConfirmPermissionsAlert,
  )
}

@Composable
fun CameraScreen(
    showPermissionsAlert: Boolean,
    onDismissPermissionsAlert: () -> Unit,
    onConfirmPermissionsAlert: () -> Unit,
) {
  CameraPermissionsAlertDialog(
    visible = showPermissionsAlert,
    onDismiss = onDismissPermissionsAlert,
    onConfirm = onConfirmPermissionsAlert,
  )
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

//region COMPOSABLE PREVIEWS
@Preview @Composable fun PreviewCameraPermissionsAlertDialog() = NoopTheme { CameraPermissionsAlertDialog(visible = true, onConfirm = {}, onDismiss = {}) }
//endregion