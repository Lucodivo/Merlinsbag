package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import coil.compose.AsyncImage
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticleDetailViewModel

@Composable
fun ArticleDetailScreen(
  modifier: Modifier = Modifier,
  imageUriString: String?) {
  AsyncImage(model = imageUriString, contentDescription = "TODO: image description")
}

fun NavController.navigateToArticleDetail(clothingArticleId: String, navOptions: NavOptions? = null){
  val route = "${ARTICLE_DETAIL_ROUTE_BASE}?${ARTICLE_ID_ARG}=$clothingArticleId"
  navigate(route, navOptions)
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
  )
}

const val ARTICLE_ID_ARG = "articleId"
const val ARTICLE_DETAIL_ROUTE_BASE = "article_detail_route"
const val ARTICLE_DETAIL_ROUTE = "$ARTICLE_DETAIL_ROUTE_BASE?$ARTICLE_ID_ARG={$ARTICLE_ID_ARG}"