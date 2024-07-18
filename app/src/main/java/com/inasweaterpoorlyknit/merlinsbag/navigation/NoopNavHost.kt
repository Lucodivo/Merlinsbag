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
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToAddArticle
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToArticleDetail
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToCamera
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToEnsembleDetail
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToSettings
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToStatistics
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToTipsAndInfo

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
    composable<ArticlesRoute>{
      ArticlesRoute(
        navigateToArticleDetail = navController::navigateToArticleDetail,
        navigateToCamera = navController::navigateToCamera,
        navigateToSettings = navController::navigateToSettings,
        navigateToAddArticle = navController::navigateToAddArticle,
        windowSizeClass = appState.windowSizeClass,
      )
    }
    composable<EnsemblesRoute>{
      EnsemblesRoute(
        navigateToSettings = navController::navigateToSettings,
        navigateToEnsembleDetail = navController::navigateToEnsembleDetail,
        windowSizeClass = appState.windowSizeClass
      )
    }
    composable<CameraRoute>{ navBackStackEntry ->
      val cameraRoute: CameraRoute = navBackStackEntry.toRoute()
      CameraRoute(
        articleId = cameraRoute.articleId,
        navigateToAddArticle = navController::navigateToAddArticle,
        navigateBack = navController::popBackStack,
      )
    }
    composable<AddArticleRoute> { navBackStackEntry ->
      val addArticleRoute: AddArticleRoute = navBackStackEntry.toRoute()
      AddArticleRoute(
        imageUriStringList = addArticleRoute.imageUriStringList,
        articleId = addArticleRoute.articleId,
        navigateBack = navController::popBackStack,
        windowSizeClass = appState.windowSizeClass,
      )
    }
    composable<ArticleDetailRoute>{ navBackStackEntry ->
      val articleDetailRoute: ArticleDetailRoute = navBackStackEntry.toRoute()
      ArticleDetailRoute(
        articleIndex = articleDetailRoute.articleIndex,
        navigateBack = navController::popBackStack,
        navigateToCamera = navController::navigateToCamera,
        navigateToEnsembleDetail = navController::navigateToEnsembleDetail,
        navigateToAddArticle = navController::navigateToAddArticle,
        filterEnsembleId = articleDetailRoute.ensembleId,
        snackbarHostState = appState.snackbarHostState,
        windowSizeClass = appState.windowSizeClass,
      )
    }
    composable<EnsembleDetailRoute>{ navBackStackEntry ->
      val ensembleDetailRoute: EnsembleDetailRoute = navBackStackEntry.toRoute()
      EnsembleDetailRoute(
        ensembleId = ensembleDetailRoute.ensembleId,
        navigateToArticleDetail = navController::navigateToArticleDetail,
        navigateBack = navController::popBackStack,
        windowSizeClass = appState.windowSizeClass
      )
    }
    composable<SettingsRoute>{
      SettingsRoute(
        navigateToStatistics = navController::navigateToStatistics,
        navigateToTipsAndInfo = navController::navigateToTipsAndInfo,
        navigateToStartDestination = navController::navigateToAppStartDestination,
      )
    }
    composable<TipsAndInfoRoute>{ TipsAndInfoRoute() }
    composable<StatisticsRoute>{ StatisticsRoute() }
  }
}