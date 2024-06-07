package com.inasweaterpoorlyknit.merlinsbag.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.COMPOSE_PREVIEW_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.NoopComposePreviewIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme

@Composable
fun NoopExpandingFloatingActionButton(
    expanded: Boolean,
    collapsedIcon: IconData = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
    expandedIcon: IconData = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
    onClick: () -> Unit,
    verticalExpandedButtons: List<TextButtonData> = emptyList(),
    horizontalExpandedButtons: List<TextButtonData> = emptyList(),
) {
  val columnPadding = 20.dp
  val expandedButtonPadding = 4.dp
  Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier.padding(columnPadding)
    ) {
      val openAnimateFloat by animateFloatAsState(
        targetValue = if(expanded) 1.0f else 0.0f,
        animationSpec = tween(),
        label = "floating action button size"
      )
      val animAlpha = (-(openAnimateFloat - 1.0f) * (openAnimateFloat - 1.0f)) + 1.0f // https://www.desmos.com/calculator/6ru1kya9ar
      Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.graphicsLayer {
          scaleY = openAnimateFloat
          scaleX = openAnimateFloat
          alpha = animAlpha
          transformOrigin = TransformOrigin(0.5f, 1.0f)
        }) {
        verticalExpandedButtons.forEach { button ->
          FloatingActionButton(
            onClick = button.onClick,
            modifier = Modifier.padding(bottom = expandedButtonPadding)
          ) {
            Icon(button.icon.icon, button.icon.contentDescription)
          }
        }
      }
      Row(
        horizontalArrangement = Arrangement.End,
      ){
        Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.graphicsLayer {
            scaleY = openAnimateFloat
            scaleX = openAnimateFloat
            alpha = animAlpha
            transformOrigin = TransformOrigin(1.0f, 0.5f)
          }
        ){
          horizontalExpandedButtons.forEach { button ->
            FloatingActionButton(
              onClick = button.onClick,
              modifier = Modifier.padding(end = expandedButtonPadding)
            ) {
              Icon(button.icon.icon, button.icon.contentDescription)
            }
          }
        }
        FloatingActionButton(onClick = onClick) {
          val iconData = if(expanded) expandedIcon else collapsedIcon
          Icon(iconData.icon, iconData.contentDescription)
        }
      }
    }
  }
}

@Composable
fun NoopFloatingActionButton(
    iconData: IconData,
    onClick: () -> Unit,
) {
  val buttonPadding = 20.dp
  Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = Modifier.fillMaxSize()
  ) {
    FloatingActionButton(
      onClick = { onClick() },
      modifier = Modifier.padding(buttonPadding)
    ) {
      Icon(iconData.icon, iconData.contentDescription)
    }
  }
}

//region Previews
// Allows previews to take up less space in the preview window
@Preview(name = "NoopFloatingActionButtonPreview", device = "spec:shape=Normal,width=300,height=300,unit=dp,dpi=480")
annotation class NoopFloatingActionButtonPreview

@NoopFloatingActionButtonPreview
@Composable
fun PreviewNoopExpandingFloatingActionButtonCollapsed() = NoopTheme {
  NoopFloatingActionButton(
    iconData = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
    onClick = {}
  )
}

@NoopFloatingActionButtonPreview
@Composable
fun PreviewNoopExpandingFloatingActionButtonExpanded() = NoopTheme {
  NoopExpandingFloatingActionButton(
    expanded = true,
    collapsedIcon = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
    expandedIcon = IconData(NoopComposePreviewIcons.Remove, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
    verticalExpandedButtons = listOf(
      TextButtonData(IconData(NoopComposePreviewIcons.AddPhotoAlbum, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
      TextButtonData(IconData(NoopComposePreviewIcons.AddPhotoCamera, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
    ),
    horizontalExpandedButtons = listOf(
      TextButtonData(IconData(NoopComposePreviewIcons.Save, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
      TextButtonData(IconData(NoopComposePreviewIcons.Settings, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
    ),
    onClick = {}
  )
}
//endregion