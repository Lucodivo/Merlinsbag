package com.inasweaterpoorlyknit.merlinsbag.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.NoopAppState
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.AddArticleRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.AddArticleRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticleDetailRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticlesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticlesRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.CameraRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.CameraRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsembleDetailRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsembleDetailRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsemblesRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsemblesRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SettingsRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SettingsRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.StatisticsRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.StatisticsRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.TipsAndInfoRoute
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.TipsAndInfoRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToAddArticle
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToArticleDetail
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToCamera
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToEnsembleDetail
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToSettings
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToStatistics
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToTipsAndInfo

val APP_START_ROUTE = ArticlesRouteArgs

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
  fun hideNavBar(hide: Boolean){ if(hide) appState.hideNavBar() else appState.showNavBar() }

  NavHost(
    navController = navController,
    startDestination = startRoute,
  ) {
    composable<ArticlesRouteArgs>{
      LaunchedEffect(Unit) { hideNavBar(false) }
      ArticlesRoute(
        navigateToArticleDetail = navController::navigateToArticleDetail,
        navigateToCamera = navController::navigateToCamera,
        navigateToSettings = navController::navigateToSettings,
        navigateToAddArticle = navController::navigateToAddArticle,
        windowSizeClass = appState.windowSizeClass,
      )
    }
    composable<EnsemblesRouteArgs>{
      LaunchedEffect(Unit) { hideNavBar(false) }
      EnsemblesRoute(
        navigateToSettings = navController::navigateToSettings,
        navigateToEnsembleDetail = navController::navigateToEnsembleDetail,
        windowSizeClass = appState.windowSizeClass
      )
    }
    composable<CameraRouteArgs>{ navBackStackEntry ->
      val cameraRouteArgs: CameraRouteArgs = navBackStackEntry.toRoute()
      LaunchedEffect(Unit) { hideNavBar(true) }
      CameraRoute(
        articleId = cameraRouteArgs.articleId,
        navigateToAddArticle = navController::navigateToAddArticle,
        navigateBack = navController::popBackStack,
      )
    }
    composable<AddArticleRouteArgs> { navBackStackEntry ->
      val addArticleRouteArgs: AddArticleRouteArgs = navBackStackEntry.toRoute()
      LaunchedEffect(Unit) { hideNavBar(true) }
      AddArticleRoute(
        imageUriStringList = addArticleRouteArgs.imageUriStringList,
        articleId = addArticleRouteArgs.articleId,
        navigateBack = navController::popBackStack,
        windowSizeClass = appState.windowSizeClass,
      )
    }
    composable<ArticleDetailRouteArgs>{ navBackStackEntry ->
      val articleDetailRouteArgs: ArticleDetailRouteArgs = navBackStackEntry.toRoute()
      LaunchedEffect(Unit) { hideNavBar(false) }
      ArticleDetailRoute(
        articleIndex = articleDetailRouteArgs.articleIndex,
        navigateBack = navController::popBackStack,
        navigateToCamera = navController::navigateToCamera,
        navigateToEnsembleDetail = navController::navigateToEnsembleDetail,
        navigateToAddArticle = navController::navigateToAddArticle,
        filterEnsembleId = articleDetailRouteArgs.ensembleId,
        snackbarHostState = appState.snackbarHostState,
        windowSizeClass = appState.windowSizeClass,
      )
    }
    composable<EnsembleDetailRouteArgs>{ navBackStackEntry ->
      val ensembleDetailRouteArgs: EnsembleDetailRouteArgs = navBackStackEntry.toRoute()
      LaunchedEffect(Unit) { hideNavBar(false) }
      EnsembleDetailRoute(
        ensembleId = ensembleDetailRouteArgs.ensembleId,
        navigateToArticleDetail = navController::navigateToArticleDetail,
        navigateBack = navController::popBackStack,
        windowSizeClass = appState.windowSizeClass
      )
    }
    composable<SettingsRouteArgs>{
      LaunchedEffect(Unit) { hideNavBar(true) }
      SettingsRoute(
        navigateToStatistics = navController::navigateToStatistics,
        navigateToTipsAndInfo = navController::navigateToTipsAndInfo,
        navigateToStartDestination = navController::navigateToAppStartDestination,
      )
    }
    composable<TipsAndInfoRouteArgs>{
      LaunchedEffect(Unit) { hideNavBar(true) }
      TipsAndInfoRoute()
    }
    composable<StatisticsRouteArgs>{
      LaunchedEffect(Unit) { hideNavBar(true) }
      StatisticsRoute()
    }
  }
}