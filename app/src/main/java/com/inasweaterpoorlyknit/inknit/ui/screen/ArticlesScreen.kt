package com.inasweaterpoorlyknit.inknit.ui.screen

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.component.IconData
import com.inasweaterpoorlyknit.inknit.ui.component.NoopExpandingFloatingActionButton
import com.inasweaterpoorlyknit.inknit.ui.component.SelectableArticleThumbnailGrid
import com.inasweaterpoorlyknit.inknit.ui.component.TextIconButtonData
import com.inasweaterpoorlyknit.inknit.ui.getActivity
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.ui.toast
import com.inasweaterpoorlyknit.inknit.viewmodel.ArticlesViewModel

const val ARTICLES_ROUTE = "articles_route"

val additionalCameraPermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_CAMERA_PERMISSIONS =
    if (additionalCameraPermissionsRequired) {
        arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    } else {
        arrayOf(permission.CAMERA)
    }

fun NavController.navigateToArticles(navOptions: NavOptions? = null) =
    navigate(ARTICLES_ROUTE, navOptions)

@Composable
fun ArticlesRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    articlesViewModel: ArticlesViewModel = hiltViewModel(),
) {
    val articleThumbnails by articlesViewModel.articleThumbnails.collectAsStateWithLifecycle()
    var showDeleteArticlesAlert by remember { mutableStateOf(false) }
    var showPermissionsAlert by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(true) } // TODO: Revert to false on release, but useful to start as true for testing
    val isItemSelected = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??

    val packageName = LocalContext.current.packageName
    val _appSettingsLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {}
    fun openAppSettings() = _appSettingsLauncher.launch(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    )

    val _photoAlbumLauncher = rememberLauncherForActivityResult(object : OpenMultipleDocuments() {
        override fun createIntent(context: Context, input: Array<String>): Intent {
            return super.createIntent(context, input)
                .apply { addCategory(Intent.CATEGORY_OPENABLE) }
        }
    }) { uris ->
        if (uris.isNotEmpty()) {
            navController.navigateToAddArticle(uris.map { navigationSafeUriStringEncode(it) })
        } else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
    }
    val _cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResult(
        RequestMultiplePermissions()
    ) { permissions ->
        var permissionsGranted = true
        var userCheckedNeverAskAgain = false
        permissions.entries.forEach { entry ->
            if (!entry.value) {
                userCheckedNeverAskAgain = !shouldShowRequestPermissionRationale(
                    navController.context.getActivity()!!,
                    entry.key
                )
                permissionsGranted = false
            }
        }
        if (permissionsGranted) {
            navController.navigateToCamera()
        } else {
            if (userCheckedNeverAskAgain) {
                showPermissionsAlert = true
            } else {
                navController.context.toast("Camera permissions required")
            }
        }
    }

    ArticlesScreen(
        thumbnailUris = articleThumbnails,
        selectedThumbnails = isItemSelected.keys,
        editMode = editMode,
        showPermissionsAlert = showPermissionsAlert,
        showDeleteArticlesAlert = showDeleteArticlesAlert,
        onClickArticle = { index ->
            if (editMode) {
                if (isItemSelected.contains(index)) isItemSelected.remove(index)
                else isItemSelected[index] = Unit
            } else {
                navController.navigateToArticleDetail(index)
            }
        },
        onClickAddPhotoAlbum = { _photoAlbumLauncher.launch(arrayOf("image/*")) },
        onClickAddPhotoCamera = {
            _cameraWithPermissionsCheckLauncher.launch(
                REQUIRED_CAMERA_PERMISSIONS
            )
        },
        onClickEdit = {
            editMode = !editMode
            isItemSelected.clear()
        },
        onClickDelete = { showDeleteArticlesAlert = true },
        onClickSelectionCancel = isItemSelected::clear,
        onPermissionsAlertPositive = {
            showPermissionsAlert = false
            openAppSettings()
        },
        onDeleteArticlesAlertPositive = {
            showDeleteArticlesAlert = false
            articlesViewModel.onDelete(isItemSelected.keys.toList())
            isItemSelected.clear()
        },
        onAlertNegative = {showDeleteArticlesAlert = false; showPermissionsAlert = false},
        onAlertOutside = {showDeleteArticlesAlert = false; showPermissionsAlert = false},
    )
}

@Composable
fun CameraPermissionsAlertDialog(
    onClickOutside: () -> Unit,
    onClickNegative: () -> Unit,
    onClickPositive: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = stringResource(id = R.string.permission_alert_title)) },
        text = {
            Text(
                text = stringResource(
                    id = if (additionalCameraPermissionsRequired) R.string.permission_alert_justification_additional
                    else R.string.permission_alert_justification
                )
            )
        },
        onDismissRequest = onClickOutside,
        confirmButton = {
            TextButton(onClick = onClickPositive) { Text(stringResource(id = R.string.permission_alert_positive)) }
        },
        dismissButton = {
            TextButton(onClick = onClickNegative) { Text(stringResource(id = R.string.permission_alert_negative)) }
        }
    )
}

@Composable
fun DeleteArticlesAlertDialog(
    onClickOutside: () -> Unit,
    onClickNegative: () -> Unit,
    onClickPositive: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = stringResource(id = R.string.delete_articles)) },
        text = { Text(text = stringResource(id = R.string.deleted_articles_unrecoverable)) },
        onDismissRequest = onClickOutside,
        confirmButton = {
            TextButton(onClick = onClickPositive) {
                Text(stringResource(id = R.string.delete_articles_alert_positive))
            }
        },
        dismissButton = {
            TextButton(onClick = onClickNegative) {
                Text(stringResource(id = R.string.delete_articles_alert_negative))
            }
        }
    )
}

@Composable
fun ArticlesScreen(
    thumbnailUris: List<String>,
    selectedThumbnails: Set<Int>,
    editMode: Boolean,
    showPermissionsAlert: Boolean,
    showDeleteArticlesAlert: Boolean,
    onClickArticle: (index: Int) -> Unit,
    onClickAddPhotoAlbum: () -> Unit,
    onClickAddPhotoCamera: () -> Unit,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickSelectionCancel: () -> Unit,
    onPermissionsAlertPositive: () -> Unit,
    onDeleteArticlesAlertPositive: () -> Unit,
    onAlertNegative: () -> Unit,
    onAlertOutside: () -> Unit,
) {
    if (showPermissionsAlert) {
        CameraPermissionsAlertDialog(
            onClickOutside = onAlertOutside,
            onClickNegative = onAlertNegative,
            onClickPositive = onPermissionsAlertPositive,
        )
    }

    if (showDeleteArticlesAlert) {
        DeleteArticlesAlertDialog(
            onClickOutside = onAlertOutside,
            onClickNegative = onAlertNegative,
            onClickPositive = onDeleteArticlesAlertPositive,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SelectableArticleThumbnailGrid(
            selectable = editMode,
            onSelected = { index ->
                onClickArticle(index)
            },
            thumbnailUris = thumbnailUris,
            selectedThumbnails = selectedThumbnails,
        )
        NoopExpandingFloatingActionButton(
            expanded = editMode,
            collapsedIcon = IconData(NoopIcons.Edit, TODO_ICON_CONTENT_DESCRIPTION),
            expandedIcon = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
            expandedButtons =
            if (selectedThumbnails.isNotEmpty()) {
                listOf(
                    TextIconButtonData(
                        text = "",
                        icon = IconData(
                            icon = NoopIcons.Cancel,
                            contentDescription = TODO_ICON_CONTENT_DESCRIPTION
                        ),
                        onClick = onClickSelectionCancel
                    ),
                    TextIconButtonData(
                        text = "",
                        icon = IconData(
                            icon = NoopIcons.Delete,
                            contentDescription = TODO_ICON_CONTENT_DESCRIPTION
                        ),
                        onClick = onClickDelete
                    ),
                )
            } else {
                listOf(
                    TextIconButtonData(
                        text = "",
                        icon = IconData(
                            icon = NoopIcons.AddPhotoAlbum,
                            contentDescription = TODO_ICON_CONTENT_DESCRIPTION
                        ),
                        onClick = onClickAddPhotoAlbum
                    ),
                    TextIconButtonData(
                        text = "",
                        icon = IconData(
                            icon = NoopIcons.AddPhotoCamera,
                            contentDescription = TODO_ICON_CONTENT_DESCRIPTION
                        ),
                        onClick = onClickAddPhotoCamera
                    ),
                )
            },
            onClickExpandCollapse = onClickEdit,
        )
    }
}

//region COMPOSABLE PREVIEWS
@Composable
fun __PreviewUtilArticleScreen(
    editMode: Boolean = false,
    showPermissionsAlert: Boolean = false,
    showDeleteArticlesAlert: Boolean = false,
    selectedThumbnails: Set<Int> = emptySet(),
) = ArticlesScreen(
        thumbnailUris = repeatedThumbnailResourceIdsAsStrings,
        selectedThumbnails = selectedThumbnails,
        editMode = editMode,
        showPermissionsAlert = showPermissionsAlert,
        showDeleteArticlesAlert = showDeleteArticlesAlert,
        onClickArticle = {}, onClickAddPhotoAlbum = {}, onClickAddPhotoCamera = {}, onClickEdit = {},
        onPermissionsAlertPositive = {}, onDeleteArticlesAlertPositive = {}, onAlertNegative = {},
        onClickDelete = {}, onClickSelectionCancel = {}, onAlertOutside = {},
    )

@Preview
@Composable
fun PreviewArticlesScreen() = NoopTheme { __PreviewUtilArticleScreen() }

@Preview
@Composable
fun PreviewArticlesScreen_editMode() {
    NoopTheme {
        __PreviewUtilArticleScreen(
            selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
            editMode = true,
        )
    }
}

@Preview
@Composable
fun PreviewArticlesScreenWithPermissionsAlert() {
    NoopTheme {
        __PreviewUtilArticleScreen(
            showPermissionsAlert = true,
            editMode = true,
        )
    }
}

@Preview
@Composable
fun PreviewArticlesScreenWithDeleteArticlesAlert() {
    NoopTheme {
        __PreviewUtilArticleScreen(
            showDeleteArticlesAlert = true,
            editMode = true,
            selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
        )
    }
}

@Preview
@Composable
fun PreviewCameraPermissionsAlert() {
    NoopTheme {
        CameraPermissionsAlertDialog(onClickPositive = {}, onClickNegative = {}, onClickOutside = {})
    }
}

@Preview
@Composable
fun PreviewDeleteArticlesAlert() {
    NoopTheme {
        DeleteArticlesAlertDialog(onClickPositive = {}, onClickNegative = {}, onClickOutside = {})
    }
}
//endregion