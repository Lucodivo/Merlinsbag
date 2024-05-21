package com.inasweaterpoorlyknit.inknit.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.COMPOSE_PREVIEW_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.NoopComposePreviewIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons

@Composable
private fun _NoopExpandingFloatingActionButton(
  expanded: Boolean,
  collapsedIcon: IconData = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
  expandedIcon: IconData = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
  onClickExpandCollapse: () -> Unit,
  expandedButtons: List<TextIconButtonData>,
) {
  val columnPadding = 20.dp
  val expandedButtonPadding = 4.dp
  Column(
    horizontalAlignment = Alignment.End,
    modifier = Modifier.padding(columnPadding)
  ) {
    val openAnimateFloat by animateFloatAsState(
      targetValue = if (expanded) 1.0f else 0.0f,
      animationSpec = tween(),
      label = "floating action button size"
    )
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier.graphicsLayer {
        scaleY = openAnimateFloat
        scaleX = openAnimateFloat
        alpha = (-(openAnimateFloat - 1.0f) * (openAnimateFloat - 1.0f)) + 1.0f // https://www.desmos.com/calculator/6ru1kya9ar
        transformOrigin = TransformOrigin(0.5f, 1.0f)
      }) {
      expandedButtons.forEach { button ->
        if(button.text.isNotEmpty()) {
          ExtendedFloatingActionButton(
            text = { Text(button.text) },
            icon = { Icon(button.icon.icon, button.icon.contentDescription) },
            onClick = button.onClick,
            modifier = Modifier.padding(bottom = expandedButtonPadding)
          )
        } else {
          FloatingActionButton(
            onClick = button.onClick,
            modifier = Modifier.padding(bottom = expandedButtonPadding)
          ){
            Icon(button.icon.icon, button.icon.contentDescription)
            Text(button.text)
          }
        }
      }
    }
    _NoopFloatingActionButton(
      iconData = if(expanded) expandedIcon else collapsedIcon,
      onClick = onClickExpandCollapse,
    )
  }
}

@Composable
fun NoopExpandingFloatingActionButton(
  expanded: Boolean,
  collapsedIcon: IconData = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
  expandedIcon: IconData = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
  onClickExpandCollapse: () -> Unit,
  expandedButtons: List<TextIconButtonData>,
) {
  Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = Modifier.fillMaxSize()
  ) {
    _NoopExpandingFloatingActionButton(
      expanded = expanded,
      collapsedIcon = collapsedIcon,
      expandedIcon = expandedIcon,
      onClickExpandCollapse = onClickExpandCollapse,
      expandedButtons = expandedButtons,
    )
  }
}

@Composable
private fun _NoopFloatingActionButton(
  iconData: IconData,
  onClick: () -> Unit,
) {
  FloatingActionButton(onClick = { onClick() }) {
    Icon(iconData.icon, iconData.contentDescription)
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

@Preview
@Composable
fun PreviewNoopFloatingActionButton_Alone(){
  NoopTheme {
    _NoopFloatingActionButton(
      iconData = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      onClick = {}
    )
  }
}

@Preview
@Composable
fun PreviewNoopExpandingFloatingActionButtonExpanded_Alone(){
  NoopTheme{
    _NoopExpandingFloatingActionButton(
      expanded = true,
      collapsedIcon = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      expandedIcon = IconData(NoopComposePreviewIcons.Remove, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      expandedButtons = listOf(
        TextIconButtonData("Album", IconData(NoopComposePreviewIcons.AddPhotoAlbum, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)){},
        TextIconButtonData("Camera", IconData(NoopComposePreviewIcons.AddPhotoCamera, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)){},
      ),
      onClickExpandCollapse = {}
    )
  }
}

@Preview
@Composable
fun PreviewNoopExpandingFloatingActionButtonCollapsed_Alone(){
  NoopTheme{
    _NoopExpandingFloatingActionButton(
      expanded = false,
      collapsedIcon = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      expandedIcon = IconData(NoopComposePreviewIcons.Remove, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      expandedButtons = listOf(
        TextIconButtonData("Album", IconData(NoopComposePreviewIcons.AddPhotoAlbum, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)){},
        TextIconButtonData("Camera", IconData(NoopComposePreviewIcons.AddPhotoCamera, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)){},
      ),
      onClickExpandCollapse = {}
    )
  }
}

@Preview
@Composable
fun PreviewNoopExpandingFloatingActionButtonExpanded_Full(){
  NoopTheme{
    NoopExpandingFloatingActionButton(
      expanded = true,
      collapsedIcon = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      expandedIcon = IconData(NoopComposePreviewIcons.Remove, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      expandedButtons = listOf(
        TextIconButtonData("", IconData(NoopComposePreviewIcons.AddPhotoAlbum, COMPOSE_PREVIEW_CONTENT_DESCRIPTION), {}),
        TextIconButtonData("", IconData(NoopComposePreviewIcons.AddPhotoCamera, COMPOSE_PREVIEW_CONTENT_DESCRIPTION), {}),
      ),
      onClickExpandCollapse = {}
    )
  }
}

@Preview
@Composable
fun PreviewNoopFloatingActionButton_Full(){
  NoopTheme {
    NoopFloatingActionButton(
      iconData = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
      onClick = {}
    )
  }
}