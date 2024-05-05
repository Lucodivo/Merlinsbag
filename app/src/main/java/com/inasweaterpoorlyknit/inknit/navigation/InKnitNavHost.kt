package com.inasweaterpoorlyknit.inknit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.inasweaterpoorlyknit.inknit.ui.InKnitAppState
import com.inasweaterpoorlyknit.inknit.ui.screen.ADD_ARTICLES_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.ARTICLE_DETAIL_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.ARTICLE_ID_ARG
import com.inasweaterpoorlyknit.inknit.ui.screen.AddArticleRoute
import com.inasweaterpoorlyknit.inknit.ui.screen.ArticleDetailRoute
import com.inasweaterpoorlyknit.inknit.ui.screen.ArticlesRoute
import com.inasweaterpoorlyknit.inknit.ui.screen.CAMERA_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.COLLECTIONS_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.CameraRoute
import com.inasweaterpoorlyknit.inknit.ui.screen.CollectionsRoute
import com.inasweaterpoorlyknit.inknit.ui.screen.IMAGE_URI_STRING_ARG
import com.inasweaterpoorlyknit.inknit.viewmodels.Event

data class ScreenSuccess(
  val id: String,
  val success: Boolean,
)

const val APP_START_DESTINATION = ARTICLES_ROUTE

@Composable
fun InKnitNavHost(
  appState: InKnitAppState,
  modifier: Modifier = Modifier,
  startDestination: String = APP_START_DESTINATION,
) {
  val navController = appState.navController
  val (screenSuccess, setScreenSuccess) = remember { mutableStateOf(Event<ScreenSuccess>(null)) }

  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = modifier,
  ){
    composable(route = ARTICLES_ROUTE) {
      ArticlesRoute(navController = navController)
    }
    composable(route = COLLECTIONS_ROUTE) {
      CollectionsRoute(navController = navController)
    }
    composable(
      route = ARTICLE_DETAIL_ROUTE,
      arguments = listOf(
        navArgument(ARTICLE_ID_ARG) {
          nullable = false;
          type = NavType.StringType
        },
      ),
    ) { navBackStackEntry ->
      val articleIdArg = navBackStackEntry.arguments!!.getString(ARTICLE_ID_ARG)!!
      ArticleDetailRoute(navController = navController, clothingArticleId = articleIdArg)
    }
    composable(route = CAMERA_ROUTE){
      CameraRoute(
        navController = navController,
        imageSuccessfullyUsed = screenSuccess.getContentIfNotHandled(),
      )
    }
    composable(
      route = ADD_ARTICLES_ROUTE,
      arguments = listOf(
        navArgument(IMAGE_URI_STRING_ARG) {
          nullable = false;
          type = NavType.StringType
        },
      ),
    ) { navBackStackEntry ->
      val imageUriStringArg = navBackStackEntry.arguments!!.getString(IMAGE_URI_STRING_ARG)!!
      AddArticleRoute(
        navController = navController,
        imageUriString = imageUriStringArg,
        windowSizeClass = appState.windowSizeClass,
        onSuccess = { success -> setScreenSuccess(Event(success)) }
      )
    }
  }
}