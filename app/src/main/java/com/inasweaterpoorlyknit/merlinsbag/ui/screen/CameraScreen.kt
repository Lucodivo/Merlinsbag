package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.CameraViewModel

const val CAMERA_ROUTE_BASE = "camera_route"
const val CAMERA_ROUTE = "$CAMERA_ROUTE_BASE?$ARTICLE_ID_ARG={$ARTICLE_ID_ARG}"

val additionalCameraPermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_CAMERA_PERMISSIONS =
    if(additionalCameraPermissionsRequired) {
      arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    } else {
      arrayOf(permission.CAMERA)
    }

fun NavController.navigateToCamera(
    articleId: String? = null,
    navOptions: NavOptions? = null
) {
  val route = "$CAMERA_ROUTE_BASE?$ARTICLE_ID_ARG=$articleId"
  navigate(route, navOptions)
}

@Composable
fun CameraRoute(
    articleId: String? = null,
    navController: NavController,
    cameraViewModel: CameraViewModel = hiltViewModel(),
) {
  val context = LocalContext.current

  val appSettingsLauncher = rememberSettingsLauncher()

  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
    onResult = { success ->
      val cameraPictureUri = cameraViewModel.takePictureUri
      cameraViewModel.onPictureTaken(success)
      if(success) {
        if(cameraPictureUri != null) navController.navigateToAddArticle(
          uriStringArray = listOf(navigationSafeUriStringEncode(cameraPictureUri)),
          articleId = articleId,
        ) else {
          Log.e("GetContent ActivityResultContract", "Temp camera picture URI was null after picture was taken")
          context.toast(R.string.sorry_try_again)
        }
      }
    })

  val cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = {
      val uri = cameraViewModel.onTakePicture()
      if(uri != null) {
        takePictureLauncher.launch(uri)
      } else {
        Log.e("GetContent ActivityResultContract", "Temp camera picture URI was returned as null at generation")
        navController.popBackStack()
      }
    },
    onPermissionDenied = {
      navController.context.toast(R.string.camera_permission_required)
      navController.popBackStack()
    },
    onNeverAskAgain = cameraViewModel::onNeverAskAgain,
  )

  LaunchedEffect(Unit) {
    if(!cameraViewModel.pictureInProgress) {
      cameraViewModel.onCameraPermissionsLaunch()
      cameraWithPermissionsCheckLauncher.launch(REQUIRED_CAMERA_PERMISSIONS)
    }
  }

  LaunchedEffect(cameraViewModel.finished){
    cameraViewModel.finished.getContentIfNotHandled()?.let {
      navController.popBackStack()
    }
  }

  LaunchedEffect(cameraViewModel.launchSettings) {
    cameraViewModel.launchSettings.getContentIfNotHandled()?.let {
      appSettingsLauncher.launch()
    }
  }

  if(cameraViewModel.showPermissionsAlert) {
    CameraPermissionsAlertDialog(
      onDismiss = cameraViewModel::onDismissPermissionsAlert,
      onConfirm = cameraViewModel::onConfirmPermissionsAlert,
    )
  }
}

@Composable
fun CameraPermissionsAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
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
@Preview @Composable fun PreviewCameraPermissionsAlertDialog() = NoopTheme { CameraPermissionsAlertDialog(onConfirm = {}, onDismiss = {}) }
//endregion