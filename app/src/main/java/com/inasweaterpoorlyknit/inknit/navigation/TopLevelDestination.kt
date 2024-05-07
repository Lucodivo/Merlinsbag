package com.inasweaterpoorlyknit.inknit.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.COLLECTIONS_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.navigateToArticles
import com.inasweaterpoorlyknit.inknit.ui.screen.navigateToCollections
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitIcons

enum class TopLevelDestination(
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  @StringRes val iconTextId: Int,
){
    ARTICLES(
        selectedIcon = InKnitIcons.ItemsSelected,
        unselectedIcon = InKnitIcons.Items,
        iconTextId = R.string.Articles,
    ),
    COLLECTIONS(
        selectedIcon = InKnitIcons.CollectionsSelected,
        unselectedIcon = InKnitIcons.Collections,
        iconTextId = R.string.Collections,
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
