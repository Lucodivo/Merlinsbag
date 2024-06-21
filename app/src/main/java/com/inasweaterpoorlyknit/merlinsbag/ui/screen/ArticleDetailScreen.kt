@file:OptIn(ExperimentalFoundationApi::class)

package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.COMPOSE_ID
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndButtonContainer
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopImage
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.core.ui.repeatedFullResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticleDetailViewModel

const val ARTICLE_INDEX_ARG = "articleIndex"
const val ARTICLE_DETAIL_ROUTE_BASE = "article_detail_route"
const val ARTICLE_DETAIL_ROUTE = "$ARTICLE_DETAIL_ROUTE_BASE?$ARTICLE_INDEX_ARG={$ARTICLE_INDEX_ARG}?$ENSEMBLE_ID_ARG={$ENSEMBLE_ID_ARG}"

val storagePermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_STORAGE_PERMISSIONS = if(storagePermissionsRequired) arrayOf(permission.WRITE_EXTERNAL_STORAGE) else emptyArray()

fun NavController.navigateToArticleDetail(articleIndex: Int, ensembleId: String? = null, navOptions: NavOptions? = null) {
  val route = "${ARTICLE_DETAIL_ROUTE_BASE}?$ARTICLE_INDEX_ARG=$articleIndex?$ENSEMBLE_ID_ARG=$ensembleId"
  navigate(route, navOptions)
}

@Composable
fun ArticleDetailRoute(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    articleIndex: Int,
    ensembleId: String?,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val articleDetailViewModel =
      hiltViewModel<ArticleDetailViewModel, ArticleDetailViewModel.ArticleDetailViewModelFactory> { factory ->
        factory.create(ensembleId)
      }
  val settingsLauncher = rememberSettingsLauncher()
  val lazyArticleImagesUris by articleDetailViewModel.articleLazyUriStrings.collectAsStateWithLifecycle()
  val articlesEnsembles by articleDetailViewModel.articleEnsembles.collectAsStateWithLifecycle()
  var editMode by remember { mutableStateOf(false) }
  var showDeleteArticleAlertDialog by remember { mutableStateOf(false) }
  var showPermissionsAlertDialog by remember { mutableStateOf(false) }
  var showRemoveFromEnsemblesAlertDialog by remember { mutableStateOf(false) }
  val selectedEnsembles = remember { mutableStateMapOf<Int, Unit>() }
  var ensembleListState by remember { mutableStateOf(LazyListState()) }
  val filter by articleDetailViewModel.filter.collectAsStateWithLifecycle()
  val pagerState = rememberPagerState(
    initialPage = articleIndex,
    initialPageOffsetFraction = 0.0f,
    pageCount = { lazyArticleImagesUris.size },
  )
  val articleBeingExported = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??
  val exportWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = {
      val index = pagerState.currentPage
      articleBeingExported[index] = Unit
      articleDetailViewModel.exportArticle(index)
    },
    onPermissionDenied = { navController.context.toast(R.string.storage_permissions_required) },
    onNeverAskAgain = { showPermissionsAlertDialog = true },
  )
  LaunchedEffect(articleDetailViewModel.articleExported) {
    articleDetailViewModel.articleExported.collect { (index, exportedImageUri) ->
      articleBeingExported.remove(index)
      when(snackbarHostState.showSnackbar(
        message = context.getString(R.string.image_exported),
        actionLabel = context.getString(R.string.open),
        duration = SnackbarDuration.Short,
      )) {
        SnackbarResult.Dismissed -> {}
        SnackbarResult.ActionPerformed -> {
          // TODO: There are other ways to open up an image URI that may need to be explored
          val intent = Intent().apply {
            setAction(Intent.ACTION_VIEW)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(exportedImageUri, "image/webp")
          }
          val pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
          pIntent.send()
        }
      }
    }
  }
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect { page ->
      articleDetailViewModel.onArticleFocus(page)
      ensembleListState = LazyListState()
      selectedEnsembles.clear()
    }
  }
  ArticleDetailScreen(
    windowSizeClass = windowSizeClass,
    filter = filter,
    articlesWithImages = lazyArticleImagesUris,
    articleEnsembleTitles = articlesEnsembles.map { it.title }, // TODO: prevent mapping on every recomposition
    pagerState = pagerState,
    ensembleListState = ensembleListState,
    selectedEnsembles = selectedEnsembles.keys,
    editMode = editMode,
    exportingEnabled = !articleBeingExported.containsKey(pagerState.currentPage),
    showDeleteArticleAlertDialog = showDeleteArticleAlertDialog,
    showPermissionsAlertDialog = showPermissionsAlertDialog,
    showRemoveFromEnsemblesAlertDialog = showRemoveFromEnsemblesAlertDialog,
    onClickEdit = {
      if(editMode) selectedEnsembles.clear()
      editMode = !editMode
    },
    onClickExport = { exportWithPermissionsCheckLauncher.launch(REQUIRED_STORAGE_PERMISSIONS) },
    onClickDelete = { showDeleteArticleAlertDialog = true },
    onDismissDeleteDialog = { showDeleteArticleAlertDialog = false },
    onConfirmDeleteDialog = {
      showDeleteArticleAlertDialog = false
      if(pagerState.pageCount == 1) navController.popBackStack()
      articleDetailViewModel.deleteArticle(pagerState.currentPage)
    },
    onDismissRemoveFromEnsemblesDialog = { showRemoveFromEnsemblesAlertDialog = false },
    onConfirmRemoveFromEnsemblesDialog = {
      articleDetailViewModel.removeArticleEnsembles(pagerState.currentPage, selectedEnsembles.keys.toList())
      selectedEnsembles.clear()
      showRemoveFromEnsemblesAlertDialog = false
      ensembleListState = LazyListState()
    },
    onDismissPermissionsDialog = { showPermissionsAlertDialog = false },
    onConfirmPermissionsDialog = {
      showPermissionsAlertDialog = false
      settingsLauncher.launch()
    },
    onClickEnsemble = {
      if(editMode) {
        if(selectedEnsembles.containsKey(it)) selectedEnsembles.remove(it)
        else selectedEnsembles[it] = Unit
      } else navController.navigateToEnsembleDetail(articlesEnsembles[it].id)
    },
    onLongPressEnsemble = {
      if(!editMode) {
        selectedEnsembles.clear()
        editMode = true
      }
    if(selectedEnsembles.containsKey(it)) selectedEnsembles.remove(it)
      else selectedEnsembles[it] = Unit
    },
    onClickRemoveEnsembles = { showRemoveFromEnsemblesAlertDialog = true },
    onClickCancelEnsemblesSelection = { selectedEnsembles.clear() },
    modifier = modifier,
  )
}

@Composable
fun ArticleDetailScreen(
    windowSizeClass: WindowSizeClass,
    filter: String,
    articleEnsembleTitles: List<String>,
    pagerState: PagerState,
    ensembleListState: LazyListState,
    articlesWithImages: LazyUriStrings,
    editMode: Boolean,
    showDeleteArticleAlertDialog: Boolean,
    showPermissionsAlertDialog: Boolean,
    showRemoveFromEnsemblesAlertDialog: Boolean,
    onClickExport: () -> Unit,
    onClickDelete: () -> Unit,
    onClickRemoveEnsembles: () -> Unit,
    onClickCancelEnsemblesSelection: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteDialog: () -> Unit,
    onConfirmRemoveFromEnsemblesDialog: () -> Unit,
    onDismissRemoveFromEnsemblesDialog: () -> Unit,
    onDismissPermissionsDialog: () -> Unit,
    onConfirmPermissionsDialog: () -> Unit,
    modifier: Modifier = Modifier,
    onClickEnsemble: (index: Int) -> Unit,
    onLongPressEnsemble: (index: Int) -> Unit,
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    exportingEnabled: Boolean,
    onClickEdit: () -> Unit,
    selectedEnsembles: Set<Int>,
) {
  val layoutDir = LocalLayoutDirection.current
  val systemBarTopPadding = systemBarPaddingValues.calculateTopPadding()
  HorizontalPager(
    state = pagerState,
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier.sizeIn(minHeight = ButtonDefaults.MinHeight)
  ) { page ->
    NoopImage(
      uriString = articlesWithImages.getUriString(page),
      contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
      modifier = modifier
          .fillMaxSize()
          .padding(16.dp),
    )
  }
  val compactWidth = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
  val iconModifier = Modifier.padding(start = if(compactWidth) 0.dp else 16.dp, end = 4.dp)
  if(filter.isNotEmpty()) {
    Box(contentAlignment = if(compactWidth) Alignment.TopCenter else Alignment.TopStart,
      modifier = Modifier.fillMaxSize().padding(top = systemBarTopPadding)) {
      Row {
        Icon(NoopIcons.ensembles(), TODO_ICON_CONTENT_DESCRIPTION, modifier = iconModifier)
        Text(text = filter, textAlign = TextAlign.Start, fontSize = MaterialTheme.typography.titleLarge.fontSize)
      }
    }
  }
  Box(
    contentAlignment = if(compactWidth) Alignment.BottomStart else Alignment.TopEnd,
    modifier = Modifier.fillMaxSize()
  ) {
    val items: LazyListScope.() -> Unit = {
      items(articleEnsembleTitles.size) { i ->
        val inputChipInteractionSource = remember { MutableInteractionSource() }
        Box(modifier = Modifier.padding(horizontal = 2.dp)) {
          val selected = selectedEnsembles.contains(i)
          InputChip(
            selected = selected,
            label = { Text(text = articleEnsembleTitles[i]) },
            leadingIcon = { Icon(imageVector = if(selected) NoopIcons.attachmentRemove() else NoopIcons.ensembles(), contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
            onClick = {},
            interactionSource = inputChipInteractionSource,
          )
          Box(modifier = Modifier
              .matchParentSize()
              .combinedClickable (
                onLongClick = { onLongPressEnsemble(i) },
                onClick = { onClickEnsemble(i) },
                interactionSource = inputChipInteractionSource,
                indication = null,
              ))
        }
      }
    }
    if(compactWidth) {
      LazyRow(state = ensembleListState) {
        items()
        item { Spacer(modifier = Modifier.fillParentMaxWidth(0.90f)) }
      }
    } else {
      LazyColumn(state = ensembleListState, horizontalAlignment = Alignment.End) {
        item { Spacer(modifier = Modifier.height(systemBarTopPadding)) }
        items()
        item { Spacer(modifier = Modifier.fillParentMaxHeight(0.90f)) }
      }
    }
  }
  FloatingActionButtonDetailScreen(
    expanded = editMode,
    exportingEnabled = exportingEnabled,
    removeEnsemblesEnabled = selectedEnsembles.isNotEmpty(),
    onClickEdit = onClickEdit,
    onClickDelete = onClickDelete,
    onClickExport = onClickExport,
    onClickRemoveEnsembles = onClickRemoveEnsembles,
    onClickCancelEnsemblesSelection = onClickCancelEnsemblesSelection,
    modifier = Modifier.padding(start = systemBarPaddingValues.calculateStartPadding(layoutDir), end = systemBarPaddingValues.calculateEndPadding(layoutDir)),
  )
  if(showDeleteArticleAlertDialog) DeleteArticleAlertDialog(onDismiss = onDismissDeleteDialog, onConfirm = onConfirmDeleteDialog)
  if(showPermissionsAlertDialog) ExportPermissionsAlertDialog(onDismiss = onDismissPermissionsDialog, onConfirm = onConfirmPermissionsDialog)
  if(showRemoveFromEnsemblesAlertDialog) RemoveFromEnsemblesAlertDialog(onDismiss = onDismissRemoveFromEnsemblesDialog, onConfirm = onConfirmRemoveFromEnsemblesDialog)
}

@Composable
fun FloatingActionButtonDetailScreen(
    expanded: Boolean,
    exportingEnabled: Boolean,
    removeEnsemblesEnabled: Boolean,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickExport: () -> Unit,
    onClickRemoveEnsembles: () -> Unit,
    onClickCancelEnsemblesSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
  NoopBottomEndButtonContainer(modifier = modifier) {
    NoopExpandingIconButton(
      expanded = expanded,
      collapsedIcon = IconData(NoopIcons.Edit, TODO_ICON_CONTENT_DESCRIPTION),
      expandedIcon = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
      onClick = onClickEdit,
      verticalExpandedButtons = if(removeEnsemblesEnabled) listOf(
        IconButtonData(
          icon = IconData(icon = NoopIcons.Cancel, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
          onClick = onClickCancelEnsemblesSelection
        ),
        IconButtonData(
          icon = IconData(NoopIcons.attachmentRemove(), TODO_ICON_CONTENT_DESCRIPTION),
          onClick = onClickRemoveEnsembles,
        ),
      ) else listOf(
        IconButtonData(
          icon = IconData(NoopIcons.DeleteForever, TODO_ICON_CONTENT_DESCRIPTION),
          onClick = { onClickDelete() }
        ),
        IconButtonData(
          icon = IconData(NoopIcons.Download, TODO_ICON_CONTENT_DESCRIPTION),
          onClick = onClickExport,
          enabled = exportingEnabled,
        ),
      ),
      horizontalExpandedButtons = listOf(),
    )
  }
}

@Composable
fun DeleteArticleAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
      title = stringResource(id = R.string.delete_article),
      text = stringResource(id = R.string.deleted_articles_unrecoverable),
      headerIcon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.delete),
      cancelText = stringResource(id = R.string.cancel),
    )

@Composable
fun RemoveFromEnsemblesAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
      title = stringResource(id = R.string.remove_ensembles),
      text = stringResource(id = R.string.are_you_sure_remove_article_from_ensembles),
      headerIcon = { Icon(imageVector = NoopIcons.attachmentRemove(), contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.remove),
      cancelText = stringResource(id = R.string.cancel),
    )

@Composable
fun ExportPermissionsAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
      title = stringResource(id = R.string.permission_alert_title),
      text = stringResource(id = R.string.export_permission_alert_justification),
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.permission_alert_positive),
      cancelText = stringResource(id = R.string.permission_alert_negative),
      headerIcon = { Icon(imageVector = NoopIcons.Folder, contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
    )

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilArticleDetailScreen(
    filter: String = "",
    darkMode: Boolean = false,
    floatingActionButtonExpanded: Boolean = false,
    showDeleteArticleAlertDialog: Boolean = false,
    showPermissionsAlertDialog: Boolean = false,
    showRemoveFromEnsemblesAlertDialog: Boolean = false,
) = NoopTheme(darkMode = if(darkMode) DarkMode.DARK else DarkMode.LIGHT) {
  val articlesWithImages = LazyArticleThumbnails(
    directory = "",
    articleThumbnailPaths = listOf(
      ArticleWithThumbnails(
        articleId = COMPOSE_ID,
        thumbnailPaths = listOf(
          ThumbnailFilename(
            filenameThumb = repeatedFullResourceIdsAsStrings[0],
          ),
        ),
      )
    )
  )
  Surface {
    ArticleDetailScreen(
      windowSizeClass = currentWindowAdaptiveInfo(),
      filter = filter,
      articleEnsembleTitles = listOf("Road Warrior", "Goth 2 Boss", "John Prine", "Townes Van Zandt", "Deafheaven"),
      pagerState = rememberPagerState(initialPage = 0, pageCount = { articlesWithImages.size }),
      ensembleListState = rememberLazyListState(),
      articlesWithImages = articlesWithImages,
      editMode = floatingActionButtonExpanded,
      showDeleteArticleAlertDialog = showDeleteArticleAlertDialog,
      showPermissionsAlertDialog = showPermissionsAlertDialog,
      showRemoveFromEnsemblesAlertDialog = showRemoveFromEnsemblesAlertDialog,
      onClickExport = {}, onClickDelete = {}, onClickRemoveEnsembles = {}, onClickCancelEnsemblesSelection = {},
      onDismissDeleteDialog = {}, onConfirmDeleteDialog = {}, onConfirmRemoveFromEnsemblesDialog = {}, onDismissRemoveFromEnsemblesDialog = {}, onLongPressEnsemble = {},
      onDismissPermissionsDialog = {}, onConfirmPermissionsDialog = {}, onClickEnsemble = {}, exportingEnabled = true, onClickEdit = {}, selectedEnsembles = setOf(1),
    )
  }
}

@PreviewScreenSizes @Composable fun PreviewArticleDetailScreen() = PreviewUtilArticleDetailScreen(filter = "Golden Girls")
@Preview @Composable fun PreviewArticleDetailScreen_expandedFAB() = PreviewUtilArticleDetailScreen(floatingActionButtonExpanded = true, darkMode = true)
@Preview @Composable fun PreviewArticleDetailScreen_deleteDialog() = PreviewUtilArticleDetailScreen(showDeleteArticleAlertDialog = true)
@Preview @Composable fun PreviewArticleDetailScreen_permissionsDialog() = PreviewUtilArticleDetailScreen(showPermissionsAlertDialog = true)
@Preview @Composable fun PreviewArticleDetailScreen_removeFromEnsemblesDialog() = PreviewUtilArticleDetailScreen(showRemoveFromEnsemblesAlertDialog = true)
//endregion