package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
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
    windowSizeClass: WindowSizeClass,
    articlesViewModel: ArticlesViewModel = hiltViewModel(),
) {

  val articleThumbnails by articlesViewModel.articleThumbnails.collectAsStateWithLifecycle()

  val photoAlbumLauncher = rememberPhotoAlbumLauncher(onResult = articlesViewModel::onPhotoAlbumResults)

  LaunchedEffect(articlesViewModel.navigateToArticleDetail){
    articlesViewModel.navigateToArticleDetail.getContentIfNotHandled()?.let {
      navController.navigateToArticleDetail(it)
    }
  }
  LaunchedEffect(articlesViewModel.navigateToCamera){
    articlesViewModel.navigateToCamera.getContentIfNotHandled()?.let {
      navController.navigateToCamera()
    }
  }
  LaunchedEffect(articlesViewModel.navigateToSettings){
    articlesViewModel.navigateToSettings.getContentIfNotHandled()?.let {
      navController.navigateToSettings()
    }
  }
  LaunchedEffect(articlesViewModel.launchPhotoAlbum){
    articlesViewModel.launchPhotoAlbum.getContentIfNotHandled()?.let {
      photoAlbumLauncher.launch()
    }
  }
  LaunchedEffect(articlesViewModel.navigateToAddArticle){
    articlesViewModel.navigateToAddArticle.getContentIfNotHandled()?.let {
      navController.navigateToAddArticle(it)
    }
  }

  ArticlesScreen(
    windowSizeClass = windowSizeClass,
    thumbnailUris = articleThumbnails,
    selectedThumbnails = articlesViewModel.selectedArticleIndices,
    editMode = articlesViewModel.editMode,
    showDeleteArticlesAlert = articlesViewModel.showDeleteArticlesAlert,
    onClickArticle = articlesViewModel::onClickArticle,
    onLongPressArticle = articlesViewModel::onLongPressArticle,
    onClickAddPhotoAlbum = articlesViewModel::onClickAddPhotoAlbum,
    onClickAddPhotoCamera = articlesViewModel::onClickAddPhotoCamera,
    onClickEdit = articlesViewModel::onClickEdit,
    onClickMinimizeButtonControl = articlesViewModel::onClickMinimizeButtonControl,
    onClickDelete = articlesViewModel::onClickDelete,
    onClickClearSelection = articlesViewModel::onClickClearSelection,
    onClickSettings = articlesViewModel::onClickSettings,
    onConfirmDeleteArticlesAlert = articlesViewModel::onConfirmDeleteArticlesAlert,
    onDismissDeleteArticlesAlert = articlesViewModel::onDismissDeleteArticlesAlert,
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
    windowSizeClass: WindowSizeClass,
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
    onClickMinimizeButtonControl: () -> Unit,
    onClickDelete: () -> Unit,
    onClickClearSelection: () -> Unit,
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
    modifier = Modifier
        .fillMaxSize()
        .padding(start = startPadding, end = endPadding)
  ) {
    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(systemBarPaddingValues.calculateTopPadding()))
    val placeholderVisibilityAnimatedFloat by animateFloatAsState(
      targetValue = if(thumbnailUris?.isEmpty() == true) 1.0f else 0.0f,
      animationSpec = tween(durationMillis = 1000),
      label = "placeholder article grid visibility"
    )
    if(placeholderVisibilityAnimatedFloat == 0.0f && thumbnailUris != null) {
      SelectableStaggeredThumbnailGrid(
        selectable = editMode,
        onSelect = onClickArticle,
        onLongSelect = onLongPressArticle,
        thumbnailUris = thumbnailUris,
        selectedThumbnails = selectedThumbnails,
      )
    } else {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .alpha(placeholderVisibilityAnimatedFloat)
      ) {
        PlaceholderThumbnailGrid()
        if(thumbnailUris?.isEmpty() != false){
          val addArticleButtonAnimatedAlphaFloat by animateFloatAsState(
            targetValue = if(editMode) 0.0f else 1.0f,
            label = "add article alpha"
          )
          val buttonAlpha = 0.9f
          if(addArticleButtonAnimatedAlphaFloat > 0.0f){
            Button(
              onClick = onClickEdit,
              modifier = Modifier.alpha(buttonAlpha * addArticleButtonAnimatedAlphaFloat)
            ){
              Text(text = stringResource(R.string.add_article))
            }
          }
        }
      }
    }
  }

  val articlesAreSelected = selectedThumbnails.isNotEmpty()
  NoopBottomEndButtonContainer(modifier = Modifier.padding(start = startPadding, end = endPadding)) {
    val expanded = editMode
    NoopExpandingIconButton(
      expanded = expanded,
      collapsedIcon = IconData(NoopIcons.Edit, stringResource(R.string.enter_editing_mode)),
      expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
      verticalExpandedButtons = if(articlesAreSelected) {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.Cancel, contentDescription = stringResource(R.string.clear_selected_articles)),
            onClick = onClickClearSelection
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
      horizontalExpandedButtons = if(!articlesAreSelected && windowSizeClass.compactWidth()) {
        listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.Settings, contentDescription = stringResource(R.string.cog)),
            onClick = onClickSettings,
          ),
        )
      } else emptyList(),
      onClick = if(expanded) onClickMinimizeButtonControl else onClickEdit,
    )
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilArticleScreen(
    editMode: Boolean = false,
    showDeleteArticlesAlert: Boolean = false,
    thumbUris: LazyUriStrings? = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails: Set<Int> = emptySet(),
) = NoopTheme {
  ArticlesScreen(
    windowSizeClass = currentWindowAdaptiveInfo(),
    thumbnailUris = thumbUris,
    selectedThumbnails = selectedThumbnails,
    editMode = editMode,
    showDeleteArticlesAlert = showDeleteArticlesAlert,
    onClickArticle = {}, onClickAddPhotoAlbum = {}, onClickAddPhotoCamera = {}, onClickEdit = {},
    onConfirmDeleteArticlesAlert = {}, onDismissDeleteArticlesAlert = {},
    onClickDelete = {}, onClickClearSelection = {}, onClickSettings = {}, onLongPressArticle = {}, onClickMinimizeButtonControl = {},
  )
}

@Preview @Composable fun PreviewArticlesScreen() = PreviewUtilArticleScreen()
@Preview @Composable fun PreviewArticlesScreen_Empty() = PreviewUtilArticleScreen(thumbUris = LazyUriStrings.Empty)

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