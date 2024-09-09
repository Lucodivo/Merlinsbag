package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun shimmerBrush(
    color: Color = Color.LightGray,
): Brush {
  val shimmerColors = listOf(
    color.copy(alpha = 0.6f),
    color.copy(alpha = 0.52f),
    color.copy(alpha = 0.6f),
  )

  val pulseRateMs = 3000L
  val xyCoord = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    while(true){
      xyCoord.animateTo(
        targetValue = 1300f,
        animationSpec = tween(
            durationMillis = 600,
        ),
      )
      xyCoord.animateTo(
        targetValue = 0f,
        animationSpec = tween(
          durationMillis = 300,
        ),
      )
      delay(pulseRateMs)
    }
  }
  return Brush.linearGradient(
    colors = shimmerColors,
    start = Offset.Zero,
    end = Offset(x = xyCoord.value, y = xyCoord.value)
  )
}