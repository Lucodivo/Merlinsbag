package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun shimmerBrush(
    color: Color = Color.LightGray,
): Brush {
  val shimmerColors = listOf(
    color.copy(alpha = 0.6f),
    color.copy(alpha = 0.52f),
    color.copy(alpha = 0.6f),
  )

  val transition = rememberInfiniteTransition(label = "shimmer brush transition")
  val translateAnimation = transition.animateFloat(
    initialValue = 0f,
    targetValue = 1300f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = 800,
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer brush animation"
  )
  return Brush.linearGradient(
    colors = shimmerColors,
    start = Offset.Zero,
    end = Offset(x = translateAnimation.value, y = translateAnimation.value)
  )
}