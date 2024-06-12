package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

open class BottomNavBarData(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val title: String,
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
        label = { Text(item.title) },
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

//region PREVIEW COMPOSABLES
@Composable
fun PreviewUtilNoopBottmNavbar(darkMode: DarkMode = DarkMode.LIGHT) = NoopTheme(darkMode = darkMode) {
  val composePreviewNavBarData = listOf(
    BottomNavBarData(
      selectedIcon = NoopIcons.ItemsSelected,
      unselectedIcon = NoopIcons.Items,
      title = "Article"
    ),
    BottomNavBarData(
      selectedIcon = NoopIcons.ItemsSelected,
      unselectedIcon = NoopIcons.Items,
      title = "Ensemble",
    ),
  )
  NoopBottomNavBar(
    bottomNavBarDataItems = composePreviewNavBarData,
    onClick = {},
    selectedIndex = 0,
  )
}

@Preview @Composable private fun PreviewNoopBottomNavBarLight() = PreviewUtilNoopBottmNavbar(DarkMode.LIGHT)
@Preview @Composable private fun PreviewNoopBottomNavBarDark() = PreviewUtilNoopBottmNavbar(DarkMode.DARK)
//endregion