@file:OptIn(ExperimentalFoundationApi::class)
// NOTE: The above is for combinedClickable which gives us an easy way to determine long presses

package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.data.model.LazyFilenames
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.ARTICLE_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.allTestFullResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.allTestThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndButtonContainer
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomSheetDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopImage
import com.inasweaterpoorlyknit.core.ui.component.NoopSearchBox
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailScreenAlertDialogMode.DeleteArticle
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailScreenAlertDialogMode.DeleteArticleByRemovingAllArticles
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailScreenAlertDialogMode.ExportPermissions
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailScreenAlertDialogMode.None
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailScreenAlertDialogMode.RemoveFromEnsembles
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailScreenAlertDialogMode.RemoveImages
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticleDetailViewModel
import kotlinx.serialization.Serializable

const val thumbnailAndEnsembleHiddenPercent = 0.98f

@Serializable
data class ArticleDetailRouteArgs(
  val articleIndex: Int,
  val ensembleId: String?,
)

val storagePermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_STORAGE_PERMISSIONS = if(storagePermissionsRequired) arrayOf(permission.WRITE_EXTERNAL_STORAGE) else emptyArray()

enum class ArticleDetailScreenEditMode {
  EnabledGeneral,
  EnabledSelectedEnsembles,
  EnabledSelectedThumbnails,
  EnabledAllThumbnails,
  Disabled,
}

enum class ArticleDetailScreenAlertDialogMode {
  DeleteArticle,
  DeleteArticleByRemovingAllArticles,
  ExportPermissions,
  RemoveFromEnsembles,
  RemoveImages,
  None,
}

fun NavController.navigateToArticleDetail(articleIndex: Int, ensembleId: String? = null, navOptions: NavOptions? = null) =
  navigate(ArticleDetailRouteArgs(articleIndex = articleIndex, ensembleId = ensembleId), navOptions)

@Composable
fun ArticleDetailRoute(
    articleIndex: Int,
    filterEnsembleId: String?,
    navigateBack: () -> Unit,
    navigateToCamera: (articleId: String) -> Unit,
    navigateToEnsembleDetail: (ensembleId: String) -> Unit,
    navigateToAddArticle: (uriStrings: List<String>, articleId: String?) -> Unit,
    snackbarHostState: SnackbarHostState,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
){
  val context = LocalContext.current
  val articleDetailViewModel =
      hiltViewModel<ArticleDetailViewModel, ArticleDetailViewModel.ArticleDetailViewModelFactory> { factory ->
        factory.create(ensembleId = filterEnsembleId, articleIndex = articleIndex)
      }
  BackHandler(enabled = true, onBack = articleDetailViewModel::onBack)

  val settingsLauncher = rememberSettingsLauncher()
  val ensembleUiState by articleDetailViewModel.ensembleUiState.collectAsStateWithLifecycle()
  val lazyArticleFilenames by articleDetailViewModel.lazyArticleFilenames.collectAsStateWithLifecycle()
  val filter by articleDetailViewModel.filter.collectAsStateWithLifecycle()

  LaunchedEffect(articleDetailViewModel.finished) {
    articleDetailViewModel.finished.getContentIfNotHandled()?.let{ navigateBack() }
  }
  LaunchedEffect(articleDetailViewModel.launchSettings) {
    articleDetailViewModel.launchSettings.getContentIfNotHandled()?.let{ settingsLauncher.launch() }
  }
  LaunchedEffect(articleDetailViewModel.navigateToCamera) {
    articleDetailViewModel.navigateToCamera.getContentIfNotHandled()?.let{ navigateToCamera(it)}
  }
  LaunchedEffect(articleDetailViewModel.navigateToEnsembleDetail) {
    articleDetailViewModel.navigateToEnsembleDetail.getContentIfNotHandled()?.let{ navigateToEnsembleDetail(it) }
  }
  LaunchedEffect(articleDetailViewModel.navigateToAddArticle) {
    articleDetailViewModel.navigateToAddArticle.getContentIfNotHandled()?.let{ (articleId, uris) ->
      navigateToAddArticle(uris, articleId)
    }
  }
  LaunchedEffect(articleDetailViewModel.exportedImage) {
    articleDetailViewModel.exportedImage.getContentIfNotHandled()?.let { exportedImageUri ->
      when(snackbarHostState.showSnackbar(
        message = context.getString(R.string.image_exported),
        actionLabel = context.getString(R.string.open),
        duration = SnackbarDuration.Short,
      )) {
        SnackbarResult.Dismissed -> {}
        SnackbarResult.ActionPerformed -> {
          val intent = Intent()
              .setAction(Intent.ACTION_VIEW)
              .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
              .setDataAndType(exportedImageUri, "image/webp")
          val pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
          pIntent.send()
        }
      }
    }
  }

  val pagerState = rememberPagerState(
    initialPage = articleDetailViewModel.articleIndex,
    initialPageOffsetFraction = 0.0f,
    pageCount = { lazyArticleFilenames.size },
  )
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect {
        i -> articleDetailViewModel.onArticleFocus(i)
    }
  }

  val photoAlbumLauncher = rememberPhotoAlbumLauncher { uris -> articleDetailViewModel.onPhotoAlbumResult(uris) }
  val exportWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = articleDetailViewModel::onExportPermissionsGranted,
    onPermissionDenied = { context.toast(R.string.storage_permissions_required) },
    onNeverAskAgain = articleDetailViewModel::neverAskExportPermissionAgain,
  )

  ArticleDetailScreen(
    windowSizeClass = windowSizeClass,
    filter = filter,
    articlesWithImages = lazyArticleFilenames,
    articleEnsembleTitles = ensembleUiState.articleEnsembles.map { it.title }, // TODO: prevent mapping on every recomposition
    pagerState = pagerState,
    articleImageIndices = articleDetailViewModel.articleImageIndices,
    ensembleListState = articleDetailViewModel.ensembleListState,
    thumbnailsAltsListState = articleDetailViewModel.thumbnailAltsListState,
    selectedEnsembles = articleDetailViewModel.selectedEnsembles,
    selectedImages = articleDetailViewModel.selectedThumbnails,
    editMode = articleDetailViewModel.editMode,
    exportingEnabled = articleDetailViewModel.exportButtonEnabled,
    alertDialogMode = articleDetailViewModel.alertDialogMode,
    showAddToEnsembleDialog = articleDetailViewModel.showAddToEnsemblesDialog,
    onClickEdit = articleDetailViewModel::onClickEdit,
    onClickMinimizeButtonControl = articleDetailViewModel::onClickMinimizeButtonControl,
    onClickExport = { exportWithPermissionsCheckLauncher.launch(REQUIRED_STORAGE_PERMISSIONS) },
    onClickAddPhotoFromAlbum = photoAlbumLauncher::launch,
    onClickAddPhotoFromCamera = articleDetailViewModel::onClickCamera,
    onClickDelete = articleDetailViewModel::onClickDelete,
    onDismissDeleteDialog = articleDetailViewModel::onDismissDeleteArticleDialog,
    onConfirmDeleteDialog = articleDetailViewModel::onConfirmDeleteArticleDialog,
    onDismissRemoveFromEnsemblesDialog = articleDetailViewModel::onDismissRemoveFromEnsemblesArticleDialog,
    onConfirmRemoveFromEnsemblesDialog = articleDetailViewModel::onConfirmRemoveFromEnsemblesArticleDialog,
    onClickThumbnail = articleDetailViewModel::onClickArticleThumbnail,
    onLongPressThumbnail = articleDetailViewModel::onLongPressArticleThumbnail,
    onDismissExportPermissionsDialog = articleDetailViewModel::onDismissExportPermissionsArticleDialog,
    onConfirmExportPermissionsDialog = articleDetailViewModel::onConfirmExportPermissionsArticleDialog,
    onClickEnsemble = articleDetailViewModel::onClickEnsemble,
    onLongPressEnsemble = articleDetailViewModel::onLongPressEnsemble,
    onClickRemoveEnsembles = articleDetailViewModel::onClickRemoveFromEnsembles,
    onClickRemoveImages = articleDetailViewModel::onClickRemoveImages,
    onDismissRemoveImagesDialog = articleDetailViewModel::onDismissRemoveImagesArticleDialog,
    onConfirmRemoveImagesDialog = articleDetailViewModel::onConfirmRemoveImagesArticleDialog,
    onClickCancelSelection = articleDetailViewModel::onClickCancelSelection,
    onClickAddToEnsemble = articleDetailViewModel::onClickAddToEnsemble,
    addNewEnsemble = articleDetailViewModel::addArticleToNewEnsemble,
    addArticleToEnsemble = articleDetailViewModel::addArticleToEnsemble,
    onCloseAddToEnsembleDialog = articleDetailViewModel::onCloseAddToEnsembleDialog,
    onSearchQueryUpdateAddToEnsembles = articleDetailViewModel::searchEnsembles,
    ensemblesSearchQuery = articleDetailViewModel.ensemblesSearchQuery,
    searchQueryUniqueTitle = ensembleUiState.searchIsUniqueTitle,
    ensemblesToAdd = ensembleUiState.searchEnsembles,
    newlyAddedEnsembles = articleDetailViewModel.newlyAddedEnsembles,
    modifier = modifier,
  )
}

@Composable
fun ArticleDetailScreen(
    windowSizeClass: WindowSizeClass,
    filter: String,
    articleEnsembleTitles: List<String>,
    pagerState: PagerState,
    articleImageIndices: List<Int>,
    articlesWithImages: LazyFilenames,
    editMode: ArticleDetailScreenEditMode,
    alertDialogMode: ArticleDetailScreenAlertDialogMode,
    showAddToEnsembleDialog: Boolean,
    onClickDelete: () -> Unit,
    onClickRemoveEnsembles: () -> Unit,
    onClickCancelSelection: () -> Unit,
    onClickAddToEnsemble: () -> Unit,
    onClickAddPhotoFromAlbum: () -> Unit,
    onClickAddPhotoFromCamera: () -> Unit,
    onClickRemoveImages: () -> Unit,
    onClickThumbnail: (index: Int) -> Unit,
    onLongPressThumbnail: (index: Int) -> Unit,
    ensembleListState: LazyListState,
    thumbnailsAltsListState: LazyListState,
    onConfirmRemoveFromEnsemblesDialog: () -> Unit,
    onDismissRemoveFromEnsemblesDialog: () -> Unit,
    onDismissExportPermissionsDialog: () -> Unit,
    onConfirmExportPermissionsDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onDismissRemoveImagesDialog: () -> Unit,
    onConfirmRemoveImagesDialog: () -> Unit,
    onLongPressEnsemble: (index: Int) -> Unit,
    exportingEnabled: Boolean,
    onClickEdit: () -> Unit,
    onClickMinimizeButtonControl: () -> Unit,
    selectedEnsembles: Set<Int>,
    selectedImages: Set<Int>,
    ensemblesToAdd: List<Ensemble>,
    addNewEnsemble: (String) -> Unit,
    onCloseAddToEnsembleDialog: () -> Unit,
    addArticleToEnsemble: (String) -> Unit,
    ensemblesSearchQuery: String,
    searchQueryUniqueTitle: Boolean,
    onSearchQueryUpdateAddToEnsembles: (String) -> Unit,
    newlyAddedEnsembles: Set<String>,
    onConfirmDeleteDialog: () -> Unit,
    onClickEnsemble: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    onClickExport: () -> Unit,
) {
  val layoutDir = LocalLayoutDirection.current
  val systemBarTopPadding = systemBarPaddingValues.calculateTopPadding()
  val systemBarStartPadding = systemBarPaddingValues.calculateStartPadding(layoutDir)
  val systemBarEndPadding = systemBarPaddingValues.calculateEndPadding(layoutDir)
  val articleIndex = pagerState.currentPage
  // Index may be out of bounds is the Pager has yet to adjust to the new size after a deletion
  val showAltThumbnails = articleIndex < articlesWithImages.size && articlesWithImages.lazyThumbImageUris.getUriStrings(articleIndex).size > 1

  val verticalArticleImagePadding = 16.dp
  val startArticleImagePadding: Dp = if(systemBarStartPadding > 0.dp) systemBarStartPadding else verticalArticleImagePadding
  val endArticleImagePadding: Dp = if(systemBarEndPadding > 0.dp) systemBarEndPadding else verticalArticleImagePadding

  if(articleImageIndices.isNotEmpty()) {
    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Bottom,
      modifier = Modifier.sizeIn(minHeight = ButtonDefaults.MinHeight)
    ) { page ->
      val imageUris = articlesWithImages.lazyFullImageUris.getUriStrings(page)
      val imageIndex = articleImageIndices[page]
      NoopImage(
        uriString = imageUris[imageIndex],
        contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
        modifier = modifier
            .fillMaxSize()
            .padding(top = verticalArticleImagePadding, bottom = verticalArticleImagePadding, start = startArticleImagePadding, end = endArticleImagePadding),
      )
    }
  }
  val compactWidth = windowSizeClass.compactWidth()
  val iconModifier = Modifier.padding(start = if(compactWidth) 0.dp else 16.dp, end = 4.dp)
  if(filter.isNotEmpty()) {
    Box(
      contentAlignment = if(compactWidth) Alignment.TopCenter else Alignment.TopStart,
      modifier = Modifier
          .fillMaxSize()
          .padding(top = systemBarTopPadding)
    ) {
      Row {
        Icon(NoopIcons.ensembles(), stringResource(R.string.hashtag), modifier = iconModifier)
        Text(text = filter, textAlign = TextAlign.Start, fontSize = MaterialTheme.typography.titleLarge.fontSize)
      }
    }
  }
  val ensembleChips: @Composable () -> Unit = {
    EnsembleLazyChips(
      compactWidth = compactWidth,
      selectable = editMode == ArticleDetailScreenEditMode.EnabledSelectedEnsembles,
      onLongPressEnsemble = onLongPressEnsemble,
      onClickEnsemble = onClickEnsemble,
      ensembleListState = ensembleListState,
      articleEnsembleTitles = articleEnsembleTitles,
      selectedEnsembles = selectedEnsembles,
      topPadding = systemBarTopPadding,
    )
  }

  val thumbnailSize = ButtonDefaults.MinHeight
  val thumbnailsSelectable = editMode == ArticleDetailScreenEditMode.EnabledSelectedThumbnails
  if(compactWidth) {
    Box(
      contentAlignment = Alignment.BottomStart,
      modifier = Modifier
          .fillMaxSize()
    ) {
      Column {
        ensembleChips()
        if(showAltThumbnails) {
          LazyRow(state = thumbnailsAltsListState, modifier = Modifier.fillMaxWidth()) {
            val thumbnailUris = articlesWithImages.lazyThumbImageUris.getUriStrings(articleIndex)
            item { Spacer(modifier = Modifier.width(8.dp)) }
            items(count = thumbnailUris.size) { thumbnailIndex ->
              SelectableNoopImage(
                uriString = thumbnailUris[thumbnailIndex],
                contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
                selected = selectedImages.contains(thumbnailIndex),
                selectable = thumbnailsSelectable,
                modifier = Modifier
                    .size(thumbnailSize)
                    .combinedClickable(
                      onClick = { onClickThumbnail(thumbnailIndex) },
                      onLongClick = { onLongPressThumbnail(thumbnailIndex) }
                    )
              )
            }
            item { Spacer(modifier = Modifier.fillParentMaxWidth(thumbnailAndEnsembleHiddenPercent)) }
          }
        } else {
          Spacer(modifier = Modifier.height(thumbnailSize))
        }
      }
    }
  } else {
    val boxModifier = Modifier
        .fillMaxSize()
        .padding(
          start = systemBarStartPadding,
          end = systemBarEndPadding,
        )
    Box(
      contentAlignment = Alignment.TopEnd,
      modifier = boxModifier
    ) {
      ensembleChips()
    }
    if(showAltThumbnails) {
      Box(
        contentAlignment = Alignment.BottomStart,
        modifier = boxModifier
      ) {
        LazyColumn(
          reverseLayout = true,
          state = thumbnailsAltsListState,
          modifier = Modifier.fillMaxHeight()
        ) {
          val thumbnailUris = articlesWithImages.lazyThumbImageUris.getUriStrings(articleIndex)
          item { Spacer(modifier = Modifier.height(8.dp)) }
          items(count = thumbnailUris.size) { thumbnailIndex ->
            SelectableNoopImage(
              uriString = thumbnailUris[thumbnailIndex],
              contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
              selected = selectedImages.contains(thumbnailIndex),
              selectable = thumbnailsSelectable,
              modifier = Modifier
                  .size(ButtonDefaults.MinHeight)
                  .combinedClickable(
                    onClick = { onClickThumbnail(thumbnailIndex) },
                    onLongClick = { onLongPressThumbnail(thumbnailIndex) }
                  )
            )
          }
          item { Spacer(modifier = Modifier.fillParentMaxHeight(thumbnailAndEnsembleHiddenPercent)) }
        }

      }
    }
  }
  FloatingActionButtonDetailScreen(
    editMode = editMode,
    exportingEnabled = exportingEnabled,
    onClickEdit = onClickEdit,
    onClickMinimizeButtonControl = onClickMinimizeButtonControl,
    onClickDelete = onClickDelete,
    onClickExport = onClickExport,
    onClickRemoveEnsembles = onClickRemoveEnsembles,
    onClickRemoveImages = onClickRemoveImages,
    onClickCancelSelection = onClickCancelSelection,
    onClickAddToEnsemble = onClickAddToEnsemble,
    onClickAddPhotoFromAlbum = onClickAddPhotoFromAlbum,
    onClickAddPhotoFromCamera = onClickAddPhotoFromCamera,
    modifier = Modifier.padding(
      start = systemBarStartPadding,
      end = systemBarEndPadding,
    ),
  )
  when(alertDialogMode){
    DeleteArticle -> DeleteArticleAlertDialog(onDismiss = onDismissDeleteDialog, onConfirm = onConfirmDeleteDialog)
    DeleteArticleByRemovingAllArticles -> DeleteArticleAlertDialog(fromDeletingAllImages = true, onDismiss = onDismissDeleteDialog, onConfirm = onConfirmDeleteDialog)
    ExportPermissions -> ExportPermissionsAlertDialog(onDismiss = onDismissExportPermissionsDialog, onConfirm = onConfirmExportPermissionsDialog)
    RemoveFromEnsembles -> RemoveFromEnsemblesAlertDialog(onDismiss = onDismissRemoveFromEnsemblesDialog, onConfirm = onConfirmRemoveFromEnsemblesDialog)
    RemoveImages -> RemoveImagesAlertDialog(onDismiss = onDismissRemoveImagesDialog, onConfirm = onConfirmRemoveImagesDialog)
    None -> {}
  }
  AddToEnsembleDialog(
    visible = showAddToEnsembleDialog,
    userSearch = ensemblesSearchQuery,
    ensemblesToAdd = ensemblesToAdd,
    addNewEnsemble = addNewEnsemble,
    addArticleToEnsemble = addArticleToEnsemble,
    onClose = onCloseAddToEnsembleDialog,
    onSearchQueryUpdate = onSearchQueryUpdateAddToEnsembles,
    searchQueryUniqueTitle = searchQueryUniqueTitle,
    newlyAddedEnsembles = newlyAddedEnsembles,
  )
}

@Composable
fun EnsembleLazyChips(
    compactWidth: Boolean,
    selectable: Boolean,
    articleEnsembleTitles: List<String>,
    selectedEnsembles: Set<Int>,
    onLongPressEnsemble: (index: Int) -> Unit,
    onClickEnsemble: (index: Int) -> Unit,
    ensembleListState: LazyListState,
    topPadding: Dp,
) {
  val chipHeight = InputChipDefaults.Height
  val items: LazyListScope.() -> Unit = {
    items(articleEnsembleTitles.size) { i ->
      val inputChipInteractionSource = remember { MutableInteractionSource() }
      Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(chipHeight + 8.dp)
      ) {
        val selected = selectable && selectedEnsembles.contains(i)
        InputChip(
          selected = selected,
          label = { Text(text = articleEnsembleTitles[i], maxLines = 1, overflow = TextOverflow.Ellipsis) },
          leadingIcon = {
            if(selectable) {
              if(selected) Icon(imageVector = NoopIcons.SelectedIndicator, contentDescription = stringResource(com.inasweaterpoorlyknit.core.ui.R.string.selected))
              else Icon(imageVector = NoopIcons.SelectableIndicator, contentDescription = stringResource(com.inasweaterpoorlyknit.core.ui.R.string.selectable))
            } else {
              Icon(imageVector = NoopIcons.ensembles(), contentDescription = stringResource(R.string.hashtag))
            }
          },
          onClick = {},
          interactionSource = inputChipInteractionSource,
          modifier = Modifier.height(InputChipDefaults.Height)
        )
        Box(
          modifier = Modifier
              .matchParentSize()
              .combinedClickable(
                onLongClick = { onLongPressEnsemble(i) },
                onClick = { onClickEnsemble(i) },
                interactionSource = inputChipInteractionSource,
                indication = null,
              )
        )
      }
    }
  }
  if(compactWidth) {
    LazyRow(state = ensembleListState) {
      items()
      item { Spacer(modifier = Modifier.fillParentMaxWidth(thumbnailAndEnsembleHiddenPercent)) }
    }
  } else {
    LazyColumn(
      state = ensembleListState,
      horizontalAlignment = Alignment.End,
      modifier = Modifier.sizeIn(maxWidth = 200.dp)
    ) {
      item { Spacer(modifier = Modifier.height(topPadding)) }
      items()
      item { Spacer(modifier = Modifier.fillParentMaxHeight(thumbnailAndEnsembleHiddenPercent)) }
    }
  }
}

@Composable
fun FloatingActionButtonDetailScreen(
    editMode: ArticleDetailScreenEditMode,
    exportingEnabled: Boolean,
    onClickEdit: () -> Unit,
    onClickMinimizeButtonControl: () -> Unit,
    onClickDelete: () -> Unit,
    onClickExport: () -> Unit,
    onClickRemoveEnsembles: () -> Unit,
    onClickRemoveImages: () -> Unit,
    onClickCancelSelection: () -> Unit,
    onClickAddToEnsemble: () -> Unit,
    onClickAddPhotoFromAlbum: () -> Unit,
    onClickAddPhotoFromCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val expanded = editMode != ArticleDetailScreenEditMode.Disabled
  NoopBottomEndButtonContainer(extraPadding = PaddingValues(bottom = 4.dp, end = 8.dp), modifier = modifier) {
    NoopExpandingIconButton(
      expanded = expanded,
      collapsedIcon = IconData(NoopIcons.Edit, stringResource(R.string.enter_editing_mode)),
      expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
      onClick = if(expanded) onClickMinimizeButtonControl else onClickEdit,
      verticalExpandedButtons = when(editMode) {
        ArticleDetailScreenEditMode.EnabledSelectedEnsembles -> listOf(
          IconButtonData(
            icon = IconData(icon = NoopIcons.Cancel, contentDescription = stringResource(R.string.clear_selected_ensembles)),
            onClick = onClickCancelSelection
          ),
          IconButtonData(
            icon = IconData(NoopIcons.attachmentRemove(), stringResource(R.string.remove_article_from_selected_ensembles)),
            onClick = onClickRemoveEnsembles,
          ),
        )
        ArticleDetailScreenEditMode.EnabledSelectedThumbnails -> {
          listOf(
            IconButtonData(
              icon = IconData(icon = NoopIcons.Cancel, contentDescription = stringResource(R.string.clear_selected_images)),
              onClick = onClickCancelSelection
            ),
            IconButtonData(
              icon = IconData(NoopIcons.DeleteForever, stringResource(R.string.delete_selected_images_from_article)),
              onClick = onClickRemoveImages
            )
          )
        }
        ArticleDetailScreenEditMode.EnabledAllThumbnails -> {
          listOf(
            IconButtonData(
              icon = IconData(icon = NoopIcons.Cancel, contentDescription = stringResource(R.string.clear_selected_images)),
              onClick = onClickCancelSelection
            ),
            IconButtonData(
              icon = IconData(NoopIcons.DeleteForever, stringResource(R.string.delete_article)),
              onClick = onClickDelete
            )
          )
        }
        else -> {
          listOf(
            IconButtonData(
              icon = IconData(NoopIcons.DeleteForever, stringResource(R.string.delete_article)),
              onClick = onClickDelete
            ),
            IconButtonData(
              icon = IconData(NoopIcons.Download, stringResource(R.string.export_article_image)),
              onClick = onClickExport,
              enabled = exportingEnabled,
            ),
            IconButtonData(
              icon = IconData(NoopIcons.AddPhotoAlbum, stringResource(R.string.add_photo_from_photo_album_to_article)),
              onClick = onClickAddPhotoFromAlbum,
            ),
            IconButtonData(
              icon = IconData(NoopIcons.AddPhotoCamera, stringResource(R.string.add_photo_from_camera_to_article)),
              onClick = onClickAddPhotoFromCamera
            ),
            IconButtonData(
              icon = IconData(NoopIcons.Attachment, stringResource(R.string.attach_to_ensembles)),
              onClick = onClickAddToEnsemble
            ),
          )
        }
      },
      horizontalExpandedButtons = listOf(),
    )
  }
}

@Composable
fun DeleteArticleAlertDialog(
    fromDeletingAllImages: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) = NoopSimpleAlertDialog(
    title = stringResource(id = R.string.delete_article),
    text = "${if(fromDeletingAllImages){stringResource(id = R.string.an_article_cannot_exist_without_an_image) + " "}else{""}}${stringResource (id = R.string.deleted_articles_unrecoverable)}",
    headerIcon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
    onDismiss = onDismiss,
    onConfirm = onConfirm,
    confirmText = stringResource(id = R.string.delete),
    cancelText = stringResource(id = R.string.cancel),
  )

@Composable
fun RemoveFromEnsemblesAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
      title = stringResource(id = R.string.remove_ensembles),
      text = stringResource(id = R.string.are_you_sure_remove_article_from_ensembles),
      headerIcon = { Icon(imageVector = NoopIcons.attachmentRemove(), contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.remove),
      cancelText = stringResource(id = R.string.cancel),
    )
@Composable
fun RemoveImagesAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
      title = stringResource(id = R.string.remove_images),
      text = stringResource(id = R.string.deleted_images_unrecoverable),
      headerIcon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.delete),
      cancelText = stringResource(id = R.string.cancel),
    )

@Composable
fun ExportPermissionsAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
      title = stringResource(id = R.string.permission_alert_title),
      text = stringResource(id = R.string.export_permission_alert_justification),
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.permission_alert_positive),
      cancelText = stringResource(id = R.string.permission_alert_negative),
      headerIcon = { Icon(imageVector = NoopIcons.Folder, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
    )

@Composable
private fun AddToEnsembleDialog(
    visible: Boolean,
    ensemblesToAdd: List<Ensemble>,
    addNewEnsemble: (String) -> Unit,
    addArticleToEnsemble: (String) -> Unit,
    onClose: () -> Unit,
    userSearch: String,
    onSearchQueryUpdate: (String) -> Unit,
    newlyAddedEnsembles: Set<String>,
    searchQueryUniqueTitle: Boolean,
) {
  val ensembleChipsHeight = 50.dp
  val chipRowPadding = PaddingValues(5.dp)
  val chipHorizontalPadding = 2.dp
  BackHandler(enabled = visible, onBack = onClose)
  NoopBottomSheetDialog(
    visible = visible,
    title = stringResource(id = R.string.attach_to_ensemble),
    positiveButtonEnabled = false,
    onClose = onClose,
  ) {
    Text(text = stringResource(id = R.string.ensembles))
    if(ensemblesToAdd.isNotEmpty()) {
      LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        contentPadding = chipRowPadding,
        modifier = Modifier.height(ensembleChipsHeight),
      ) {
        items(count = ensemblesToAdd.size) { ensembleIndex ->
          val ensemble = ensemblesToAdd[ensembleIndex]
          Box(contentAlignment = Alignment.Center) {
            AssistChip(
              label = { Text(text = ensemble.title) },
              leadingIcon = { Icon(imageVector = NoopIcons.ensembles(), contentDescription = stringResource(R.string.ensembles)) },
              onClick = { addArticleToEnsemble(ensemble.id) },
              modifier = Modifier.padding(horizontal = chipHorizontalPadding)
            )
          }
        }
      }
    } else {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(ensembleChipsHeight),
      ) {
        Text(
          text = stringResource(R.string.no_ensembles_available),
        )
      }
    }
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
          .fillMaxWidth()
          .height(ensembleChipsHeight),
    ) {
      val newEnsembleChipInteractionSource = remember { MutableInteractionSource() }
      val searchQueryHasRecentlyBeenAdded = newlyAddedEnsembles.contains(userSearch)
      Box {
        InputChip(
          selected = searchQueryUniqueTitle,
          label = {
            Text(
              text = userSearch,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = { Icon(imageVector = NoopIcons.ensembles(), contentDescription = stringResource(R.string.hashtag)) },
          trailingIcon = {
            if(searchQueryHasRecentlyBeenAdded) {
              Icon(imageVector = NoopIcons.Check, contentDescription = stringResource(R.string.ensemble_successfully_added))
            } else if(searchQueryUniqueTitle) {
              Icon(imageVector = NoopIcons.Add, contentDescription = stringResource(R.string.add_ensemble))
            }
          },
          onClick = {},
          interactionSource = newEnsembleChipInteractionSource,
          modifier = Modifier.padding(horizontal = chipRowPadding.calculateStartPadding(LocalLayoutDirection.current) + chipHorizontalPadding)
        )
        Box(
          modifier = Modifier
              .matchParentSize()
              .clickable(
                onClick = { if(searchQueryUniqueTitle) addNewEnsemble(userSearch) },
                interactionSource = if(searchQueryUniqueTitle) newEnsembleChipInteractionSource else null,
                indication = null,
              )
        )
      }
    }
    NoopSearchBox(
      query = userSearch,
      placeholder = stringResource(R.string.search),
      onQueryChange = { onSearchQueryUpdate(it) },
      onClearQuery = { onSearchQueryUpdate("") },
      modifier = Modifier
          .fillMaxWidth()
          .padding(15.dp),
    )
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilArticleDetailScreen(
    filter: String = "",
    darkMode: Boolean = false,
    floatingActionButtonExpanded: ArticleDetailScreenEditMode = ArticleDetailScreenEditMode.Disabled,
    alertDialogMode: ArticleDetailScreenAlertDialogMode = ArticleDetailScreenAlertDialogMode.None,
    showAddToEnsembleDialog: Boolean = false,
    selectedEnsembles: Set<Int> = emptySet(),
    addEnsembles: List<Ensemble> = emptyList(),
) = NoopTheme(darkMode = if(darkMode) DarkMode.DARK else DarkMode.LIGHT) {
  val articlesWithImages = object: LazyFilenames {
    val fullImageUris = allTestFullResourceIdsAsStrings
    val thumbImageUris = allTestThumbnailResourceIdsAsStrings
    override val lazyFullImageUris: LazyUriStrings
      get() = object: LazyUriStrings {
        override val size: Int get() = fullImageUris.size
        override fun getUriStrings(index: Int): List<String> {
          return fullImageUris.toList()
        }
      }
    override val lazyThumbImageUris: LazyUriStrings
      get() = object: LazyUriStrings {
        override val size: Int get() = thumbImageUris.size
        override fun getUriStrings(index: Int): List<String> {
          return thumbImageUris.toList()
        }
      }
    override val size: Int = 1

  }
  Surface {
    ArticleDetailScreen(
      windowSizeClass = currentWindowAdaptiveInfo(),
      filter = filter,
      articleEnsembleTitles = listOf("Road Warrior", "Goth 2 Boss", "John Prine", "Townes Van Zandt", "Deafheaven"),
      pagerState = rememberPagerState(initialPage = 0, pageCount = { articlesWithImages.size }),
      articleImageIndices = listOf(0),
      articlesWithImages = articlesWithImages,
      editMode = floatingActionButtonExpanded,
      alertDialogMode = alertDialogMode,
      showAddToEnsembleDialog = showAddToEnsembleDialog,
      selectedEnsembles = selectedEnsembles,
      ensemblesToAdd = addEnsembles,
      exportingEnabled = true,
      searchQueryUniqueTitle = true,
      selectedImages = setOf(0),
      ensemblesSearchQuery = "Goth 2 Boss",
      newlyAddedEnsembles = emptySet(),
      ensembleListState = rememberLazyListState(), thumbnailsAltsListState = rememberLazyListState(),
      onClickDelete = {}, onClickRemoveEnsembles = {}, onClickCancelSelection = {}, onClickAddToEnsemble = {}, onClickAddPhotoFromAlbum = {}, onClickAddPhotoFromCamera = {},
      onClickRemoveImages = {}, onClickThumbnail = {}, onLongPressThumbnail = {}, onConfirmRemoveFromEnsemblesDialog = {}, onDismissRemoveFromEnsemblesDialog = {}, onDismissExportPermissionsDialog = {},
      onConfirmExportPermissionsDialog = {}, onDismissDeleteDialog = {}, onDismissRemoveImagesDialog = {}, onConfirmRemoveImagesDialog = {}, onLongPressEnsemble = {},
      onClickEdit = {}, addNewEnsemble = {}, onCloseAddToEnsembleDialog = {}, addArticleToEnsemble = {},   onSearchQueryUpdateAddToEnsembles = {},  onConfirmDeleteDialog = {},
      onClickEnsemble = {}, onClickExport = {}, onClickMinimizeButtonControl = {},
    )
  }
}

@PreviewScreenSizes @Composable fun PreviewArticleDetailScreen() = PreviewUtilArticleDetailScreen(filter = "Golden Girls")
@Preview @Composable fun PreviewArticleDetailScreen_expandedFAB() = PreviewUtilArticleDetailScreen(floatingActionButtonExpanded = ArticleDetailScreenEditMode.EnabledGeneral, darkMode = true)
@Preview @Composable fun PreviewArticleDetailScreen_deleteDialog() = PreviewUtilArticleDetailScreen(alertDialogMode = ArticleDetailScreenAlertDialogMode.DeleteArticle, floatingActionButtonExpanded = ArticleDetailScreenEditMode.EnabledGeneral, selectedEnsembles = setOf(1))
@Preview @Composable fun PreviewArticleDetailScreen_permissionsDialog() = PreviewUtilArticleDetailScreen(alertDialogMode = ArticleDetailScreenAlertDialogMode.ExportPermissions)
@Preview @Composable fun PreviewArticleDetailScreen_removeFromEnsemblesDialog() = PreviewUtilArticleDetailScreen(alertDialogMode = ArticleDetailScreenAlertDialogMode.RemoveFromEnsembles)
@Preview @Composable fun PreviewArticleDetailScreen_removeImages() = PreviewUtilArticleDetailScreen(alertDialogMode = ArticleDetailScreenAlertDialogMode.RemoveImages)
@Preview @Composable fun PreviewArticleDetailScreen_addToEnsembleDialog() = PreviewUtilArticleDetailScreen(
  showAddToEnsembleDialog = true,
  addEnsembles = listOf(
    Ensemble(id = "1", title = "Road Warrior"),
    Ensemble(id = "2", title = "Goth 2 Boss"),
    Ensemble(id = "3", title = "Boy Wonder"),
    Ensemble(id = "4", title = "Deely Stan"),
  )
)

@Preview @Composable fun PreviewAddToEnsembleDialog() = NoopTheme(darkMode = DarkMode.DARK) {
  AddToEnsembleDialog(
    visible = true,
    ensemblesToAdd = listOf(
      Ensemble(id = "1", title = "Road Warrior"),
      Ensemble(id = "2", title = "Goth 2 Boss"),
      Ensemble(id = "3", title = "Boy Wonder"),
      Ensemble(id = "4", title = "Deely Stan"),
    ),
    searchQueryUniqueTitle = true,
    userSearch = "Jurassic Bark ggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg",
    onClose = {}, addArticleToEnsemble = {}, onSearchQueryUpdate = {}, addNewEnsemble = {}, newlyAddedEnsembles = emptySet(),
  )
}
//endregion