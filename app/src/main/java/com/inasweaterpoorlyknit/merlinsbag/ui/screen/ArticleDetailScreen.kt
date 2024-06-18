@file:OptIn(ExperimentalFoundationApi::class)
package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
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
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val articleDetailViewModel =
      hiltViewModel<ArticleDetailViewModel, ArticleDetailViewModel.ArticleDetailViewModelFactory> { factory ->
        factory.create(ensembleId)
      }
  val settingsLauncher = rememberSettingsLauncher()
  val lazyArticleImagesUris by articleDetailViewModel.articleDetailUiState.collectAsStateWithLifecycle()
  var floatingActionButtonExpanded by remember { mutableStateOf(false) }
  var showDeleteArticleAlertDialog by remember { mutableStateOf(false) }
  var showPermissionsAlertDialog by remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(
    initialPage = articleIndex,
    initialPageOffsetFraction = 0.0f,
    pageCount = { lazyArticleImagesUris.size },
  )
  val articleBeingExported = remember { mutableStateMapOf<Int,Unit>() } // TODO: No mutableStateSetOf ??
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
      )){
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
  ArticleDetailScreen(
    articlesWithImages = lazyArticleImagesUris,
    pagerState = pagerState,
    floatingActionButtonExpanded = floatingActionButtonExpanded,
    exportingEnabled = !articleBeingExported.containsKey(pagerState.currentPage),
    showDeleteArticleAlertDialog = showDeleteArticleAlertDialog,
    showPermissionsAlertDialog = showPermissionsAlertDialog,
    onClickEdit = { floatingActionButtonExpanded = !floatingActionButtonExpanded },
    onClickExport = { exportWithPermissionsCheckLauncher.launch(REQUIRED_STORAGE_PERMISSIONS) },
    onClickDelete = { showDeleteArticleAlertDialog = true },
    onDismissDeleteDialog = { showDeleteArticleAlertDialog = false },
    onConfirmDeleteDialog = {
      showDeleteArticleAlertDialog = false
      if(pagerState.pageCount == 1) navController.popBackStack()
      articleDetailViewModel.deleteArticle(pagerState.currentPage)
    },
    onDismissPermissionsDialog = { showPermissionsAlertDialog = false },
    onConfirmPermissionsDialog = {
      showPermissionsAlertDialog = false
      settingsLauncher.launch()
    },
    modifier = modifier,
  )
}

@Composable
fun ArticleDetailScreen(
    articlesWithImages: LazyUriStrings,
    pagerState: PagerState,
    exportingEnabled: Boolean,
    floatingActionButtonExpanded: Boolean,
    showDeleteArticleAlertDialog: Boolean,
    showPermissionsAlertDialog: Boolean,
    onClickEdit: () -> Unit,
    onClickExport: () -> Unit,
    onClickDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteDialog: () -> Unit,
    onDismissPermissionsDialog: () -> Unit,
    onConfirmPermissionsDialog: () -> Unit,
    modifier: Modifier = Modifier,
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
) {
  val layoutDir = LocalLayoutDirection.current
  HorizontalPager(
    state = pagerState
  ) { page ->
    NoopImage(
      uriString = articlesWithImages.getUriString(page),
      contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
      modifier = modifier
          .fillMaxSize()
          .padding(16.dp),
    )
  }
  FloatingActionButtonDetailScreen(
    expanded = floatingActionButtonExpanded,
    exportingEnabled = exportingEnabled,
    onClickEdit = onClickEdit,
    onClickDelete = onClickDelete,
    onClickExport = onClickExport,
    modifier = Modifier.padding(start = systemBarPaddingValues.calculateStartPadding(layoutDir), end = systemBarPaddingValues.calculateEndPadding(layoutDir)),
  )
  if(showDeleteArticleAlertDialog){
    DeleteArticleAlertDialog(onDismiss = onDismissDeleteDialog, onConfirm = onConfirmDeleteDialog)
  }
  if(showPermissionsAlertDialog) {
    ExportPermissionsAlertDialog(onDismiss = onDismissPermissionsDialog, onConfirm = onConfirmPermissionsDialog)
  }
}

@Composable
fun FloatingActionButtonDetailScreen(
    expanded: Boolean,
    exportingEnabled: Boolean,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
  NoopBottomEndButtonContainer(modifier = modifier) {
    NoopExpandingIconButton(
      expanded = expanded,
      collapsedIcon = IconData(NoopIcons.Edit, TODO_ICON_CONTENT_DESCRIPTION),
      expandedIcon = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
      onClick = onClickEdit,
      verticalExpandedButtons = listOf(
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
    floatingActionButtonExpanded: Boolean = false,
    showDeleteArticleAlertDialog: Boolean = false,
    showPermissionsAlertDialog: Boolean = false,
) = NoopTheme {
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
  ArticleDetailScreen(
    articlesWithImages = articlesWithImages,
    pagerState = rememberPagerState(initialPage = 0, pageCount = { articlesWithImages.size }),
    floatingActionButtonExpanded = floatingActionButtonExpanded,
    exportingEnabled = true,
    showDeleteArticleAlertDialog = showDeleteArticleAlertDialog,
    showPermissionsAlertDialog = showPermissionsAlertDialog,
    onClickDelete = {}, onClickExport = {}, onClickEdit = {}, onConfirmDeleteDialog = {},
    onDismissPermissionsDialog = {}, onConfirmPermissionsDialog = {},
    onDismissDeleteDialog = {},
  )
}

@Preview @Composable fun PreviewArticleDetailScreen() = PreviewUtilArticleDetailScreen()
@Preview @Composable fun PreviewArticleDetailScreen_expandedFAB() = PreviewUtilArticleDetailScreen(floatingActionButtonExpanded = true)
@Preview @Composable fun PreviewArticleDetailScreen_deleteDialog() =
    PreviewUtilArticleDetailScreen(floatingActionButtonExpanded = true, showDeleteArticleAlertDialog = true)
@Preview @Composable fun PreviewArticleDetailScreen_permissionsDialog() =
    PreviewUtilArticleDetailScreen(floatingActionButtonExpanded = true, showPermissionsAlertDialog = true)
//endregion