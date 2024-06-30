package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndButtonContainer
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.PlaceholderThumbnailGrid
import com.inasweaterpoorlyknit.core.ui.component.SelectableStaggeredThumbnailGrid
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticlesViewModel

const val ARTICLES_ROUTE = "articles_route"

fun NavController.navigateToArticles(navOptions: NavOptions? = null) = navigate(ARTICLES_ROUTE, navOptions)

@Composable
fun ArticlesRoute(
    navController: NavController,
    articlesViewModel: ArticlesViewModel = hiltViewModel(),
) {
  val articleThumbnails by articlesViewModel.articleThumbnails.collectAsStateWithLifecycle()
  var showDeleteArticlesAlert by remember { mutableStateOf(false) }
  var editMode by remember { mutableStateOf(false) }
  val isItemSelected = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??

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

  ArticlesScreen(
    thumbnailUris = articleThumbnails,
    selectedThumbnails = isItemSelected.keys,
    editMode = editMode,
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
    onClickAddPhotoCamera = { navController.navigateToCamera() },
    onClickEdit = {
      editMode = !editMode
      if(editMode) isItemSelected.clear()
    },
    onClickDelete = { showDeleteArticlesAlert = true },
    onClickSelectionCancel = isItemSelected::clear,
    onClickSettings = { navController.navigateToSettings() },
    onConfirmDeleteArticlesAlert = {
      showDeleteArticlesAlert = false
      articlesViewModel.onDelete(isItemSelected.keys.toList())
      isItemSelected.clear()
    },
    onDismissDeleteArticlesAlert = { showDeleteArticlesAlert = false },
  )
}

@Composable
fun DeleteArticlesAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
  NoopSimpleAlertDialog(
    title = stringResource(id = R.string.delete_article),
    text = stringResource(id = R.string.deleted_articles_unrecoverable),
    headerIcon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
    onDismiss = onDismiss,
    onConfirm = onConfirm,
    confirmText = stringResource(id = R.string.delete),
    cancelText = stringResource(id = R.string.cancel),
  )

@Composable
fun ArticlesScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    thumbnailUris: LazyUriStrings?,
    selectedThumbnails: Set<Int>,
    editMode: Boolean,
    showDeleteArticlesAlert: Boolean,
    onClickArticle: (index: Int) -> Unit,
    onLongPressArticle: (index: Int) -> Unit,
    onClickAddPhotoAlbum: () -> Unit,
    onClickAddPhotoCamera: () -> Unit,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickSelectionCancel: () -> Unit,
    onClickSettings: () -> Unit,
    onConfirmDeleteArticlesAlert: () -> Unit,
    onDismissDeleteArticlesAlert: () -> Unit,
) {
  if(showDeleteArticlesAlert) {
    DeleteArticlesAlertDialog(
      onDismiss = onDismissDeleteArticlesAlert,
      onConfirm = onConfirmDeleteArticlesAlert,
    )
  }

  val layoutDir = LocalLayoutDirection.current
  val startPadding = systemBarPaddingValues.calculateStartPadding(layoutDir)
  val endPadding = systemBarPaddingValues.calculateEndPadding(layoutDir)
  Column(
    verticalArrangement = Arrangement.Top,
    modifier = Modifier.fillMaxSize().padding(start = startPadding, end = endPadding)
  ) {
    Spacer(modifier = Modifier.fillMaxWidth().height(systemBarPaddingValues.calculateTopPadding()))
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
  NoopBottomEndButtonContainer(modifier = Modifier.padding(start = startPadding, end = endPadding)) {
    NoopExpandingIconButton(
      expanded = editMode,
      collapsedIcon = IconData(NoopIcons.Edit, stringResource(R.string.enter_editing_mode)),
      expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
      verticalExpandedButtons = if(articlesAreSelected) {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.Cancel, contentDescription = stringResource(R.string.clear_selected_articles)),
            onClick = onClickSelectionCancel
          ),
          IconButtonData(
            icon = IconData(icon = NoopIcons.DeleteForever, contentDescription = stringResource(R.string.delete_selected_articles)),
            onClick = onClickDelete
          ),
        )
      } else {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.AddPhotoAlbum, contentDescription = stringResource(R.string.add_article_from_camera_roll)),
            onClick = onClickAddPhotoAlbum
          ),
          IconButtonData(
            icon = IconData(icon = NoopIcons.AddPhotoCamera, contentDescription = stringResource(R.string.add_article_from_camera)),
            onClick = onClickAddPhotoCamera
          ),
        )
      },
      horizontalExpandedButtons = if(!articlesAreSelected) {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.Settings, contentDescription = stringResource(R.string.cog)),
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
    showDeleteArticlesAlert: Boolean = false,
    selectedThumbnails: Set<Int> = emptySet(),
) = NoopTheme {
  ArticlesScreen(
    thumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails = selectedThumbnails,
    editMode = editMode,
    showDeleteArticlesAlert = showDeleteArticlesAlert,
    onClickArticle = {}, onClickAddPhotoAlbum = {}, onClickAddPhotoCamera = {}, onClickEdit = {},
    onConfirmDeleteArticlesAlert = {}, onDismissDeleteArticlesAlert = {},
    onClickDelete = {}, onClickSelectionCancel = {}, onClickSettings = {},
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
fun PreviewArticlesScreenWithDeleteArticlesAlert() = PreviewUtilArticleScreen(
  showDeleteArticlesAlert = true,
  editMode = true,
  selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
)

@Preview
@Composable
fun PreviewDeleteArticlesAlert() = NoopTheme {
  DeleteArticlesAlertDialog(onConfirm = {}, onDismiss = {})
}
//endregion