package com.inasweaterpoorlyknit.inknit.ui.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons
import kotlin.math.max
import kotlin.math.min

@Composable
fun HorizontalOverlappingCollectionLayout(
  modifier: Modifier = Modifier,
  overlapPercentage: Float = 0.5f,
  overflowIcon: IconData = IconData(icon = NoopIcons.MoreHorizontal, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
  content: @Composable () -> Unit
) {
  Layout(
    modifier = modifier,
    content = {
      content()
      Icon(overflowIcon.icon, contentDescription = overflowIcon.contentDescription)
    },
  ) { measurables, constraints ->
    val showingPercentage = 1.0f - overlapPercentage
    val iconPlaceable = measurables.last().measure(constraints)
    val thumbnailPlaceables = Array(measurables.size - 1) { index -> measurables[index].measure(constraints) }
    val maxHeight = min(
      constraints.maxHeight,
      max(thumbnailPlaceables.maxOf { it.height }, iconPlaceable.height)
    )
    val maxWidth = min(
      constraints.maxWidth,
      thumbnailPlaceables.sumOf { (it.width * showingPercentage).toInt() } + (thumbnailPlaceables.last().width * overlapPercentage).toInt() + iconPlaceable.width
    )
    layout(width = maxWidth, height = maxHeight) {
      var xPos = 0
      for (i in 0..thumbnailPlaceables.lastIndex) {
        val placeable = thumbnailPlaceables[i]
        val widthInc = (placeable.width * showingPercentage).toInt()
        if ((xPos + placeable.width) > (constraints.maxWidth - iconPlaceable.width)) {
          if (i > 0) {
            xPos += (thumbnailPlaceables[i - 1].width * overlapPercentage).toInt()
            iconPlaceable.placeRelative(x = xPos, y = (maxHeight / 2) - (iconPlaceable.height / 2))
          }
          break
        }
        val yPos = (maxHeight - placeable.height) / 2
        val zIndex = i.toFloat()
        placeable.placeRelative(x = xPos, y = yPos, zIndex = zIndex)
        xPos += widthInc
      }
    }
  }
}