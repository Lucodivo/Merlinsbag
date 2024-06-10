package com.inasweaterpoorlyknit.merlinsbag.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.merlinsbag.navigation.TopLevelDestination
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme

open class BottomNavBarData(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
)

@Composable
fun NoopBottomNavBar(
    bottomNavBarDataItems: List<BottomNavBarData>,
    onClick: (index: Int) -> Unit,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
  NoopNavBar(modifier = modifier) {
    bottomNavBarDataItems.forEachIndexed { index, item ->
      NoopNavigationBarItem(
        selected = selectedIndex == index,
        onClick = { onClick(index) },
        icon = {
          Icon(
            imageVector = item.unselectedIcon,
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
          )
        },
        selectedIcon = {
          Icon(
            imageVector = item.selectedIcon,
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
          )
        },
        label = { Text(stringResource(item.iconTextId)) },
        modifier = Modifier,
      )
    }
  }
}

@Composable
private fun NoopNavBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
  NavigationBar(
    modifier = modifier,
    tonalElevation = 0.dp,
    content = content,
  )
}

@Composable
private fun RowScope.NoopNavigationBarItem(
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
    icon = if(selected) selectedIcon else icon,
    modifier = modifier,
    enabled = enabled,
    label = label,
    alwaysShowLabel = alwaysShowLabel,
  )
}

@Preview
@Composable
fun PreviewNoopBottomNavBarPreviewLight() = NoopTheme(darkTheme = false) {
  NoopBottomNavBar(
    bottomNavBarDataItems = TopLevelDestination.entries,
    onClick = { _ -> },
    selectedIndex = 0,
  )
}

@Preview
@Composable
fun PreviewNoopBottomNavBarDark() = NoopTheme(darkTheme = true) {
  NoopBottomNavBar(
    bottomNavBarDataItems = TopLevelDestination.entries,
    onClick = { _ -> },
    selectedIndex = TopLevelDestination.entries.lastIndex,
  )
}