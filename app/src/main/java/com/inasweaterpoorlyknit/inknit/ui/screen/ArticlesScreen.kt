package com.inasweaterpoorlyknit.inknit.ui.screen

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.getActivity
import com.inasweaterpoorlyknit.inknit.ui.theme.AppIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import com.inasweaterpoorlyknit.inknit.ui.toast
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticlesViewModel

const val ARTICLES_ROUTE = "articles_route"

private val REQUIRED_PERMISSIONS =
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
        arrayOf(permission.CAMERA)
    } else {
        arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    }

fun NavController.navigateToArticles(){
    navigate(ARTICLES_ROUTE) { launchSingleTop = true }
}

@Composable
fun ArticlesRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    articlesViewModel: ArticlesViewModel = hiltViewModel(), // MainMenuViewModel
){
    val thumbnailDetails = articlesViewModel.thumbnailDetails.observeAsState()
    val _appSettingsLauncher = rememberLauncherForActivityResult(StartActivityForResult()){}
    val _photoAlbumLauncher = rememberLauncherForActivityResult(GetContent()){ uri ->
        if(uri != null) {
            navController.navigateToAddArticle(uri.toString())
        } else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
    }
    val _cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResult(
        RequestMultiplePermissions()
    ){ permissions ->
        fun openAppSettings() = _appSettingsLauncher.launch(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", navController.context.packageName, null)
            }
        )

        val context = navController.context
        var permissionsGranted = true
        var userCheckedNeverAskAgain = false
        permissions.entries.forEach { entry ->
            if(!entry.value) {
                userCheckedNeverAskAgain = !shouldShowRequestPermissionRationale(navController.context.getActivity()!!, entry.key)
                permissionsGranted = false
            }
        }
        if(permissionsGranted) {
            navController.navigateToCamera()
        } else {
            if(userCheckedNeverAskAgain) {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_alert_title))
                    .setMessage(context.getString(R.string.permission_alert_justification))
                    .setNegativeButton(context.getString(R.string.permission_alert_negative)){ _, _ -> }
                    .setPositiveButton(context.getString(R.string.permission_alert_positive)){ _, _ -> openAppSettings() }
                    .show()
            } else {
                navController.context.toast("Camera permissions required")
            }
        }
    }
    ArticlesScreen(
        thumbnailUris = thumbnailDetails.value?.map { it.thumbnailUri } ?: emptyList(),
        onClickArticle = { thumbnailIndex ->
            navController.navigateToArticleDetail(thumbnailDetails.value!![thumbnailIndex].articleId)
        },
        onClickAddPhotoAlbum = {
            _photoAlbumLauncher.launch("image/*")
        },
        onClickAddPhotoCamera = {
            _cameraWithPermissionsCheckLauncher.launch(REQUIRED_PERMISSIONS)
        }
    )
}

@Composable
fun ArticlesScreen(
    thumbnailUris: List<String> = emptyList(),
    onClickArticle: (index: Int) -> Unit = {},
    onClickAddPhotoAlbum: () -> Unit = {},
    onClickAddPhotoCamera: () -> Unit = {},
) {
    val gridMinWidth = 100.dp
    val gridItemPadding = 16.dp
    val articlesGridState = rememberLazyStaggeredGridState()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            // typical dp width of a smart phone is 320dp-480dp
            columns = StaggeredGridCells.Adaptive(minSize = gridMinWidth),
            content = {
                items(count = thumbnailUris.size) { thumbnailGridItemIndex ->
                    val thumbnailUri = thumbnailUris[thumbnailGridItemIndex]
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().clickable{
                            onClickArticle(thumbnailGridItemIndex)
                        }){
                        AsyncImage(
                            model = thumbnailUri,
                            contentScale = ContentScale.Fit,
                            contentDescription = null, // TODO: Thumbnail description
                            modifier = Modifier.padding(gridItemPadding)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            state = articlesGridState,
        )

        // add article floating buttons
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            // TODO: Revert to false on release, but useful to start as true for testing
            var addButtonActive by remember { mutableStateOf(true) }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(20.dp)
            ) {
                val openAnimateFloat by animateFloatAsState(
                    targetValue = if(addButtonActive) 1.0f else 0.0f,
                    label = "floating action button size"
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.graphicsLayer {
                        scaleY = openAnimateFloat
                        scaleX = openAnimateFloat
                        // https://www.desmos.com/calculator/6ru1kya9ar
                        alpha = (-(openAnimateFloat - 1.0f)*(openAnimateFloat - 1.0f)) + 1.0f
                        transformOrigin = TransformOrigin(0.9f, 1.0f)
                    }) {
                    ExtendedFloatingActionButton(
                        text = { Text("album") },
                        icon = { Icon(AppIcons.PhotoAlbum, "add a photo from album") },
                        onClick = onClickAddPhotoAlbum
                    )
                    ExtendedFloatingActionButton(
                        text = { Text("camera") },
                        icon = { Icon(AppIcons.AddPhoto, "add a photo from camera") },
                        onClick = onClickAddPhotoCamera,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                FloatingActionButton(
                    onClick = { addButtonActive = !addButtonActive },
                ) {
                    if (addButtonActive) {
                        Icon(AppIcons.Remove, "remove icon")
                    } else {
                        Icon(AppIcons.Add, "add icon")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewArticlesScreen() {
    ArticlesScreen()
}
