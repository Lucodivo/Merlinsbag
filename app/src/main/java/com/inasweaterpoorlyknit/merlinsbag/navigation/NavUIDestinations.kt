package com.inasweaterpoorlyknit.merlinsbag.navigation

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.ArticlesRouteArgs
import com.inasweaterpoorlyknit.merlinsbag.ui.screen.EnsemblesRouteArgs

enum class NavUIDestinations(
    @StringRes val label: Int,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit,
    val route: Any,
) {
  ARTICLES(
    label = R.string.articles,
    selectedIcon = {
      Icon(
        imageVector = NoopIcons.ItemsSelected,
        contentDescription = stringResource(R.string.simple_shapes),
      )
    },
    unselectedIcon = {
      Icon(
        imageVector = NoopIcons.Items,
        contentDescription = stringResource(R.string.simple_shapes),
      )
    },
    route = ArticlesRouteArgs
  ),
  ENSEMBLES(
    label = R.string.ensembles,
    selectedIcon = {
      Icon(
        imageVector = NoopIcons.ensemblesSelected(),
        contentDescription = stringResource(R.string.hashtag),
      )
    },
    unselectedIcon = {
      Icon(
        imageVector = NoopIcons.ensembles(),
        contentDescription = stringResource(R.string.hashtag),
      )
    },
    route = EnsemblesRouteArgs
  ),
}