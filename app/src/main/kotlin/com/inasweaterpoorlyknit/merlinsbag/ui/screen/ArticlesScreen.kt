package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticlesViewModel.EditState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticlesViewModel
import kotlinx.serialization.Serializable

@Serializable
object ArticlesRouteArgs

fun NavController.navigateToArticles(navOptions: NavOptions? = null) = navigate(ArticlesRouteArgs, navOptions)

@Composable
fun ArticlesRoute(
    navigateToArticleDetail: (index: Int) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToAddArticle: (uriStrings: List<String>) -> Unit,
    windowSizeClass: WindowSizeClass,
    articlesViewModel: ArticlesViewModel = hiltViewModel(),
) {
  BackHandler(enabled = articlesViewModel.onBackEnabled, onBack = articlesViewModel::onBack)

  val articleThumbnails by articlesViewModel.articleThumbnails.collectAsStateWithLifecycle()

  val photoAlbumLauncher = rememberPhotoAlbumLauncher(onResult = articlesViewModel::onPhotoAlbumResults)

  LaunchedEffect(articlesViewModel.navigationEventState){
    articlesViewModel.navigationEventState.getContentIfNotHandled()?.let {
      when(it) {
        is ArticlesViewModel.NavigationState.AddArticle -> navigateToAddArticle(it.uriStrings)
        is ArticlesViewModel.NavigationState.ArticleDetail -> navigateToArticleDetail(it.index)
        ArticlesViewModel.NavigationState.PhotoAlbum -> photoAlbumLauncher.launch()
        ArticlesViewModel.NavigationState.Settings -> navigateToSettings()
      }
    }
  }

  ArticlesScreen(
    windowSizeClass = windowSizeClass,
    thumbnailUris = articleThumbnails,
    selectedThumbnails = articlesViewModel.selectedArticleIndices,
    editMode = articlesViewModel.editState,
    showPlaceholder = articlesViewModel.showPlaceholder,
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
fun DeleteArticlesAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
  visible = visible,
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
    thumbnailUris: LazyUriStrings,
    selectedThumbnails: Set<Int>,
    editMode: EditState,
    showPlaceholder: Boolean,
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

  val layoutDir = LocalLayoutDirection.current

  Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(
          top = systemBarPaddingValues.calculateTopPadding(),
          start = systemBarPaddingValues.calculateStartPadding(layoutDir),
          end = systemBarPaddingValues.calculateEndPadding(layoutDir)
        )
  ){
    ArticlesScreenThumbnailGrid(
      thumbnailUris = thumbnailUris,
      selectedThumbnails = selectedThumbnails,
      editMode = editMode,
      showPlaceholder = showPlaceholder,
      onClickArticle = onClickArticle,
      onLongPressArticle = onLongPressArticle,
      onClickAddArticle = onClickEdit,
    )
    ArticlesButtonControls(
      windowSizeClass = windowSizeClass,
      editMode = editMode,
      onClickClearSelection = onClickClearSelection,
      onClickDelete = onClickDelete,
      onClickAddPhotoAlbum = onClickAddPhotoAlbum,
      onClickAddPhotoCamera = onClickAddPhotoCamera,
      onClickSettings = onClickSettings,
      onClickMinimizeButtonControl = onClickMinimizeButtonControl,
      onClickEdit = onClickEdit,
    )
  }

  DeleteArticlesAlertDialog(
    visible = showDeleteArticlesAlert,
    onDismiss = onDismissDeleteArticlesAlert,
    onConfirm = onConfirmDeleteArticlesAlert,
  )
}

@Composable fun ArticlesScreenThumbnailGrid(
    thumbnailUris: LazyUriStrings,
    selectedThumbnails: Set<Int>,
    showPlaceholder: Boolean,
    editMode: EditState,
    onClickArticle: (index: Int) -> Unit,
    onLongPressArticle: (index: Int) -> Unit,
    onClickAddArticle: () -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.Top,
    modifier = Modifier.fillMaxSize()
  ) {
    val placeholderVisibilityAnimatedFloat by animateFloatAsState(
      targetValue = if(showPlaceholder) 1.0f else 0.0f,
      animationSpec = tween(durationMillis = 1000),
      label = "placeholder article grid visibility"
    )
    if(placeholderVisibilityAnimatedFloat > 0.0f) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .alpha(placeholderVisibilityAnimatedFloat)
      ) {
        PlaceholderThumbnailGrid()
        val addArticleButtonAlpha by animateFloatAsState(
          targetValue = if(editMode != EditState.DISABLED) 0.0f else 0.9f,
          animationSpec = tween(durationMillis = 400),
          label = "add article alpha"
        )
        Button(onClick = onClickAddArticle, modifier = Modifier.alpha(addArticleButtonAlpha)){
          Text(text = stringResource(R.string.add_article))
        }
      }
    } else {
      SelectableStaggeredThumbnailGrid(
        selectable = editMode == EditState.ENABLED_SELECTED_ARTICLES,
        onSelect = onClickArticle,
        onLongSelect = onLongPressArticle,
        thumbnailUris = thumbnailUris,
        selectedThumbnails = selectedThumbnails,
      )
    }
  }
}

@Composable
fun ArticlesButtonControls(
    windowSizeClass: WindowSizeClass,
    editMode: EditState,
    onClickClearSelection: () -> Unit,
    onClickDelete: () -> Unit,
    onClickAddPhotoAlbum: () -> Unit,
    onClickAddPhotoCamera: () -> Unit,
    onClickSettings: () -> Unit,
    onClickMinimizeButtonControl: () -> Unit,
    onClickEdit: () -> Unit,
){
  NoopBottomEndButtonContainer {
    val expanded = editMode != EditState.DISABLED
    NoopExpandingIconButton(
      expanded = expanded,
      collapsedIcon = IconData(NoopIcons.Edit, stringResource(R.string.enter_editing_mode)),
      expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
      verticalExpandedButtons = if(editMode == EditState.ENABLED_SELECTED_ARTICLES) {
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
      horizontalExpandedButtons = if(editMode != EditState.ENABLED_SELECTED_ARTICLES && windowSizeClass.compactWidth()) {
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
    editMode: EditState = EditState.DISABLED,
    showDeleteArticlesAlert: Boolean = false,
    showPlaceholder: Boolean = false,
    thumbUris: LazyUriStrings = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails: Set<Int> = emptySet(),
) = NoopTheme {
  ArticlesScreen(
    windowSizeClass = currentWindowAdaptiveInfo(),
    thumbnailUris = thumbUris,
    showPlaceholder = true,
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
  editMode = EditState.ENABLED_GENERAL,
)


@Preview
@Composable
fun PreviewArticlesScreenWithDeleteArticlesAlert() = PreviewUtilArticleScreen(
  showDeleteArticlesAlert = true,
  editMode = EditState.ENABLED_SELECTED_ARTICLES,
  selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
)

@Preview
@Composable
fun PreviewDeleteArticlesAlert() = NoopTheme {
  DeleteArticlesAlertDialog(visible = true, onConfirm = {}, onDismiss = {})
}
//endregion