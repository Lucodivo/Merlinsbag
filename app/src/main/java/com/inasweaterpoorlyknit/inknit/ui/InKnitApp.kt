package com.inasweaterpoorlyknit.inknit.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.navigation.InKnitNavHost
import com.inasweaterpoorlyknit.inknit.ui.screen.navigateToArticles
import com.inasweaterpoorlyknit.inknit.ui.screen.navigateToOutfits
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.selects.select

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun InKnitApp(
    appState: InKnitAppState,
    modifier: Modifier = Modifier,
) {
/*
    Scaffold(
        modifier = modifier.semantics {
            testTagsAsResourceId = true
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0,0,0,0),
        bottomBar = {
            if(appState.showBottomNavBar){
                InKnitBottomBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination,
                    modifier = Modifier.testTag("InKnitBottomBar"),
                )
            }
        }
    ){
*/
        Surface(
            modifier = modifier.fillMaxSize(),
        ) {
            CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
                InKnitNavHost(
                    appState = appState,
                    modifier = modifier,
                )
            }
        }
//    }
}


enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
){
    ARTICLES(
        selectedIcon = InKnitIcons.ItemsSelected,
        unselectedIcon = InKnitIcons.Items,
        iconTextId = R.string.assorted_blocks,
        titleTextId = R.string.articles,
    ),
    COLLECTIONS(
        selectedIcon = InKnitIcons.CollectionsSelected,
        unselectedIcon = InKnitIcons.Collections,
        iconTextId = R.string.folder_with_star,
        titleTextId = R.string.outfits
    )
}

@Stable
class InKnitAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
    val showBottomNavBar: Boolean,
) {
}

fun NavHostController.navigateToTopLevelDestination(topLevelDestination: TopLevelDestination){
    val topLevelNavOptions = navOptions {
        popUpTo(
            graph.findStartDestination().id,
        ){
            inclusive = true
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

    when(topLevelDestination){
        TopLevelDestination.ARTICLES -> navigateToArticles(topLevelNavOptions)
        TopLevelDestination.COLLECTIONS -> navigateToOutfits(topLevelNavOptions)
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
          showBottomNavBar = true,
      )
  }
}