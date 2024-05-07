package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.component.ArticleThumbnailImage
import com.inasweaterpoorlyknit.inknit.ui.theme.AppTheme
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
    ArticleThumbnailImage(
      uriString = imageUriString,
      contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
      modifier = modifier.fillMaxSize().padding(16.dp),
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
fun PreviewArticleDetailScreen(){
  AppTheme {
    ArticleDetailScreen(
      imageUriString = R.raw.test_full_1.toString()
    )
  }
}