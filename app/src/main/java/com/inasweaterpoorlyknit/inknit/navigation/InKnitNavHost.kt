package com.inasweaterpoorlyknit.inknit.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.tracing.trace
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.CameraScreen
import com.inasweaterpoorlyknit.inknit.ui.ArticlesScreen
import com.inasweaterpoorlyknit.inknit.ui.icons.InKnitIcons
import com.inasweaterpoorlyknit.inknit.viewmodels.ArticlesViewModel
import kotlinx.coroutines.CoroutineScope

const val ARTICLES_ROUTE = "articles_route"
const val CAMERA_ROUTE = "camera_route"
const val ADD_ARTICLES_ROUTE = "add_articles_route"
const val ARTICLE_DETAIL_ROUTE = "article_detail_route"
const val OUTFITS_ROUTE = "outfits_route"

fun NavController.navigateToArticles(navOptions: NavOptions? = null) = navigate(ARTICLES_ROUTE, navOptions)
fun NavController.navigateToCamera(navOptions: NavOptions? = null) = navigate(CAMERA_ROUTE, navOptions)
fun NavController.navigateToAddArticle(navOptions: NavOptions? = null) = navigate(ADD_ARTICLES_ROUTE, navOptions)
fun NavController.navigateToArticleDetail(navOptions: NavOptions? = null) = navigate(ARTICLE_DETAIL_ROUTE, navOptions)
fun NavController.navigateToOutfits(navOptions: NavOptions? = null) = navigate(OUTFITS_ROUTE, navOptions)

@Composable
fun ArticlesRoute(
  modifier: Modifier = Modifier,
  articlesViewModel: ArticlesViewModel = hiltViewModel(), // MainMenuViewModel
){
  val thumbnailDetails = articlesViewModel.thumbnailDetails.observeAsState()
  ArticlesScreen(
    thumbnailUris = thumbnailDetails.value?.map { it.thumbnailUri } ?: emptyList(),
  )
}

@Composable
fun CameraRoute(
  modifier: Modifier = Modifier,
  articlesViewModel: ArticlesViewModel = hiltViewModel(), // MainMenuViewModel
){
  CameraScreen() // MainMenuScreen
}

enum class TopLevelDestination(
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val iconTextId: Int,
) {
  COLLECTIONS(
    selectedIcon = InKnitIcons.Collections,
    unselectedIcon = InKnitIcons.CollectionsBorder,
    iconTextId = R.string.collections,
  ),
  ARTICLES(
    selectedIcon = InKnitIcons.Articles,
    unselectedIcon = InKnitIcons.ArticlesBorder,
    iconTextId = R.string.articles,
  ),
}

@Composable
fun rememberInKnitAppState(
  windowSizeClass: WindowSizeClass,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  navController: NavHostController = rememberNavController(),
): InKnitAppState {
  return remember(
    navController,
    coroutineScope,
    windowSizeClass,
  ) {
    InKnitAppState(
      navController = navController,
      coroutineScope = coroutineScope,
      windowSizeClass = windowSizeClass,
    )
  }
}

@Stable
class InKnitAppState(
  val navController: NavHostController,
  coroutineScope: CoroutineScope,
  val windowSizeClass: WindowSizeClass,
){
  val currentDestination: NavDestination?
    @Composable get() = navController
      .currentBackStackEntryAsState().value?.destination

  /**
   * UI logic for navigating to a top level destination in the app. Top level destinations have
   * only one copy of the destination of the back stack, and save and restore state whenever you
   * navigate to and from it.
   *
   * @param topLevelDestination: The destination the app needs to navigate to.
   */
  fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
    trace("Navigation: ${topLevelDestination.name}") {
      val topLevelNavOptions = navOptions {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
          saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
      }

      when (topLevelDestination) {
        TopLevelDestination.COLLECTIONS -> navController.navigateToOutfits(topLevelNavOptions)
        TopLevelDestination.ARTICLES -> navController.navigateToArticles(topLevelNavOptions)
      }
    }
  }
}

@Composable
fun InKnitNavHost(
  appState: InKnitAppState,
  modifier: Modifier = Modifier,
  startDestination: String = ARTICLES_ROUTE,
) {
  val navController = appState.navController
  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = modifier,
  ){
    composable(route = ARTICLES_ROUTE) { ArticlesRoute() }
  }
}

object InKnitNavigationDefaults {
  @Composable fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant
  @Composable fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer
  @Composable fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun RowScope.InKnitNavigationBarItem(
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  alwaysShowLabel: Boolean = true,
  icon: @Composable () -> Unit,
  selectedIcon: @Composable () -> Unit = icon,
  label: @Composable (() -> Unit)? = null,
) {
  NavigationBarItem(
    selected = selected,
    onClick = onClick,
    icon = if (selected) selectedIcon else icon,
    modifier = modifier,
    enabled = enabled,
    label = label,
    alwaysShowLabel = alwaysShowLabel,
    colors = NavigationBarItemDefaults.colors(
      selectedIconColor = InKnitNavigationDefaults.navigationSelectedItemColor(),
      unselectedIconColor = InKnitNavigationDefaults.navigationContentColor(),
      selectedTextColor = InKnitNavigationDefaults.navigationSelectedItemColor(),
      unselectedTextColor = InKnitNavigationDefaults.navigationContentColor(),
      indicatorColor = InKnitNavigationDefaults.navigationIndicatorColor(),
    ),
  )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
  this?.hierarchy?.any {
    it.route?.contains(destination.name, true) ?: false
  } ?: false

@Composable
private fun NiaBottomBar(
  destinations: List<TopLevelDestination>,
  onNavigateToDestination: (TopLevelDestination) -> Unit,
  currentDestination: NavDestination?,
  modifier: Modifier = Modifier,
) {
  NavigationBar(
    modifier = modifier,
  ) {
    destinations.forEach { destination ->
      val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
      InKnitNavigationBarItem(
        selected = selected,
        onClick = { onNavigateToDestination(destination) },
        icon = { Icon(imageVector = destination.unselectedIcon, contentDescription = null) },
        selectedIcon = { Icon(imageVector = destination.selectedIcon, contentDescription = null) },
        label = { Text(stringResource(destination.iconTextId)) },
        modifier = Modifier,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun InKnitApp(
  appState: InKnitAppState,
  modifier: Modifier = Modifier,
) {
  val color = Color.Unspecified
  val tonalElevation = Dp.Unspecified
  Surface(
    color = if (color == Color.Unspecified) Color.Transparent else color,
    tonalElevation = if (tonalElevation == Dp.Unspecified) 0.dp else tonalElevation,
    modifier = modifier.fillMaxSize(),
  ) {
    CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
      InKnitNavHost(
        appState = appState,
        modifier = modifier,
      )
    }
  }
}