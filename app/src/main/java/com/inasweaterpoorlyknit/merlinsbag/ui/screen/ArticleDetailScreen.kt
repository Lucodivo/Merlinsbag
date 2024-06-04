package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.COMPOSE_ID
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopImage
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticleDetailViewModel

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
        factory.create(articleIndex, ensembleId)
      }
  val articleDetailUiState by articleDetailViewModel.articleDetailUiState.collectAsStateWithLifecycle()
  ArticleDetailScreen(
    articlesWithImages = articleDetailUiState.articleFullImages,
    startingIndex = articleIndex,
    modifier = modifier,
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleDetailScreen(
    articlesWithImages: LazyUriStrings,
    startingIndex: Int,
    modifier: Modifier = Modifier,
) {
  if(articlesWithImages.isEmpty()) {
    CircularProgressIndicator()
  } else {
    val pagerState = rememberPagerState(
      initialPage = startingIndex,
      initialPageOffsetFraction = 0.0f,
      pageCount = { articlesWithImages.size },
    )
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
  }
}

@Preview
@Composable
fun PreviewArticleDetailScreen() {
  val articlesWithImages =
      LazyArticleThumbnails(
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
  NoopTheme {
    ArticleDetailScreen(
      articlesWithImages = articlesWithImages,
      startingIndex = 0,
    )
  }
}