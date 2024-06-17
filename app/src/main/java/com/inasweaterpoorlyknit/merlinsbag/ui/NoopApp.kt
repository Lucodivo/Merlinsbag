package com.inasweaterpoorlyknit.merlinsbag.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.navigation.NoopNavHost
import com.inasweaterpoorlyknit.merlinsbag.navigation.NavUIDestinations
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ADD_ARTICLES_BASE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.Onboarding
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SETTINGS_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToArticles
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToEnsembles

fun NavController.navigateToTopLevelDestination(from: NavUIDestinations, to: NavUIDestinations) {
  val topLevelNavOptions = navOptions {
    popUpTo(route = from.route) {
      inclusive = true
      saveState = true
    }
    launchSingleTop = true
    restoreState = true
  }

  when(to) {
    NavUIDestinations.ARTICLES -> navigateToArticles(topLevelNavOptions)
    NavUIDestinations.ENSEMBLES -> navigateToEnsembles(topLevelNavOptions)
  }
}

@Composable
fun NoopApp(
    appState: NoopAppState,
    modifier: Modifier = Modifier,
    showOnboarding: Boolean,
) {
  Box{
    var currentDestination by rememberSaveable { mutableStateOf(NavUIDestinations.ARTICLES) }
    NavigationSuiteScaffold(
      navigationSuiteItems = {
        NavUIDestinations.entries.forEach {
          val selected = it == currentDestination
          item(
            icon = if(selected) it.selectedIcon else it.unselectedIcon,
            label = { Text(stringResource(it.label)) },
            selected = selected,
            onClick = {
              appState.navController.navigateToTopLevelDestination(currentDestination, it)
              currentDestination = it
            }
          )
        }
      },
      layoutType =
        if(!appState.showNavBar.value) NavigationSuiteType.None
        else if(appState.windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) NavigationSuiteType.NavigationRail
        else NavigationSuiteType.NavigationBar
      ,
      containerColor = Color.Transparent,
    ){
      NoopScaffold(
        snackbarHostState = appState.snackbarHostState,
        modifier = modifier,
      ) { padding ->
        Surface(
          modifier = modifier
              .fillMaxSize()
              .padding(padding),
        ) {
          NoopNavHost(
            appState = appState,
            modifier = modifier,
          )
        }
      }
    }
    if(showOnboarding) Onboarding()
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NoopScaffold(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) = Scaffold(
  modifier = modifier.semantics {
    testTagsAsResourceId = true
  },
  containerColor = Color.Transparent,
  contentColor = MaterialTheme.colorScheme.onBackground,
  contentWindowInsets = WindowInsets(0, 0, 0, 0),
  snackbarHost = { SnackbarHost(snackbarHostState) },
  content = content,
)

@Stable
class NoopAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
    val snackbarHostState: SnackbarHostState,
) {
  var showNavBar = mutableStateOf(true)

  init {
    navController.addOnDestinationChangedListener { controller, destination, arguments ->
      val route = destination.route
      if(route != null && (
          route.startsWith(ADD_ARTICLES_BASE) ||
          route.startsWith(SETTINGS_ROUTE)
      )){
        showNavBar.value = false
      } else if(!showNavBar.value) {
        showNavBar.value = true
      }
    }
  }
}

@Composable
fun rememberNoopAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
): NoopAppState {
  return remember(navController, windowSizeClass) {
    NoopAppState(
      navController = navController,
      windowSizeClass = windowSizeClass,
      snackbarHostState = snackbarHostState,
    )
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilNoopScaffold() = NoopTheme {
  NoopScaffold(
    snackbarHostState = SnackbarHostState(),
    content = {},
  )
}

@Preview
@Composable
fun PreviewNoopScaffold_Selected0() = PreviewUtilNoopScaffold()

@Preview
@Composable
fun PreviewNoopScaffold_Selected1() = PreviewUtilNoopScaffold()
//endregion