package com.inasweaterpoorlyknit.inknit.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.navigation.TopLevelDestination
import com.inasweaterpoorlyknit.inknit.ui.theme.AppTheme

@Composable
fun InKnitBottomNavBar(
  destinations: List<TopLevelDestination>,
  onNavigateToTopLevelDestination: (from: TopLevelDestination, to: TopLevelDestination) -> Unit,
  startDestination: TopLevelDestination,
  modifier: Modifier = Modifier,
){
  val topLevelDestination = remember { mutableStateOf(startDestination) }
  InKnitNavBar(
    modifier = modifier
  ) {
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
private fun InKnitNavBar(
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
){
  NavigationBar(
    modifier = modifier,
    tonalElevation = 0.dp,
    content = content,
  )
}

@Composable
private fun RowScope.InKnitNavigationBarItem(
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
  )
}

@Preview
@Composable
fun InKnitBottomNavBarPreviewLight(){
  AppTheme(darkTheme = false) {
    InKnitBottomNavBar(
      destinations = TopLevelDestination.entries,
      onNavigateToTopLevelDestination = {_, _ ->},
      startDestination = TopLevelDestination.entries.first(),
    )
  }
}

@Preview
@Composable
fun InKnitBottomNavBarPreviewDark(){
  AppTheme(darkTheme = true) {
    InKnitBottomNavBar(
      destinations = TopLevelDestination.entries,
      onNavigateToTopLevelDestination = {_, _ ->},
      startDestination = TopLevelDestination.entries.last(),
    )
  }
}
