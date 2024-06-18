package com.inasweaterpoorlyknit.merlinsbag.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.inasweaterpoorlyknit.merlinsbag.ui.NoopAppState
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ADD_ARTICLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLE_DETAIL_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLE_INDEX_ARG
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.AddArticleRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticlesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLE_DETAIL_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLE_ID_ARG
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsembleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsemblesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.IMAGE_URI_STRING_LIST_ARG
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SETTINGS_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SettingsRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigationSafeUriStringDecode

const val APP_START_DESTINATION = ARTICLES_ROUTE

fun NavController.navigateToAppStartDestination() {
  val navOptions = navOptions {
    popUpTo(route = APP_START_DESTINATION) {
      inclusive = true
      saveState = false
    }
    launchSingleTop = true
    restoreState = false
  }
  navigate(APP_START_DESTINATION, navOptions)
}

@Composable
fun NoopNavHost(
    appState: NoopAppState,
    startDestination: String = APP_START_DESTINATION,
) {
  val navController = appState.navController

  NavHost(
    navController = navController,
    startDestination = startDestination,
  ) {
    composable(route = ARTICLES_ROUTE) { ArticlesRoute(navController = navController) }
    composable(route = ENSEMBLES_ROUTE) { EnsemblesRoute(navController = navController) }
    composable(route = SETTINGS_ROUTE) { SettingsRoute(navController = navController, snackbarHostState = appState.snackbarHostState) }
    composable(
      route = ARTICLE_DETAIL_ROUTE,
      arguments = listOf(
        navArgument(ARTICLE_INDEX_ARG) {
          nullable = false
          type = NavType.IntType
        },
        navArgument(ENSEMBLE_ID_ARG) {
          nullable = true
          type = NavType.StringType
        },
      ),
    ) { navBackStackEntry ->
      val articleIndexArg = navBackStackEntry.arguments!!.getInt(ARTICLE_INDEX_ARG)
      val ensembleIdArg = navBackStackEntry.arguments!!.getString(ENSEMBLE_ID_ARG)
      ArticleDetailRoute(
        navController = navController,
        snackbarHostState = appState.snackbarHostState,
        articleIndex = articleIndexArg,
        ensembleId = ensembleIdArg,
      )
    }
    composable(
      route = ENSEMBLE_DETAIL_ROUTE,
      arguments = listOf(
        navArgument(ENSEMBLE_ID_ARG) {
          nullable = false
          type = NavType.StringType
        },
      ),
    ) { navBackStackEntry ->
      val ensembleIdArg = navBackStackEntry.arguments!!.getString(ENSEMBLE_ID_ARG)!!
      EnsembleDetailRoute(navController = navController, ensembleId = ensembleIdArg)
    }
    composable(
      route = ADD_ARTICLES_ROUTE,
      arguments = listOf(
        navArgument(IMAGE_URI_STRING_LIST_ARG) {
          nullable = false
          type = NavType.StringType
        },
      ),
    ) { navBackStackEntry ->
      val imageUriStringListArg = navBackStackEntry.arguments!!.getString(IMAGE_URI_STRING_LIST_ARG)!!
      val imageUriStringList = imageUriStringListArg.split(",").map { navigationSafeUriStringDecode(it) }
      AddArticleRoute(
        navController = navController,
        imageUriStringList = imageUriStringList,
        windowSizeClass = appState.windowSizeClass,
      )
    }
  }
}