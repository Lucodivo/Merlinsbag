package com.inasweaterpoorlyknit.merlinsbag.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.COMPOSE_PREVIEW_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import kotlin.math.max
import kotlin.math.min

@Composable
fun HorizontalOverlappingLayout(
    modifier: Modifier = Modifier,
    overlapPercentage: Float = 0.5f,
    overflowIcon: IconData = IconData(icon = NoopIcons.MoreHorizontal, contentDescription = TODO_ICON_CONTENT_DESCRIPTION),
    content: @Composable () -> Unit,
) {
  Box(modifier = modifier) {
    Layout(
      content = {
        content()
        Icon(overflowIcon.icon, contentDescription = overflowIcon.contentDescription)
      },
    ) { measurables, constraints ->
      val showingPercentage = 1.0f - overlapPercentage
      val iconPlaceable = measurables.last().measure(constraints)
      val placeables = Array(measurables.size - 1) { index -> measurables[index].measure(constraints) }
      val maxHeight = min(
        constraints.maxHeight,
        if(placeables.isNotEmpty()) {
          max(placeables.maxOfOrNull { it.height } ?: 0, iconPlaceable.height)
        } else 0
      )
      val maxWidth = min(
        constraints.maxWidth,
        if(placeables.isNotEmpty()) {
          placeables.sumOf { (it.width * showingPercentage).toInt() } + (placeables.last().width * overlapPercentage).toInt() + iconPlaceable.width
        } else 0
      )
      layout(width = maxWidth, height = maxHeight) {
        var xPos = 0
        for(i in 0..placeables.lastIndex) {
          val placeable = placeables[i]
          val widthInc = (placeable.width * showingPercentage).toInt()
          if((xPos + placeable.width) > (constraints.maxWidth - iconPlaceable.width)) {
            if(i > 0) {
              xPos += (placeables[i - 1].width * overlapPercentage).toInt()
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
}

//region COMPOSABLE PREVIEWS
@Preview
@Composable
fun PreviewHorizontalOverlappingLayout() = NoopTheme {
  val thumbnailUriStrings = repeatedThumbnailResourceIdsAsStrings.slice(6..16)
  val padding = 10.dp
  Surface {
    HorizontalOverlappingLayout(
      modifier = Modifier
          .fillMaxWidth()
          .padding(padding)
    ) {
      thumbnailUriStrings.forEach { thumbnailUriString ->
        NoopImage(
          uriString = thumbnailUriString,
          contentDescription = COMPOSE_PREVIEW_CONTENT_DESCRIPTION,
        )
      }
    }
  }
}

@Preview
@Composable
fun PreviewHorizontalOverlappigLayoutOverflow() = NoopTheme {
  val thumbnailUriStrings = repeatedThumbnailResourceIdsAsStrings.slice(5..11)
  val padding = 10.dp
  Surface {
    HorizontalOverlappingLayout(
      modifier = Modifier
          .fillMaxWidth()
          .padding(padding)
    ) {
      thumbnailUriStrings.forEach { thumbnailUriString ->
        NoopImage(
          uriString = thumbnailUriString,
          contentDescription = COMPOSE_PREVIEW_CONTENT_DESCRIPTION,
        )
      }
    }
  }
}
//endregion