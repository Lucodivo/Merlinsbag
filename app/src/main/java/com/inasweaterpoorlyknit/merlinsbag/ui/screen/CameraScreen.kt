package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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

const val CAMERA_ROUTE = "camera_route"

val additionalCameraPermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_CAMERA_PERMISSIONS =
    if(additionalCameraPermissionsRequired) {
      arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    } else {
      arrayOf(permission.CAMERA)
    }

fun NavController.navigateToCamera(navOptions: NavOptions? = null) = navigate(CAMERA_ROUTE, navOptions)

@Composable
fun CameraRoute(
    navController: NavController,
    cameraViewModel: CameraViewModel = hiltViewModel(),
) {
  val context = LocalContext.current

  var permissionsGranted by remember { mutableStateOf(false) }
  var showPermissionsAlert by remember { mutableStateOf(false) }

  val appSettingsLauncher = rememberSettingsLauncher()

  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
    onResult = { success ->
      val cameraPictureUri = cameraViewModel.takePictureUri
      cameraViewModel.pictureTaken(success, context)
      if(success) {
        if(cameraPictureUri != null) navController.navigateToAddArticle(listOf(navigationSafeUriStringEncode(cameraPictureUri)))
        else Log.e("GetContent ActivityResultContract", "Camera picture URI was null")
      } else {
        navController.popBackStack()
      }
    })

  val cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = {
      cameraViewModel.onTakePicture(context)
      permissionsGranted = true
    },
    onPermissionDenied = {
      navController.context.toast(R.string.camera_permission_required)
      navController.popBackStack()
    },
    onNeverAskAgain = {
      showPermissionsAlert = true
    },
  )

  LaunchedEffect(permissionsGranted) {
    if(!permissionsGranted) cameraWithPermissionsCheckLauncher.launch(REQUIRED_CAMERA_PERMISSIONS)
  }

  LaunchedEffect(cameraViewModel.takePictureUri) {
    val uri = cameraViewModel.takePictureUri
    if(uri != null) takePictureLauncher.launch(uri)
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