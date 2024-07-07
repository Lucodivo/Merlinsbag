package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

  var showPermissionsAlert by remember { mutableStateOf(false) }

  val appSettingsLauncher = rememberSettingsLauncher()

  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
    onResult = { success ->
      val cameraPictureUri = cameraViewModel.takePictureUri
      cameraViewModel.pictureTaken(success, context)
      if(success) {
        if(cameraPictureUri != null) navController.navigateToAddArticle(
          uriStringArray = listOf(navigationSafeUriStringEncode(cameraPictureUri)),
          articleId = articleId,
        ) else {
          Log.e("GetContent ActivityResultContract", "Temp camera picture URI was null after picture was taken")
        }
      } else {
        navController.popBackStack()
      }
    })

  val cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = {
      val uri = cameraViewModel.onTakePicture(context)
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
    onNeverAskAgain = {
      showPermissionsAlert = true
    },
  )

  // TODO: Not a perfect system. If backstack popped too quickly, permissions granted may not have changed in value.
  LaunchedEffect(Unit) {
    cameraWithPermissionsCheckLauncher.launch(REQUIRED_CAMERA_PERMISSIONS)
  }

  LaunchedEffect(cameraViewModel.takePictureUri) {
  }

  if(showPermissionsAlert) {
    CameraPermissionsAlertDialog(
      onDismiss = {
        showPermissionsAlert = false
        navController.popBackStack()
      },
      onConfirm = {
        showPermissionsAlert = false
        appSettingsLauncher.launch()
        navController.popBackStack()
      }
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