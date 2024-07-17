package com.inasweaterpoorlyknit.merlinsbag.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.NoopAppState
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.AddArticleRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticlesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.CameraRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsembleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsemblesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SettingsRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.StatisticsRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.TipsAndInfoRoute

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

  // TODO: Avoid sending nav controller to routes
  NavHost(
    navController = navController,
    startDestination = startRoute,
  ) {
    composable<ArticlesRoute>{ ArticlesRoute(navController = navController, windowSizeClass = appState.windowSizeClass) }
    composable<EnsemblesRoute>{ EnsemblesRoute(navController = navController, windowSizeClass = appState.windowSizeClass) }
    composable<SettingsRoute>{ SettingsRoute(navController = navController) }
    composable<TipsAndInfoRoute>{ TipsAndInfoRoute() }
    composable<StatisticsRoute>{ StatisticsRoute() }
    composable<ArticleDetailRoute>{ navBackStackEntry ->
      val articleDetailRoute: ArticleDetailRoute = navBackStackEntry.toRoute()
      ArticleDetailRoute(
        navController = navController,
        snackbarHostState = appState.snackbarHostState,
        windowSizeClass = appState.windowSizeClass,
        articleIndex = articleDetailRoute.articleIndex,
        filterEnsembleId = articleDetailRoute.ensembleId,
      )
    }
    composable<EnsembleDetailRoute>{ navBackStackEntry ->
      val ensembleDetailRoute: EnsembleDetailRoute = navBackStackEntry.toRoute()
      EnsembleDetailRoute(navController = navController, ensembleId = ensembleDetailRoute.ensembleId, windowSizeClass = appState.windowSizeClass)
    }
    composable<CameraRoute>{ navBackStackEntry ->
      val cameraRoute: CameraRoute = navBackStackEntry.toRoute()
      CameraRoute(articleId = cameraRoute.articleId, navController = navController)
    }
    composable<AddArticleRoute> { navBackStackEntry ->
      val addArticleRoute: AddArticleRoute = navBackStackEntry.toRoute()
      AddArticleRoute(
        navController = navController,
        imageUriStringList = addArticleRoute.imageUriStringList,
        articleId = addArticleRoute.articleId,
        windowSizeClass = appState.windowSizeClass,
      )
    }
  }
}