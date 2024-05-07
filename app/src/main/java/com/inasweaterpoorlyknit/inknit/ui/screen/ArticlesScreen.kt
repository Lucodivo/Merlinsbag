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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.getActivity
import com.inasweaterpoorlyknit.inknit.ui.theme.AppTheme
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitIcons
import com.inasweaterpoorlyknit.inknit.ui.toast
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticlesViewModel
import androidx.compose.material3.AlertDialog as AlertDialogCompose

const val ARTICLES_ROUTE = "articles_route"

val additionalPermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_PERMISSIONS =
   if(additionalPermissionsRequired){
        arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    } else {
        arrayOf(permission.CAMERA)
    }

fun NavController.navigateToArticles(navOptions: NavOptions? = null) = navigate(ARTICLES_ROUTE, navOptions)

@Composable
fun ArticlesRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    articlesViewModel: ArticlesViewModel = hiltViewModel(), // MainMenuViewModel
){
    val _photoAlbumLauncher = rememberLauncherForActivityResult(GetContent()){ uri ->
        if(uri != null) {
            navController.navigateToAddArticle(uri.toString())
        } else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
    }
    val _cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResult(
        RequestMultiplePermissions()
    ){ permissions ->
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
                articlesViewModel.userCheckedNeverAskAgain()
            } else {
                navController.context.toast("Camera permissions required")
            }
        }
    }

    val packageName = LocalContext.current.packageName
    val _appSettingsLauncher = rememberLauncherForActivityResult(StartActivityForResult()){}
    fun openAppSettings() = _appSettingsLauncher.launch(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    )

    val thumbnailDetails = articlesViewModel.thumbnailDetails.observeAsState()
    val showPermissionsAlert = articlesViewModel.showPermissionsAlert.observeAsState(false)
    var addButtonActive by remember { mutableStateOf(true) } // TODO: Revert to false on release, but useful to start as true for testing
    articlesViewModel.openSettings.observeAsState().value?.getContentIfNotHandled()?.let { openAppSettings() }
    ArticlesScreen(
        thumbnailUris = thumbnailDetails.value?.map { it.thumbnailUri } ?: emptyList(),
        addButtonActive = addButtonActive,
        showPermissionsAlert = showPermissionsAlert.value,
        onClickArticle = { i -> navController.navigateToArticleDetail(thumbnailDetails.value!![i].articleId) },
        onClickAddPhotoAlbum = { _photoAlbumLauncher.launch("image/*") },
        onClickAddPhotoCamera = { _cameraWithPermissionsCheckLauncher.launch(REQUIRED_PERMISSIONS) },
        onClickAddButton = { addButtonActive = !addButtonActive },
        onPermissionsAlertPositive = { articlesViewModel.onPermissionsAlertPositive() },
        onPermissionsAlertNegative = { articlesViewModel.onPermissionsAlertNegative() },
        onPermissionsAlertOutside = { articlesViewModel.onPermissionsAlertOutside() },
    )
}

@Composable
fun ArticlesScreen(
    thumbnailUris: List<String> = emptyList(),
    addButtonActive: Boolean = false,
    showPermissionsAlert: Boolean = false,
    onClickArticle: (index: Int) -> Unit = {},
    onClickAddPhotoAlbum: () -> Unit = {},
    onClickAddPhotoCamera: () -> Unit = {},
    onClickAddButton: () -> Unit = {},
    onPermissionsAlertPositive: () -> Unit = {},
    onPermissionsAlertNegative: () -> Unit = {},
    onPermissionsAlertOutside: () -> Unit = {},
) {
    val gridMinWidth = 100.dp
    val gridItemPadding = 16.dp
    val articlesGridState = rememberLazyStaggeredGridState()

    if(showPermissionsAlert) {
        AlertDialogCompose(
            title = {
                Text(text = stringResource(id = R.string.permission_alert_title))
            },
            text = {
                Text(text = stringResource(id =
                if(additionalPermissionsRequired) R.string.permission_alert_justification_additional
                else R.string.permission_alert_justification)
                )
            },
            onDismissRequest = onPermissionsAlertOutside,
            confirmButton = {
                TextButton(onClick = onPermissionsAlertPositive) {
                    Text(stringResource(id = R.string.permission_alert_positive))
                }
            },
            dismissButton = {
                TextButton(onClick = onPermissionsAlertNegative){
                    Text(stringResource(id = R.string.permission_alert_negative))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            // typical dp width of a smart phone is 320dp-480dp
            columns = StaggeredGridCells.Adaptive(minSize = gridMinWidth),
            content = {
                items(count = thumbnailUris.size) { thumbnailGridItemIndex ->
                    ArticleThumbnailImage(
                        uriString = thumbnailUris[thumbnailGridItemIndex],
                        modifier = Modifier
                            .padding(gridItemPadding)
                            .clickable { onClickArticle(thumbnailGridItemIndex) }
                            .fillMaxSize(),
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
            state = articlesGridState,
        )

        // add article floating buttons
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(20.dp)
            ) {
                val openAnimateFloat by animateFloatAsState(
                    targetValue = if(addButtonActive) 1.0f else 0.0f,
                    animationSpec = tween(),
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
                        text = { Text(stringResource(id = R.string.Album)) },
                        icon = { Icon(InKnitIcons.PhotoAlbum, "add a photo from album") },
                        onClick = onClickAddPhotoAlbum
                    )
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(id = R.string.Camera)) },
                        icon = { Icon(InKnitIcons.AddPhoto, "add a photo from camera") },
                        onClick = onClickAddPhotoCamera,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                FloatingActionButton(
                    onClick = { onClickAddButton() },
                ) {
                    if (addButtonActive) {
                        Icon(InKnitIcons.Remove, "remove icon")
                    } else {
                        Icon(InKnitIcons.Add, "add icon")
                    }
                }
            }
        }
    }
}

val allThumbnailUris = listOf(
    R.raw.test_thumb_1.toString(), R.raw.test_thumb_2.toString(), R.raw.test_thumb_3.toString(),
    R.raw.test_thumb_4.toString(), R.raw.test_thumb_5.toString(), R.raw.test_thumb_6.toString(),
    R.raw.test_thumb_7.toString(), R.raw.test_thumb_8.toString(), R.raw.test_thumb_9.toString(),
)
val testList = mutableListOf<String>().apply {
    repeat(3){ addAll(allThumbnailUris) }
}

@Preview
@Composable
fun PreviewArticlesScreen() {
    AppTheme {
        ArticlesScreen(
            thumbnailUris = testList,
            addButtonActive = true,
        )
    }
}

@Preview
@Composable
fun PreviewArticlesScreenWithAlert() {
    AppTheme {
        ArticlesScreen(
            thumbnailUris = testList,
            showPermissionsAlert = true,
            addButtonActive = true,
        )
    }
}