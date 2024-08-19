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
import com.inasweaterpoorlyknit.core.ui.isComposePreview

@Composable
fun staggeredHorizontallyAnimatedComposables(
    content: List<@Composable AnimatedVisibilityScope.() -> Unit>,
    millisecondsPerRow: Int = 30,
): List<@Composable () -> Unit> {
  val targetValue = content.size * 0.1f + 0.1f // +0.1 as a safety buffer
  val initialValue = if(isComposePreview) targetValue else 0.0f
  val animationFloat = remember { Animatable(initialValue = initialValue) }
  LaunchedEffect(content.size) {
    animationFloat.animateTo(
      targetValue = targetValue,
      animationSpec = TweenSpec(
        durationMillis = millisecondsPerRow * content.size,
        easing = LinearEasing,
      )
    )
  }
  return content.mapIndexed { index, item -> {
    val visibility = animationFloat.value >= (0.1f * (index + 1))
    AnimatedVisibility(
      visible = visibility,
      enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
      exit = slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }),
      content = item,
    )
  }}
}
