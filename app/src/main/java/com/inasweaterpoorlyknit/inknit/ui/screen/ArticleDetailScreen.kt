package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.COMPOSE_ID
import com.inasweaterpoorlyknit.inknit.ui.component.NoopImage
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.viewmodel.ArticleDetailViewModel

const val ARTICLE_INDEX_ARG = "articleIndex"
const val ARTICLE_DETAIL_ROUTE_BASE = "article_detail_route"
const val ARTICLE_DETAIL_ROUTE = "$ARTICLE_DETAIL_ROUTE_BASE?$ARTICLE_INDEX_ARG={$ARTICLE_INDEX_ARG}?$ENSEMBLE_ID_ARG={$ENSEMBLE_ID_ARG}"

fun NavController.navigateToArticleDetail(articleIndex: Int, ensembleId: String? = null, navOptions: NavOptions? = null){
  val route = "${ARTICLE_DETAIL_ROUTE_BASE}?$ARTICLE_INDEX_ARG=$articleIndex?$ENSEMBLE_ID_ARG=$ensembleId"
  navigate(route, navOptions)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleDetailScreen(
  articlesWithImages: List<ArticleWithImages>,
  startingIndex: Int,
  modifier: Modifier = Modifier
) {
  if(articlesWithImages.isEmpty()){
    CircularProgressIndicator()
  } else {
    val pagerState = rememberPagerState(
      initialPage = startingIndex,
      initialPageOffsetFraction = 0.0f,
      pageCount = { articlesWithImages.size },
    )
    HorizontalPager(
      state = pagerState
    ){ page ->
      NoopImage(
        uriString = articlesWithImages[page].images[0].uri,
        contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
        modifier = modifier.fillMaxSize().padding(16.dp),
      )
    }
  }
}

@Composable
fun ArticleDetailRoute(
  navController: NavController,
  articleIndex: Int,
  ensembleId: String?,
  modifier: Modifier = Modifier,
){
  val articleDetailViewModel =
    hiltViewModel<ArticleDetailViewModel, ArticleDetailViewModel.ArticleDetailViewModelFactory> { factory ->
      factory.create(articleIndex, ensembleId)
    }
  val articleDetailUiState by articleDetailViewModel.articleDetailUiState.collectAsStateWithLifecycle()
  ArticleDetailScreen(
    articlesWithImages = articleDetailUiState.articleWithImages,
    startingIndex = articleIndex,
    modifier = modifier,
  )
}

@Preview
@Composable
fun PreviewArticleDetailScreen(){
  val articlesWithImages = listOf(
    ArticleWithImages(
      articleId = COMPOSE_ID,
      images = listOf(
        ArticleImageEntity(
          id = COMPOSE_ID,
          articleId = COMPOSE_ID,
          uri = R.raw.test_full_1.toString(),
          thumbUri = R.raw.test_thumb_1.toString(),
        )
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