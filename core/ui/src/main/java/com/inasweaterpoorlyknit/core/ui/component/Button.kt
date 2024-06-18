package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.ui.COMPOSE_PREVIEW_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.NoopComposePreviewIcons
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

@Composable
fun NoopBottomEndButtonContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = modifier.fillMaxSize().padding(8.dp),
    content = content,
  )


@Composable
fun NoopIconButton(
    iconData: IconData,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = FilledTonalButton (
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.sizeIn(maxHeight = ButtonDefaults.MinHeight),
) {
  Icon(
    imageVector = iconData.icon,
    contentDescription = iconData.contentDescription,
  )
}

@Composable
fun NoopExpandingIconButton(
    expanded: Boolean,
    enabled: Boolean = true,
    collapsedIcon: IconData = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
    expandedIcon: IconData = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
    onClick: () -> Unit,
    verticalExpandedButtons: List<IconButtonData> = emptyList(),
    horizontalExpandedButtons: List<IconButtonData> = emptyList(),
) {
  val buttonModifier = Modifier.padding(2.dp)
  Column(horizontalAlignment = Alignment.End) {
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
      }
    ) {
      verticalExpandedButtons.forEach { button ->
        NoopIconButton(
          iconData = button.icon,
          enabled = button.enabled,
          onClick = button.onClick,
          modifier = buttonModifier,
        )
      }
    }
    Row(horizontalArrangement = Arrangement.End){
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
          NoopIconButton(
            iconData = button.icon,
            enabled = button.enabled,
            onClick = button.onClick,
            modifier = buttonModifier,
          )
        }
      }
      NoopIconButton(
        iconData = if(expanded) expandedIcon else collapsedIcon,
        enabled = enabled,
        onClick = onClick,
        modifier = buttonModifier,
      )
    }
  }
}

//region Previews
@Preview
@Composable
fun PreviewNoopExpandingIconButtonCollapsed() = NoopTheme {
  NoopIconButton(
    iconData = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
    onClick = {}
  )
}

@Preview
@Composable
fun PreviewNoopExpandingIconButtonExpanded() = NoopTheme {
  NoopExpandingIconButton(
    expanded = true,
    collapsedIcon = IconData(NoopComposePreviewIcons.Edit, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
    expandedIcon = IconData(NoopComposePreviewIcons.Remove, COMPOSE_PREVIEW_CONTENT_DESCRIPTION),
    verticalExpandedButtons = listOf(
      IconButtonData(IconData(NoopComposePreviewIcons.AddPhotoAlbum, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
      IconButtonData(IconData(NoopComposePreviewIcons.AddPhotoCamera, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
    ),
    horizontalExpandedButtons = listOf(
      IconButtonData(IconData(NoopComposePreviewIcons.Save, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
      IconButtonData(IconData(NoopComposePreviewIcons.Settings, COMPOSE_PREVIEW_CONTENT_DESCRIPTION)) {},
    ),
    onClick = {}
  )
}
//endregion