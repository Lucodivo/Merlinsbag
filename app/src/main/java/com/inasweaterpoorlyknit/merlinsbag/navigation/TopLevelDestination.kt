package com.inasweaterpoorlyknit.merlinsbag.navigation

import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLES_ROUTE

sealed class TopLevelDestination {
  data object ARTICLES: TopLevelDestination()
  data object ENSEMBLES: TopLevelDestination()

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