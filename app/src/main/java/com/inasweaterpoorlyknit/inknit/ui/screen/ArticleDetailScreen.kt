package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import coil.compose.AsyncImage
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticleDetailViewModel

const val ARTICLE_ID_ARG = "articleId"
const val ARTICLE_DETAIL_ROUTE_BASE = "article_detail_route"
const val ARTICLE_DETAIL_ROUTE = "$ARTICLE_DETAIL_ROUTE_BASE?$ARTICLE_ID_ARG={$ARTICLE_ID_ARG}"

fun NavController.navigateToArticleDetail(clothingArticleId: String, navOptions: NavOptions? = null){
  val route = "${ARTICLE_DETAIL_ROUTE_BASE}?${ARTICLE_ID_ARG}=$clothingArticleId"
  navigate(route, navOptions)
}

@Composable
fun ArticleDetailScreen(
  imageUriString: String?,
  modifier: Modifier = Modifier
) {
    AsyncImage(
      model = imageUriString,
      contentDescription = "TODO: image description",
      modifier = modifier.padding(16.dp),
    )
}

@Composable
fun ArticleDetailRoute(
  navController: NavController,
  clothingArticleId: String,
  modifier: Modifier = Modifier,
  articleDetailViewModel: ArticleDetailViewModel = hiltViewModel(), // MainMenuViewModel
){
  val clothingDetail = articleDetailViewModel.getArticleDetails(clothingArticleId).observeAsState(initial = null)
  ArticleDetailScreen(
    imageUriString = clothingDetail.value?.imageUriString,
    modifier = modifier,
  )
}

@Preview
@Composable
fun ArticleDetailScreenPreview(){
  ArticleDetailScreen(
    imageUriString = resourceAsUriString(R.raw.add_article_compose_preview)
  )
}