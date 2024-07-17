package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.annotation.StringRes
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

// magic number based on NowInAndroid source code
// prevents flows from timing out on basic configuration changes
const val WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS = 5_000L

@Composable
fun Toast(@StringRes msg: Int) = LocalContext.current.toast(msg)

fun Context.getActivity(): ComponentActivity? = when(this) {
  is ComponentActivity -> this
  is ContextWrapper -> baseContext.getActivity()
  else -> null
}

fun Context.toast(msg: String) = android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
fun Context.toast(@StringRes msg: Int) = android.widget.Toast.makeText(this, resources.getString(msg), android.widget.Toast.LENGTH_SHORT).show()

fun WindowSizeClass.compactWidth(): Boolean = widthSizeClass == WindowWidthSizeClass.Compact

@Composable
fun rememberLauncherForActivityResultPermissions(
    onPermissionsGranted: () -> Unit,
    onNeverAskAgain: () -> Unit,
    onPermissionDenied: () -> Unit,
): ActivityResultLauncher<Array<String>> {
  val context = LocalContext.current
  return rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { permissions ->
      var permissionsGranted = true
      var userCheckedNeverAskAgain = false
      permissions.entries.forEach { entry ->
        if(!entry.value) {
          userCheckedNeverAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(
            context.getActivity()!!,
            entry.key
          )
          permissionsGranted = false
        }
      }
      if(permissionsGranted) onPermissionsGranted()
      else if(userCheckedNeverAskAgain) onNeverAskAgain() else onPermissionDenied()
    }
  )
}

class SettingsLauncher private constructor(
    val activityResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    val packageName: String,
) {
  companion object {
    @Composable
    fun create(): SettingsLauncher {
      val packageName = LocalContext.current.packageName
      return SettingsLauncher(rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}, packageName)
    }
  }

  fun launch() = activityResultLauncher.launch(
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", packageName, null)
    }
  )
}

@Composable
fun rememberSettingsLauncher() = SettingsLauncher.create()

interface Launcher {
  fun launch()
}

@Composable
fun rememberPhotoAlbumLauncher(
    onResult: (uris: List<Uri>) -> Unit,
) = object: Launcher {
  val launcherForActivityResult = rememberLauncherForActivityResult(
    contract = object: OpenMultipleDocuments() {
      override fun createIntent(context: Context, input: Array<String>): Intent {
        return super.createIntent(context, input)
            .apply { addCategory(Intent.CATEGORY_OPENABLE) }
      }
    },
    onResult = { uris ->
      if(uris.isEmpty()) Log.i("GetContent ActivityResultContract", "Picture not returned from album")
      onResult(uris)
    },
  )

  override fun launch() = launcherForActivityResult.launch(arrayOf("image/*"))
}
