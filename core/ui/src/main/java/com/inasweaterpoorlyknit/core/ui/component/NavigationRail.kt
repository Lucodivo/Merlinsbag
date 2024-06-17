package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

data class NoopNavigationRailItem(
    val label: String,
    val onClick: () -> Unit,
    val selectedIcon: IconData,
    val unselectedIcon: IconData,
)

@Composable
fun NoopNavigationRail(
    items: List<NoopNavigationRailItem>,
    selectedIndex: Int,
) {
  NavigationRail {
    items.forEachIndexed { index, item ->
      val selected = index == selectedIndex
      NavigationRailItem(
        selected = selected,
        onClick = item.onClick,
        icon = {
          if(selected) {
            Icon(item.selectedIcon.icon, item.selectedIcon.contentDescription)
          } else {
            Icon(item.unselectedIcon.icon, item.unselectedIcon.contentDescription)
          }
        },
        label = { Text(text = item.label) }
      )
    }
  }
}

@Preview
@Composable
fun PreviewNoopNavigationRail() = NoopTheme {
  NoopNavigationRail(
    items = listOf(
      NoopNavigationRailItem(
        label = "Articles",
        onClick = {},
        selectedIcon = IconData(NoopIcons.ItemsSelected, TODO_ICON_CONTENT_DESCRIPTION),
        unselectedIcon = IconData(NoopIcons.Items, TODO_ICON_CONTENT_DESCRIPTION),
      ),
      NoopNavigationRailItem(
        label = "Ensembles",
        onClick = {},
        selectedIcon = IconData(NoopIcons.ensemblesSelected(), TODO_ICON_CONTENT_DESCRIPTION),
        unselectedIcon = IconData(NoopIcons.ensembles(), TODO_ICON_CONTENT_DESCRIPTION),
      ),
    ),
    selectedIndex = 0
  )
}