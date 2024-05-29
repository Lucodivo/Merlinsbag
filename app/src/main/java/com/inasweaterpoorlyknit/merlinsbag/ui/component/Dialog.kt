package com.inasweaterpoorlyknit.merlinsbag.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme

/*
  Heavily based off of the Material 3 specks for fullscreen dialog:
     https://m3.material.io/components/dialogs/specs
 */
@Composable
fun NoopAddEnsembleDialog(
  visible: Boolean,
  title: String,
  positiveButtonLabel: String,
  modifier: Modifier = Modifier,
  onPositive: () -> Unit,
  onClose: () -> Unit,
  content: @Composable ColumnScope.() -> Unit
) {
  val headerHeight = 56.dp
  val padding = 16.dp
  val scrimAlphaAnimatedScale by animateFloatAsState(
    targetValue = if (visible) 1.0f else 0.0f,
    label = "Dialog sheet scrim animated scale",
  )
  val scrimInteractionSource = remember { MutableInteractionSource() }
  Box(
    contentAlignment = Alignment.BottomCenter,
    modifier = Modifier.fillMaxSize()
  ) {
    if (scrimAlphaAnimatedScale > 0.0f) {
      Box(
        modifier = Modifier
          .testTag("DialogSheetScrim")
          .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f * scrimAlphaAnimatedScale))
          .clickable(interactionSource = scrimInteractionSource, indication = null, onClick = onClose)
          .fillMaxSize()
      )
    }
    AnimatedVisibility(
      visible = visible,
      enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
      exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
    ) {
      Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
      ) {
        Column(
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .height(headerHeight),
          ) {
            Row(
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = NoopIcons.Close,
                contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
                modifier = Modifier
                  .size(headerHeight)
                  .padding(padding)
                  .clickable { onClose() }
              )
              Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
              )
            }
            Text(
              text = positiveButtonLabel,
              color = MaterialTheme.colorScheme.primary,
              fontSize = MaterialTheme.typography.labelLarge.fontSize,
              textAlign = TextAlign.End,
              modifier = Modifier
                .height(headerHeight)
                .padding(padding)
                .clickable { onPositive() }
            )
          }
          content()
          Spacer(modifier = Modifier.height(padding))
        }
      }
    }
  }
}

// TODO: Consider implementing as a Modal Bottom Sheet?
@Preview
@Composable
fun PreviewNoopDialog() {
  NoopTheme {
    NoopAddEnsembleDialog(
      visible = true,
      title = "Dialog title",
      positiveButtonLabel = "Save",
      onPositive = {},
      onClose = {}
    ){
      OutlinedTextField(
        value = "",
        placeholder = {},
        onValueChange = {},
        label = { Text(text = "Preview label") },
      )
      Spacer(modifier = Modifier.height(10.dp))
      OutlinedTextField(
        value = "user text",
        placeholder = {},
        onValueChange = {},
        label = { Text(text = "Preview label") },
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ){
        Switch(checked = false, onCheckedChange = {})
        Spacer(modifier = Modifier.size(width = 30.dp, height = 0.dp))
        Text(text = "Preview switch", textAlign = TextAlign.End)
      }
    }
  }
}