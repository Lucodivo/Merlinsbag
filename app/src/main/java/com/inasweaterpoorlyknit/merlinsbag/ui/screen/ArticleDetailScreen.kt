@file:OptIn(ExperimentalFoundationApi::class)
package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.COMPOSE_ID
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingFloatingActionButton
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopImage
import com.inasweaterpoorlyknit.core.ui.component.TextButtonData
import com.inasweaterpoorlyknit.core.ui.repeatedFullResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.rememberLauncherForActivityResultPermissions
import com.inasweaterpoorlyknit.merlinsbag.ui.rememberSettingsLauncher
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.ui.toast
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
  val articleDetailUiState by articleDetailViewModel.articleDetailUiState.collectAsStateWithLifecycle()
  val floatingActionButtonExpanded = remember { mutableStateOf(false) }
  val showDeleteArticleAlertDialog = remember { mutableStateOf(false) }
  val showPermissionsAlertDialog = remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(
    initialPage = articleIndex,
    initialPageOffsetFraction = 0.0f,
    pageCount = { articleDetailUiState.articleFullImages.size },
  )
  val exportWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = { articleDetailViewModel.exportArticle(pagerState.currentPage) },
    onPermissionDenied = { navController.context.toast(R.string.storage_permissions_required) },
    onNeverAskAgain = { showPermissionsAlertDialog.value = true },
  )
  LaunchedEffect(articleDetailViewModel.exportedImageUri) {
    articleDetailViewModel.exportedImageUri.collect { exportedImageUri ->
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
    articlesWithImages = articleDetailUiState.articleFullImages,
    pagerState = pagerState,
    floatingActionButtonExpanded = floatingActionButtonExpanded.value,
    showDeleteArticleAlertDialog = showDeleteArticleAlertDialog.value,
    showPermissionsAlertDialog = showPermissionsAlertDialog.value,
    onClickEdit = { floatingActionButtonExpanded.value = !floatingActionButtonExpanded.value },
    onClickExport = { exportWithPermissionsCheckLauncher.launch(REQUIRED_STORAGE_PERMISSIONS) },
    onClickDelete = { showDeleteArticleAlertDialog.value = true },
    onDismissDeleteDialog = { showDeleteArticleAlertDialog.value = false },
    onConfirmDeleteDialog = {
      showDeleteArticleAlertDialog.value = false
      if(pagerState.pageCount == 1) navController.popBackStack()
      articleDetailViewModel.deleteArticle(pagerState.currentPage)
    },
    onDismissPermissionsDialog = { showPermissionsAlertDialog.value = false },
    onConfirmPermissionsDialog = {
      showPermissionsAlertDialog.value = false
      settingsLauncher.launch()
    },
    modifier = modifier,
  )
}

@Composable
fun ArticleDetailScreen(
    articlesWithImages: LazyUriStrings,
    pagerState: PagerState,
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
) {
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
    onClickEdit = onClickEdit,
    onClickDelete = onClickDelete,
    onClickExport = onClickExport,
  )
  if(showDeleteArticleAlertDialog){
    DeleteArticleAlertDialog(
      onClickOutside = onDismissDeleteDialog,
      onClickNegative = onDismissDeleteDialog,
      onClickPositive = onConfirmDeleteDialog,
    )
  }
  if(showPermissionsAlertDialog) {
    ExportPermissionsAlertDialog(
      onClickOutside = onDismissPermissionsDialog,
      onClickNegative = onDismissPermissionsDialog,
      onClickPositive = onConfirmPermissionsDialog,
    )
  }
}

@Composable
fun FloatingActionButtonDetailScreen(
    expanded: Boolean,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickExport: () -> Unit,
) {
  NoopExpandingFloatingActionButton(
    expanded = expanded,
    collapsedIcon = IconData(NoopIcons.Edit, TODO_ICON_CONTENT_DESCRIPTION),
    expandedIcon = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
    onClick = onClickEdit,
    verticalExpandedButtons = listOf(
      TextButtonData(
        icon = IconData(NoopIcons.DeleteForever, TODO_ICON_CONTENT_DESCRIPTION),
        onClick = { onClickDelete() }
      ),
      TextButtonData(
        icon = IconData(NoopIcons.Download, TODO_ICON_CONTENT_DESCRIPTION),
        onClick = onClickExport,
      ),
    ),
    horizontalExpandedButtons = listOf(),
  )
}

@Composable
fun DeleteArticleAlertDialog(onClickOutside: () -> Unit, onClickNegative: () -> Unit, onClickPositive: () -> Unit) {
  AlertDialog(
    title = { Text(text = stringResource(id = R.string.delete_article)) },
    text = { Text(text = stringResource(id = R.string.deleted_articles_unrecoverable)) },
    icon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
    onDismissRequest = onClickOutside,
    confirmButton = { TextButton(onClick = onClickPositive) { Text(stringResource(id = R.string.delete_article_alert_positive)) } },
    dismissButton = { TextButton(onClick = onClickNegative) { Text(stringResource(id = R.string.delete_article_alert_negative)) } }
  )
}

@Composable
fun ExportPermissionsAlertDialog(onClickOutside: () -> Unit, onClickNegative: () -> Unit, onClickPositive: () -> Unit) {
  AlertDialog(
    title = { Text(text = stringResource(id = R.string.permission_alert_title)) },
    text = { Text(text = stringResource(id = R.string.export_permission_alert_justification)) },
    onDismissRequest = onClickOutside,
    confirmButton = { TextButton(onClick = onClickPositive) { Text(stringResource(id = R.string.permission_alert_positive)) } },
    dismissButton = { TextButton(onClick = onClickNegative) { Text(stringResource(id = R.string.permission_alert_negative)) } }
  )
}

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