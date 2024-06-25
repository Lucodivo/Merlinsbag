package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.ui.COMPOSE_PREVIEW_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.R
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import kotlin.math.max
import kotlin.math.min

@Composable
fun HorizontalOverlappingLayout(
    modifier: Modifier = Modifier,
    overlapPercentage: Float = 0.5f,
    content: @Composable () -> Unit,
) {
  Box(modifier = modifier) {
    Layout(
      content = {
        content()
        Icon(NoopIcons.MoreHorizontal, contentDescription = stringResource(R.string.more))
      },
    ) { measurables, constraints ->
      if(measurables.size == 1) return@Layout layout(width = 0, height = 0) {}
      val showingPercentage = 1.0f - overlapPercentage
      val iconPlaceable = measurables.last().measure(constraints)
      val placeables = Array(measurables.size - 1) { measurables[it].measure(constraints) }
      val maxHeight = min(
        constraints.maxHeight,
        max(placeables.maxOfOrNull { it.height } ?: 0, iconPlaceable.height)
      )
      val maxWidth = constraints.maxWidth
      val overflowWidth = maxWidth - iconPlaceable.width
      layout(width = maxWidth, height = maxHeight) {
        var xPos = 0
        for(i in 0..placeables.lastIndex) {
          val placeable = placeables[i]
          val widthInc = (placeable.width * showingPercentage).toInt()
          val nextWidth = xPos + placeable.width
          if(nextWidth < overflowWidth || (i == placeables.lastIndex && nextWidth <= maxWidth)) {
            val yPos = (maxHeight - placeable.height) / 2
            val zIndex = i.toFloat()
            placeable.placeRelative(x = xPos, y = yPos, zIndex = zIndex)
            xPos += widthInc
          } else {
            if(i > 0) xPos += (placeables[i - 1].width * overlapPercentage).toInt()
            iconPlaceable.placeRelative(
              x = xPos,
              y = (maxHeight / 2) - (iconPlaceable.height / 2)
            )
            break
          }
        }
      }
    }
  }
}

//region COMPOSABLE PREVIEWS
@Preview
@Composable
fun PreviewHorizontalOverlappingLayout() = NoopTheme {
  val thumbnailUriStrings = repeatedThumbnailResourceIdsAsStrings.slice(6..18)
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