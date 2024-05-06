package com.inasweaterpoorlyknit.inknit.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.navigation.APP_START_DESTINATION
import com.inasweaterpoorlyknit.inknit.navigation.InKnitNavHost
import com.inasweaterpoorlyknit.inknit.ui.screen.ADD_ARTICLES_BASE
import com.inasweaterpoorlyknit.inknit.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.CAMERA_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.COLLECTIONS_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.navigateToArticles
import com.inasweaterpoorlyknit.inknit.ui.screen.navigateToCollections
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitIcons

object InKnitNavigationDefaults {
    @Composable fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant
    @Composable fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer
    @Composable fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}

@Composable
fun InKnitNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
){
    NavigationBar(
        modifier = modifier,
        contentColor = InKnitNavigationDefaults.navigationContentColor(),
        tonalElevation = 0.dp,
        content = content,
    )
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


@Composable
private fun InKnitBottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToTopLevelDestination: (from: TopLevelDestination, to: TopLevelDestination) -> Unit,
    startDestination: TopLevelDestination,
    modifier: Modifier = Modifier,
){
    val topLevelDestination = remember { mutableStateOf(startDestination) }
    InKnitNavigationBar(modifier = modifier) {
        destinations.forEach { destination ->
            val selected = destination == topLevelDestination.value
            InKnitNavigationBarItem(
                selected = selected,
                onClick = {
                    val from = topLevelDestination.value
                    topLevelDestination.value = destination
                    onNavigateToTopLevelDestination(from, topLevelDestination.value)
                },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
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
    Scaffold(
        modifier = modifier.semantics {
            testTagsAsResourceId = true
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0,0,0,0),
        bottomBar = {
            if(appState.showBottomNavBar.value){
                InKnitBottomBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToTopLevelDestination = { from, to ->
                        appState.navController.navigateToTopLevelDestination(from, to)
                    },
                    startDestination = TopLevelDestination.routeToTopLevelDestination(APP_START_DESTINATION)!!,
                    modifier = Modifier.testTag("InKnitBottomBar"),
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
                InKnitNavHost(
                    appState = appState,
                    modifier = modifier,
                )
            }
        }
    }
}


enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
){
    ARTICLES(
        selectedIcon = InKnitIcons.ItemsSelected,
        unselectedIcon = InKnitIcons.Items,
        iconTextId = R.string.articles,
    ),
    COLLECTIONS(
        selectedIcon = InKnitIcons.CollectionsSelected,
        unselectedIcon = InKnitIcons.Collections,
        iconTextId = R.string.collections,
    );

    companion object{
        fun routeToTopLevelDestination(route: String): TopLevelDestination? {
            return when(route){
                ARTICLES_ROUTE -> ARTICLES
                COLLECTIONS_ROUTE -> COLLECTIONS
                else -> null
            }
        }
        fun topLevelDestinationToRoute(dest: TopLevelDestination): String {
            return when(dest){
                ARTICLES -> ARTICLES_ROUTE
                COLLECTIONS -> COLLECTIONS_ROUTE
            }
        }
    }
}

@Stable
class InKnitAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries
    val showBottomNavBar: MutableState<Boolean> = mutableStateOf(true)
    var showSystemUI: Boolean = true

    init {
        navController.addOnDestinationChangedListener{ controller, dest, args ->
            dest.route?.let{ route ->
                val hideSystemUI = route.contains(CAMERA_ROUTE)
                if(!hideSystemUI != showSystemUI) {
                    showSystemUI = !hideSystemUI
                    val context = navController.context
                    if(showSystemUI) context.showSystemUI() else context.hideSystemUI()
                }
                val hideNavBar = hideSystemUI || route.contains(ADD_ARTICLES_BASE)
                if(!hideNavBar != showBottomNavBar.value){ showBottomNavBar.value = !hideNavBar }
            }
        }
    }
}

fun NavHostController.navigateToTopLevelDestination(from: TopLevelDestination, to: TopLevelDestination){
    val topLevelNavOptions = navOptions {
        popUpTo(route = TopLevelDestination.topLevelDestinationToRoute(from)){
            inclusive = true
//            saveState = true
        }
        launchSingleTop = true
//        restoreState = true
    }

    when(to){
        TopLevelDestination.ARTICLES -> navigateToArticles(topLevelNavOptions)
        TopLevelDestination.COLLECTIONS -> navigateToCollections(topLevelNavOptions)
    }
}

@Composable
fun rememberInKnitAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
): InKnitAppState {
  return remember(navController, windowSizeClass) {
      InKnitAppState(
          navController = navController,
          windowSizeClass = windowSizeClass,
      )
  }
}