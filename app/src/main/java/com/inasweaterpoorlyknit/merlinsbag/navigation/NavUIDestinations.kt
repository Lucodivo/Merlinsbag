package com.inasweaterpoorlyknit.merlinsbag.navigation

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ARTICLES_ROUTE
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ENSEMBLES_ROUTE

enum class NavUIDestinations(
    @StringRes val label: Int,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit,
    val route: String,
) {
  ARTICLES(
    label = R.string.articles,
    selectedIcon = {
      Icon(
        imageVector = NoopIcons.ItemsSelected,
        contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
      )
    },
    unselectedIcon = {
      Icon(
        imageVector = NoopIcons.Items,
        contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
      )
    },
    route = ARTICLES_ROUTE
  ),
  ENSEMBLES(
    label = R.string.ensembles,
    selectedIcon = {
      Icon(
        imageVector = NoopIcons.ensemblesSelected(),
        contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
      )
    },
    unselectedIcon = {
      Icon(
        imageVector = NoopIcons.ensembles(),
        contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
      )
    },
    route = ENSEMBLES_ROUTE
  ),
}