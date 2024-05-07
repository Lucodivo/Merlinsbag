package com.inasweaterpoorlyknit.inknit.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.BottomNavBarData
import com.inasweaterpoorlyknit.inknit.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.screen.COLLECTIONS_ROUTE
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitIcons

sealed class TopLevelDestination(selectedIcon: ImageVector, unselectedIcon: ImageVector, @StringRes iconTextId: Int)
    : BottomNavBarData(selectedIcon = selectedIcon, unselectedIcon = unselectedIcon, iconTextId = iconTextId){
    data object ARTICLES: TopLevelDestination(InKnitIcons.ItemsSelected, InKnitIcons.Items, R.string.Articles)
    data object COLLECTIONS: TopLevelDestination(InKnitIcons.CollectionsSelected, InKnitIcons.Collections, R.string.Collections)

    companion object{
        val entries = listOf(ARTICLES, COLLECTIONS)

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