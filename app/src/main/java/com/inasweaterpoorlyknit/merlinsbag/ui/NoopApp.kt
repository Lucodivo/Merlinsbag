package com.inasweaterpoorlyknit.merlinsbag.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.inasweaterpoorlyknit.merlinsbag.navigation.APP_START_DESTINATION
import com.inasweaterpoorlyknit.merlinsbag.navigation.NoopNavHost
import com.inasweaterpoorlyknit.merlinsbag.navigation.TopLevelDestination
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ADD_ARTICLES_BASE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.Onboarding
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.SETTINGS_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToArticles
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.navigateToEnsembles
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.MainActivityUiState


@Composable
fun NoopApp(
    appState: NoopAppState,
    modifier: Modifier = Modifier,
    showOnboarding: Boolean,
) {
  val currentTopLevelDestination = remember {
    mutableIntStateOf(
      TopLevelDestination.entries.indexOf(TopLevelDestination.routeToTopLevelDestination(APP_START_DESTINATION))
    )
  }
  val topLevelDestinations = remember { appState.topLevelDestinations }
  Box {
    NoopScaffold(
      showBottomNavBar = appState.showBottomNavBar.value,
      snackbarHostState = appState.snackbarHostState,
      bottomNavBarDataItems = topLevelDestinations,
      selectedNavBarIndex = currentTopLevelDestination.intValue,
      onSelectedNavBarItem = { selectedIndex ->
        val previousIndex = currentTopLevelDestination.intValue
        currentTopLevelDestination.intValue = selectedIndex
        appState.navController.navigateToTopLevelDestination(
          TopLevelDestination.entries[previousIndex],
          TopLevelDestination.entries[selectedIndex]
        )
      },
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
    if(showOnboarding) {
      Onboarding()
    }
  }
}

@Composable
private fun showOnboarding(
    uiState: MainActivityUiState,
): Boolean = when (uiState) {
  MainActivityUiState.Loading -> false
  is MainActivityUiState.Success -> !uiState.userPreferences.hasCompletedOnboarding
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NoopScaffold(
    showBottomNavBar: Boolean,
    snackbarHostState: SnackbarHostState,
    bottomNavBarDataItems: List<BottomNavBarData>,
    selectedNavBarIndex: Int,
    onSelectedNavBarItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) = Scaffold(
  modifier = modifier.semantics {
    testTagsAsResourceId = true
  },
  containerColor = Color.Transparent,
  contentColor = MaterialTheme.colorScheme.onBackground,
  contentWindowInsets = WindowInsets(0, 0, 0, 0),
  bottomBar = {
    if(showBottomNavBar) {
      NoopBottomNavBar(
        bottomNavBarDataItems = bottomNavBarDataItems,
        onClick = onSelectedNavBarItem,
        selectedIndex = selectedNavBarIndex,
        modifier = Modifier.testTag("NoopBottomBar")
      )
    }
  },
  snackbarHost = { SnackbarHost(snackbarHostState) },
  content = content,
)

@Stable
class NoopAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
    val snackbarHostState: SnackbarHostState,
) {
  val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries
  var showBottomNavBar = mutableStateOf(true)

  init {
    navController.addOnDestinationChangedListener { controller, destination, arguments ->
      val route = destination.route
      if(route != null && (
          route.startsWith(ADD_ARTICLES_BASE) ||
          route.startsWith(SETTINGS_ROUTE)
      )){
        showBottomNavBar.value = false
      } else if(!showBottomNavBar.value) {
        showBottomNavBar.value = true
      }
    }
  }
}

@Composable
fun rememberNoopAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    loading: MutableState<Boolean> = remember{ mutableStateOf(true) }
): NoopAppState {
  return remember(navController, windowSizeClass) {
    NoopAppState(
      navController = navController,
      windowSizeClass = windowSizeClass,
      snackbarHostState = snackbarHostState,
    )
  }
}

fun NavController.navigateToTopLevelDestination(from: TopLevelDestination, to: TopLevelDestination) {
  val topLevelNavOptions = navOptions {
    popUpTo(route = TopLevelDestination.topLevelDestinationToRoute(from)) {
      inclusive = true
      saveState = true
    }
    launchSingleTop = true
    restoreState = true
  }

  when(to) {
    TopLevelDestination.ARTICLES -> navigateToArticles(topLevelNavOptions)
    TopLevelDestination.ENSEMBLES -> navigateToEnsembles(topLevelNavOptions)
  }
}

@Composable
fun PreviewUtilNoopScaffold(
    showBottomNavBar: Boolean,
    bottomNavBarSelectedIndex: Int,
) = NoopTheme {
  NoopScaffold(
    snackbarHostState = SnackbarHostState(),
    showBottomNavBar = showBottomNavBar,
    bottomNavBarDataItems = TopLevelDestination.entries,
    selectedNavBarIndex = bottomNavBarSelectedIndex,
    onSelectedNavBarItem = {},
    content = {},
  )
}

@Preview
@Composable
fun PreviewNoopScaffold_Selected0() = PreviewUtilNoopScaffold(
  showBottomNavBar = true,
  bottomNavBarSelectedIndex = 0,
)

@Preview
@Composable
fun PreviewNoopScaffold_Selected1() = PreviewUtilNoopScaffold(
  showBottomNavBar = true,
  bottomNavBarSelectedIndex = 1,
)
