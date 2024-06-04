package com.inasweaterpoorlyknit.merlinsbag.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.BottomNavBarData
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons

sealed class TopLevelDestination(selectedIcon: ImageVector, unselectedIcon: ImageVector, @StringRes iconTextId: Int): BottomNavBarData(selectedIcon = selectedIcon, unselectedIcon = unselectedIcon, iconTextId = iconTextId) {
  data object ARTICLES: TopLevelDestination(NoopIcons.ItemsSelected, NoopIcons.Items, R.string.Articles)
  data object ENSEMBLES: TopLevelDestination(NoopIcons.EnsemblesSelected, NoopIcons.Ensembles, R.string.Ensembles)

  companion object {
    val entries = listOf(ARTICLES, ENSEMBLES)

    fun routeToTopLevelDestination(route: String): TopLevelDestination? {
      return when(route) {
        ARTICLES_ROUTE -> ARTICLES
        ENSEMBLES_ROUTE -> ENSEMBLES
        else -> null
      }
    }

    fun topLevelDestinationToRoute(dest: TopLevelDestination): String {
      return when(dest) {
        ARTICLES -> ARTICLES_ROUTE
        ENSEMBLES -> ENSEMBLES_ROUTE
      }
    }
  }
}