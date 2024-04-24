package com.inasweaterpoorlyknit.inknit.ui

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.toast
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme

class MainMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                InKnitTheme {
                    MainMenuScreen(
                        onClickAddPhotoAlbum = { selectAlbumImage() },
                        onClickAddPhotoCamera = { selectCameraImage() },
                    )
                }
            }
        }
    }

    fun handleAlbumImageUriResult(uri: Uri){
        val uriString = uri.toString()
        val action = MainMenuFragmentDirections.actionMainMenuFragmentToAddArticleFragment(uriString)
        findNavController().navigate(action)
    }

    fun selectCameraImage() = _cameraWithPermissionsCheckLauncher.launch(REQUIRED_PERMISSIONS)
    fun selectAlbumImage() = _photoAlbumLauncher.launch("image/*")

    companion object {
        private val REQUIRED_PERMISSIONS =
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                arrayOf(permission.CAMERA)
            } else {
                arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
            }
    }

    //region REGISTER FOR ACTIVITY RESULTS
    // Go to settings
    val _appSettingsLauncher = registerForActivityResult(StartActivityForResult()){}
    val _cameraWithPermissionsCheckLauncher = registerForActivityResult(RequestMultiplePermissions()){ permissions ->
        fun openAppSettings() = _appSettingsLauncher.launch(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
        )

        val context = requireContext()
        var permissionsGranted = true
        var userCheckedNeverAskAgain = false
        permissions.entries.forEach { entry ->
            if(!entry.value) {
                userCheckedNeverAskAgain = !shouldShowRequestPermissionRationale(entry.key)
                permissionsGranted = false
            }
        }
        if(permissionsGranted) {
            val directions = MainMenuFragmentDirections.actionMainMenuFragmentToCameraFragment()
            findNavController().navigate(directions)
        } else {
            if(userCheckedNeverAskAgain) {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_alert_title))
                    .setMessage(context.getString(R.string.permission_alert_justification))
                    .setNegativeButton(context.getString(R.string.permission_alert_negative)){ _, _ -> }
                    .setPositiveButton(context.getString(R.string.permission_alert_positive)){ _, _ -> openAppSettings() }
                    .show()
            } else {
                toast("Camera permissions required")
            }
        }
    }

    // Get image already saved on phone
    val _photoAlbumLauncher = registerForActivityResult(GetContent()){ uri ->
        if(uri != null) handleAlbumImageUriResult(uri)
        else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
    }
    //endregion REGISTER FOR ACTIVITY RESULTS
}

@Preview
@Composable
fun MainMenuScreen(
    onClickAddPhotoAlbum: () -> Unit = {},
    onClickAddPhotoCamera: () -> Unit = {},
) {
    InKnitTheme {
        Box(
            contentAlignment = Alignment.BottomEnd, modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            var addButtonActive by remember { mutableStateOf(false) }
            Column(horizontalAlignment = Alignment.End) {
                if (addButtonActive) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.animateContentSize()) {
                        ExtendedFloatingActionButton(
                            text = { Text("album") },
                            icon = { Icon(Icons.Filled.PhotoAlbum, "add a photo from album") },
                            onClick = onClickAddPhotoAlbum
                        )
                        ExtendedFloatingActionButton(
                            text = { Text("camera") },
                            icon = { Icon(Icons.Filled.AddAPhoto, "add a photo from camera") },
                            onClick = onClickAddPhotoCamera,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { addButtonActive = !addButtonActive },
                ) {
                    if (addButtonActive) {
                        Icon(Icons.Filled.Remove, "addition icon")
                    } else {
                        Icon(Icons.Filled.Add, "addition icon")
                    }
                }
            }
        }
    }
}
