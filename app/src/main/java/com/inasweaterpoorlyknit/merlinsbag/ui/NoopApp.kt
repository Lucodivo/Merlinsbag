package com.inasweaterpoorlyknit.merlinsbag.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.navigation.NavUIDestinations
import com.inasweaterpoorlyknit.merlinsbag.navigation.NoopNavHost
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ADD_ARTICLES_BASE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.Onboarding
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SETTINGS_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.compactWidth
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToArticles
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToEnsembles
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToSettings

fun NavController.navigateToNavUiDestination(from: NavUIDestinations, to: NavUIDestinations) {
  val topLevelNavOptions = navOptions {
    popUpTo(route = from.route) {
      inclusive = true
    }
    launchSingleTop = true
  }

  when(to) {
    NavUIDestinations.ARTICLES -> navigateToArticles(topLevelNavOptions)
    NavUIDestinations.ENSEMBLES -> navigateToEnsembles(topLevelNavOptions)
  }
}

@Composable
fun NoopApp(
    appState: NoopAppState,
    showOnboarding: Boolean,
) {
  Box{
    var currentDestination by rememberSaveable { mutableStateOf(NavUIDestinations.ARTICLES) }
    val compactWidth = appState.windowSizeClass.compactWidth()
    NavigationSuiteScaffold(
      navigationSuiteItems = {
        if(!compactWidth){
          item(
            icon = { Icon(NoopIcons.Settings, stringResource(R.string.cog)) },
            label = { Text(stringResource(R.string.settings)) },
            selected = false,
            onClick = { appState.navController.navigateToSettings() }
          )
        }
        NavUIDestinations.entries.forEach {
          val selected = it == currentDestination
          item(
            icon = if(selected) it.selectedIcon else it.unselectedIcon,
            label = { Text(stringResource(it.label)) },
            selected = selected,
            onClick = {
              appState.navController.navigateToNavUiDestination(currentDestination, it)
              currentDestination = it
            }
          )
        }
      },
      layoutType =
        if(!appState.showNavBar.value) NavigationSuiteType.None
        else if(compactWidth) NavigationSuiteType.NavigationBar
        else NavigationSuiteType.NavigationRail
      ,
      containerColor = Color.Transparent,
    ){
      NoopScaffold(
        snackbarHostState = appState.snackbarHostState,
        modifier = Modifier,
      ) { padding ->
        Surface(
          modifier = Modifier
              .fillMaxSize()
              .padding(padding),
        ) {
          NoopNavHost(
            appState = appState,
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