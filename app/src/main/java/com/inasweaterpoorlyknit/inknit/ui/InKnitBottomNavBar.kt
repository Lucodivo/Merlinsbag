package com.inasweaterpoorlyknit.inknit.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.navigation.TopLevelDestination
import com.inasweaterpoorlyknit.inknit.ui.theme.AppTheme

open class BottomNavBarData(
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  @StringRes val iconTextId: Int,
)

@Composable
fun InKnitBottomNavBar(
  bottomNavBarDataItems: List<BottomNavBarData>,
  onClick: (index: Int) -> Unit,
  selectedIndex: Int,
  modifier: Modifier = Modifier,
){
  InKnitNavBar(
    modifier = modifier
  ) {
    bottomNavBarDataItems.forEachIndexed { index, item ->
      InKnitNavigationBarItem(
        selected = selectedIndex == index,
        onClick = { onClick(index) },
        icon = {
          Icon(
            imageVector = item.unselectedIcon,
            contentDescription = null,
          )
        },
        selectedIcon = {
          Icon(
            imageVector = item.selectedIcon,
            contentDescription = null,
          )
        },
        label = { Text(stringResource(item.iconTextId)) },
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
      bottomNavBarDataItems = TopLevelDestination.entries,
      onClick = {_ ->},
      selectedIndex = 0,
    )
  }
}

@Preview
@Composable
fun InKnitBottomNavBarPreviewDark(){
  AppTheme(darkTheme = true) {
    InKnitBottomNavBar(
      bottomNavBarDataItems = TopLevelDestination.entries,
      onClick = {_ ->},
      selectedIndex = TopLevelDestination.entries.lastIndex,
    )
  }
}
