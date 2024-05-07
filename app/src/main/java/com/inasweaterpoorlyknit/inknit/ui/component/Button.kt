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
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitIcons

@Composable
fun ExpandingFloatingActionButton(
  expanded: Boolean = false,
  collapsedIcon: IconData = IconData(InKnitIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
  expandedIcon: IconData = IconData(InKnitIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
  onClickExpandCollapse: () -> Unit = {},
  expandedButtons: List<TextIconButtonData> = emptyList()
) {
    // add article floating buttons
  Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier.padding(20.dp)
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
          // https://www.desmos.com/calculator/6ru1kya9ar
          alpha = (-(openAnimateFloat - 1.0f) * (openAnimateFloat - 1.0f)) + 1.0f
          transformOrigin = TransformOrigin(0.9f, 1.0f)
        }) {
        expandedButtons.forEach { button ->
          ExtendedFloatingActionButton(
            text = { Text(button.text) },
            icon = { Icon(button.icon.icon, button.icon.contentDescription) },
            onClick = button.onClick,
            modifier = Modifier.padding(bottom = 4.dp)
          )
        }
      }

      FloatingActionButton(
        onClick = { onClickExpandCollapse() },
      ) {
        if (expanded) {
          Icon(expandedIcon.icon, expandedIcon.contentDescription)
        } else {
          Icon(collapsedIcon.icon, collapsedIcon.contentDescription)
        }
      }
    }
  }
}