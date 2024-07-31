import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun staggeredHorizontallyAnimatedComposables(
    millisecondsPerRow: Int = 30,
    content: List<@Composable AnimatedVisibilityScope.() -> Unit>)
: List<@Composable () -> Unit> {
  val animationFloat = remember { Animatable(initialValue = 0.0f) }
  LaunchedEffect(content.size) {
    animationFloat.animateTo(
      targetValue = content.size * 0.1f + 0.1f, // +0.1 as a safety buffer
      animationSpec = TweenSpec(
        durationMillis = millisecondsPerRow * content.size,
        easing = LinearEasing,
      )
    )
  }
  return content.mapIndexed { index, item -> {
    AnimatedVisibility(
      visible = animationFloat.value >= (0.1f * index),
      enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
      exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }),
      content = item,
    )
  }}
}
