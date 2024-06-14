package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingFloatingActionButton
import com.inasweaterpoorlyknit.core.ui.component.PlaceholderThumbnailGrid
import com.inasweaterpoorlyknit.core.ui.component.SelectableStaggeredThumbnailGrid
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndFloatingActionButtonContainer
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticlesViewModel

const val ARTICLES_ROUTE = "articles_route"

val additionalCameraPermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_CAMERA_PERMISSIONS =
    if(additionalCameraPermissionsRequired) {
      arrayOf(permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE)
    } else {
      arrayOf(permission.CAMERA)
    }

fun NavController.navigateToArticles(navOptions: NavOptions? = null) =
    navigate(ARTICLES_ROUTE, navOptions)

@Composable
fun ArticlesRoute(
    navController: NavController,
    articlesViewModel: ArticlesViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val articleThumbnails by articlesViewModel.articleThumbnails.collectAsStateWithLifecycle()
  var showDeleteArticlesAlert by remember { mutableStateOf(false) }
  var showPermissionsAlert by remember { mutableStateOf(false) }
  var editMode by remember { mutableStateOf(false) }
  val isItemSelected = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??

  val appSettingsLauncher = rememberSettingsLauncher()
  val photoAlbumLauncher = rememberLauncherForActivityResult(object: OpenMultipleDocuments() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
      return super.createIntent(context, input)
          .apply { addCategory(Intent.CATEGORY_OPENABLE) }
    }
  }) { uris ->
    if(uris.isNotEmpty()) {
      navController.navigateToAddArticle(uris.map { navigationSafeUriStringEncode(it) })
    } else Log.i("GetContent ActivityResultContract", "Picture not returned from album")
  }

  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
    onResult = { success ->
      if(success) {
        val cameraPictureUri = articlesViewModel.takePictureUri
        if(cameraPictureUri != null) navController.navigateToAddArticle(listOf(navigationSafeUriStringEncode(cameraPictureUri)))
        else Log.e("GetContent ActivityResultContract", "Camera picture URI was null")
      }
      articlesViewModel.pictureTaken(success, context)
    })
  articlesViewModel.launchCamera.value.getContentIfNotHandled()?.let { takePictureLauncher.launch(it) }

  val cameraWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = { articlesViewModel.onTakePicture(context) },
    onPermissionDenied = { navController.context.toast(R.string.camera_permission_required) },
    onNeverAskAgain = { showPermissionsAlert = true },
  )

  ArticlesScreen(
    thumbnailUris = articleThumbnails,
    selectedThumbnails = isItemSelected.keys,
    editMode = editMode,
    showPermissionsAlert = showPermissionsAlert,
    showDeleteArticlesAlert = showDeleteArticlesAlert,
    onClickArticle = { index ->
      if(editMode) {
        if(isItemSelected.contains(index)) isItemSelected.remove(index)
        else isItemSelected[index] = Unit
      } else {
        navController.navigateToArticleDetail(index)
      }
    },
    onLongPressArticle = { index ->
      if(!editMode) {
        editMode = true
        isItemSelected.clear()
      }
      if(isItemSelected.contains(index)) isItemSelected.remove(index)
      else isItemSelected[index] = Unit
    },
    onClickAddPhotoAlbum = { photoAlbumLauncher.launch(arrayOf("image/*")) },
    onClickAddPhotoCamera = { cameraWithPermissionsCheckLauncher.launch(REQUIRED_CAMERA_PERMISSIONS) },
    onClickEdit = {
      editMode = !editMode
      if(editMode) isItemSelected.clear()
    },
    onClickDelete = { showDeleteArticlesAlert = true },
    onClickSelectionCancel = isItemSelected::clear,
    onClickSettings = { navController.navigateToSettings() },
    onPermissionsAlertPositive = {
      showPermissionsAlert = false
      appSettingsLauncher.launch()
    },
    onDeleteArticlesAlertPositive = {
      showDeleteArticlesAlert = false
      articlesViewModel.onDelete(isItemSelected.keys.toList())
      isItemSelected.clear()
    },
    onAlertNegative = { showDeleteArticlesAlert = false; showPermissionsAlert = false },
    onAlertOutside = { showDeleteArticlesAlert = false; showPermissionsAlert = false },
  )
}

@Composable
fun CameraPermissionsAlertDialog(onClickOutside: () -> Unit, onClickNegative: () -> Unit, onClickPositive: () -> Unit) {
  val justificationText = stringResource(id = if(additionalCameraPermissionsRequired) R.string.camera_permission_alert_justification_additional
                                                      else R.string.camera_permission_alert_justification)
  AlertDialog(
    title = { Text(text = stringResource(id = R.string.permission_alert_title)) },
    text = { Text(text = justificationText) },
    onDismissRequest = onClickOutside,
    confirmButton = { TextButton(onClick = onClickPositive) { Text(stringResource(id = R.string.permission_alert_positive)) } },
    dismissButton = { TextButton(onClick = onClickNegative) { Text(stringResource(id = R.string.permission_alert_negative)) } }
  )
}

@Composable
fun DeleteArticlesAlertDialog(onClickOutside: () -> Unit, onClickNegative: () -> Unit, onClickPositive: () -> Unit) {
  AlertDialog(
    title = { Text(text = stringResource(id = R.string.delete_articles)) },
    text = { Text(text = stringResource(id = R.string.deleted_articles_unrecoverable)) },
    icon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
    onDismissRequest = onClickOutside,
    confirmButton = { TextButton(onClick = onClickPositive) { Text(stringResource(id = R.string.delete_articles_alert_positive)) } },
    dismissButton = { TextButton(onClick = onClickNegative) { Text(stringResource(id = R.string.delete_articles_alert_negative)) }
    }
  )
}

@Composable
fun ArticlesScreen(
    thumbnailUris: LazyUriStrings?,
    selectedThumbnails: Set<Int>,
    editMode: Boolean,
    showPermissionsAlert: Boolean,
    showDeleteArticlesAlert: Boolean,
    onClickArticle: (index: Int) -> Unit,
    onLongPressArticle: (index: Int) -> Unit,
    onClickAddPhotoAlbum: () -> Unit,
    onClickAddPhotoCamera: () -> Unit,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickSelectionCancel: () -> Unit,
    onClickSettings: () -> Unit,
    onPermissionsAlertPositive: () -> Unit,
    onDeleteArticlesAlertPositive: () -> Unit,
    onAlertNegative: () -> Unit,
    onAlertOutside: () -> Unit,
) {
  if(showPermissionsAlert) {
    CameraPermissionsAlertDialog(
      onClickOutside = onAlertOutside,
      onClickNegative = onAlertNegative,
      onClickPositive = onPermissionsAlertPositive,
    )
  }

  if(showDeleteArticlesAlert) {
    DeleteArticlesAlertDialog(
      onClickOutside = onAlertOutside,
      onClickNegative = onAlertNegative,
      onClickPositive = onDeleteArticlesAlertPositive,
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    val placeholderVisibilityAnimatedFloat by animateFloatAsState(
      targetValue = if(thumbnailUris?.isEmpty() == true) 1.0f else 0.0f,
      animationSpec = tween(durationMillis = 1000),
      label = "placeholder article grid visibility"
    )
    if(placeholderVisibilityAnimatedFloat == 0.0f && thumbnailUris != null){
      SelectableStaggeredThumbnailGrid(
        selectable = editMode,
        onSelect = onClickArticle,
        onLongSelect = onLongPressArticle,
        thumbnailUris = thumbnailUris,
        selectedThumbnails = selectedThumbnails,
      )
    } else {
      PlaceholderThumbnailGrid(
        modifier = Modifier.alpha(placeholderVisibilityAnimatedFloat)
      )
    }
  }

  val articlesAreSelected = selectedThumbnails.isNotEmpty()
  NoopBottomEndFloatingActionButtonContainer {
    NoopExpandingFloatingActionButton(
      expanded = editMode,
      collapsedIcon = IconData(NoopIcons.Edit, TODO_ICON_CONTENT_DESCRIPTION),
      expandedIcon = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
      verticalExpandedButtons = if(articlesAreSelected) {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.Cancel, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
            onClick = onClickSelectionCancel
          ),
          IconButtonData(
            icon = IconData(icon = NoopIcons.DeleteForever, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
            onClick = onClickDelete
          ),
        )
      } else {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.AddPhotoAlbum, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
            onClick = onClickAddPhotoAlbum
          ),
          IconButtonData(
            icon = IconData(icon = NoopIcons.AddPhotoCamera, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
            onClick = onClickAddPhotoCamera
          ),
        )
      },
      horizontalExpandedButtons = if(!articlesAreSelected) {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.Settings, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
            onClick = onClickSettings,
          ),
        )
      } else emptyList(),
      onClick = onClickEdit,
    )
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilArticleScreen(
    editMode: Boolean = false,
    showPermissionsAlert: Boolean = false,
    showDeleteArticlesAlert: Boolean = false,
    selectedThumbnails: Set<Int> = emptySet(),
) = NoopTheme {
  ArticlesScreen(
    thumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails = selectedThumbnails,
    editMode = editMode,
    showPermissionsAlert = showPermissionsAlert,
    showDeleteArticlesAlert = showDeleteArticlesAlert,
    onClickArticle = {}, onClickAddPhotoAlbum = {}, onClickAddPhotoCamera = {}, onClickEdit = {},
    onPermissionsAlertPositive = {}, onDeleteArticlesAlertPositive = {}, onAlertNegative = {},
    onClickDelete = {}, onClickSelectionCancel = {}, onClickSettings = {}, onAlertOutside = {},
    onLongPressArticle = {},
  )
}

@Preview
@Composable
fun PreviewArticlesScreen() = PreviewUtilArticleScreen()

@Preview
@Composable
fun PreviewArticlesScreen_editMode() = PreviewUtilArticleScreen(
  selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
  editMode = true,
)

@Preview
@Composable
fun PreviewArticlesScreenWithPermissionsAlert() = PreviewUtilArticleScreen(
  showPermissionsAlert = true,
  editMode = true,
)

@Preview
@Composable
fun PreviewArticlesScreenWithDeleteArticlesAlert() = PreviewUtilArticleScreen(
  showDeleteArticlesAlert = true,
  editMode = true,
  selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
)

@Preview
@Composable
fun PreviewCameraPermissionsAlert() = NoopTheme {
  CameraPermissionsAlertDialog(onClickPositive = {}, onClickNegative = {}, onClickOutside = {})
}

@Preview
@Composable
fun PreviewDeleteArticlesAlert() = NoopTheme {
  DeleteArticlesAlertDialog(onClickPositive = {}, onClickNegative = {}, onClickOutside = {})
}
//endregion