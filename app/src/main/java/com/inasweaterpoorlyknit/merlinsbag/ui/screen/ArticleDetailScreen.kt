@file:OptIn(ExperimentalFoundationApi::class)
package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.COMPOSE_ID
import com.inasweaterpoorlyknit.merlinsbag.ui.component.IconData
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopExpandingFloatingActionButton
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopImage
import com.inasweaterpoorlyknit.merlinsbag.ui.component.TextButtonData
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticleDetailViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


const val ARTICLE_INDEX_ARG = "articleIndex"
const val ARTICLE_DETAIL_ROUTE_BASE = "article_detail_route"
const val ARTICLE_DETAIL_ROUTE = "$ARTICLE_DETAIL_ROUTE_BASE?$ARTICLE_INDEX_ARG={$ARTICLE_INDEX_ARG}?$ENSEMBLE_ID_ARG={$ENSEMBLE_ID_ARG}"

fun NavController.navigateToArticleDetail(articleIndex: Int, ensembleId: String? = null, navOptions: NavOptions? = null) {
  val route = "${ARTICLE_DETAIL_ROUTE_BASE}?$ARTICLE_INDEX_ARG=$articleIndex?$ENSEMBLE_ID_ARG=$ensembleId"
  navigate(route, navOptions)
}

@Composable
fun ArticleDetailRoute(
    navController: NavController,
    articleIndex: Int,
    ensembleId: String?,
    modifier: Modifier = Modifier,
) {
  val articleDetailViewModel =
      hiltViewModel<ArticleDetailViewModel, ArticleDetailViewModel.ArticleDetailViewModelFactory> { factory ->
        factory.create(ensembleId)
      }
  val articleDetailUiState by articleDetailViewModel.articleDetailUiState.collectAsStateWithLifecycle()
  val floatingActionButtonExpanded = remember { mutableStateOf(false) }
  val showDeleteArticleAlertDialog = remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(
    initialPage = articleIndex,
    initialPageOffsetFraction = 0.0f,
    pageCount = { articleDetailUiState.articleFullImages.size },
  )
  ArticleDetailScreen(
    articlesWithImages = articleDetailUiState.articleFullImages,
    pagerState = pagerState,
    floatingActionButtonExpanded = floatingActionButtonExpanded.value,
    showDeleteArticleAlertDialog = showDeleteArticleAlertDialog.value,
    onClickEdit = { floatingActionButtonExpanded.value = !floatingActionButtonExpanded.value },
    onClickExport = articleDetailViewModel::exportArticle,
    onClickDelete = { showDeleteArticleAlertDialog.value = true },
    onDismissDeleteDialog = { showDeleteArticleAlertDialog.value = false },
    onConfirmDeleteDialog = {
      showDeleteArticleAlertDialog.value = false
      if(pagerState.pageCount == 1) navController.popBackStack()
      articleDetailViewModel.deleteArticle(pagerState.currentPage)
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
    onClickEdit: () -> Unit,
    onClickExport: () -> Unit,
    onClickDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteDialog: () -> Unit,
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
}

@Composable
fun FloatingActionButtonDetailScreen(
    expanded: Boolean,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickExport: () -> Unit
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
      )
    ),
    horizontalExpandedButtons = listOf(
      TextButtonData(
        icon = IconData(NoopIcons.Download, TODO_ICON_CONTENT_DESCRIPTION),
        onClick = onClickExport,
      ),
    ),
  )
}

@Composable
fun DeleteArticleAlertDialog(
    onClickOutside: () -> Unit,
    onClickNegative: () -> Unit,
    onClickPositive: () -> Unit,
) {
  AlertDialog(
    title = { Text(text = stringResource(id = R.string.delete_article)) },
    text = { Text(text = stringResource(id = R.string.deleted_articles_unrecoverable)) },
    icon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
    onDismissRequest = onClickOutside,
    confirmButton = {
      TextButton(onClick = onClickPositive) {
        Text(stringResource(id = R.string.delete_article_alert_positive))
      }
    },
    dismissButton = {
      TextButton(onClick = onClickNegative) {
        Text(stringResource(id = R.string.delete_article_alert_negative))
      }
    }
  )
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilArticleDetailScreen(
    floatingActionButtonExpanded: Boolean = false,
    showDeleteArticleAlertDialog: Boolean = false,
) = NoopTheme {
  val articlesWithImages = LazyArticleThumbnails(
        directory = "",
        articleThumbnailPaths = listOf(
          ArticleWithThumbnails(
            articleId = COMPOSE_ID,
            thumbnailPaths = listOf(
              ThumbnailFilename(
                uri = R.raw.test_full_1.toString(),
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
    onClickDelete = {}, onClickExport = {}, onClickEdit = {}, onConfirmDeleteDialog = {},
    onDismissDeleteDialog = {},
  )
}

@Preview @Composable fun PreviewArticleDetailScreen() = PreviewUtilArticleDetailScreen()
@Preview @Composable fun PreviewArticleDetailScreen_expandedFAB() = PreviewUtilArticleDetailScreen(floatingActionButtonExpanded = true)
@Preview @Composable fun PreviewArticleDetailScreen_deleteDialog() = PreviewUtilArticleDetailScreen(
  floatingActionButtonExpanded = true,
  showDeleteArticleAlertDialog = true,
)
//endregion