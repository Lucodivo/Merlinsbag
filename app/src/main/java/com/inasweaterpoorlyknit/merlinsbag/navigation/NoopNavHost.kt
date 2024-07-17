package com.inasweaterpoorlyknit.merlinsbag.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.inasweaterpoorlyknit.merlinsbag.ui.NoopAppState
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ADD_ARTICLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLE_DETAIL_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLE_ID_ARG
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLE_INDEX_ARG
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.AddArticleRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticlesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.CAMERA_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.CameraRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLE_DETAIL_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLE_ID_ARG
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsembleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsemblesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.IMAGE_URI_STRING_LIST_ARG
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SettingsRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.StatisticsRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.TipsAndInfoRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigationSafeUriStringDecode

val APP_START_ROUTE = ArticlesRoute

fun NavController.navigateToAppStartDestination() {
  val navOptions = navOptions {
    popUpTo(route = APP_START_ROUTE) {
      inclusive = true
      saveState = false
    }
    launchSingleTop = true
    restoreState = false
  }
  navigate(APP_START_ROUTE, navOptions)
}

@Composable
fun NoopNavHost(
    appState: NoopAppState,
    startRoute: Any = APP_START_ROUTE,
) {
  val navController = appState.navController

  NavHost(
    navController = navController,
    startDestination = startRoute,
  ) {
    composable<ArticlesRoute>{ ArticlesRoute(navController = navController, windowSizeClass = appState.windowSizeClass) }
    composable<EnsemblesRoute>{ EnsemblesRoute(navController = navController, windowSizeClass = appState.windowSizeClass) }
    composable<SettingsRoute>{ SettingsRoute(navController = navController) }
    composable<TipsAndInfoRoute>{ TipsAndInfoRoute() }
    composable<StatisticsRoute>{ StatisticsRoute() }
    composable(
      route = CAMERA_ROUTE,
      arguments = listOf(
        navArgument(ARTICLE_ID_ARG) {
          nullable = true
          defaultValue = null
          type = NavType.StringType
        },
      ),
    ) { navBackStackEntry ->
      val articleIdArg = navBackStackEntry.arguments!!.getString(ARTICLE_ID_ARG)
      CameraRoute(articleId = articleIdArg, navController = navController)
    }
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
        windowSizeClass = appState.windowSizeClass,
        articleIndex = articleIndexArg,
        filterEnsembleId = ensembleIdArg,
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
      EnsembleDetailRoute(navController = navController, ensembleId = ensembleIdArg, windowSizeClass = appState.windowSizeClass)
    }
    composable(
      route = ADD_ARTICLES_ROUTE,
      arguments = listOf(
        navArgument(IMAGE_URI_STRING_LIST_ARG) {
          nullable = false
          type = NavType.StringType
        },
        navArgument(ARTICLE_ID_ARG) {
          nullable = true
          defaultValue = null
          type = NavType.StringType
        },
      ),
    ) { navBackStackEntry ->
      val args = navBackStackEntry.arguments!!
      val imageUriStringListArg = args.getString(IMAGE_URI_STRING_LIST_ARG)!!
      val imageUriStringList = imageUriStringListArg.split(",").map { navigationSafeUriStringDecode(it) }
      val articleIdArg = args.getString(ARTICLE_ID_ARG)
      AddArticleRoute(
        navController = navController,
        imageUriStringList = imageUriStringList,
        articleId = articleIdArg,
        windowSizeClass = appState.windowSizeClass,
      )
    }
  }
}