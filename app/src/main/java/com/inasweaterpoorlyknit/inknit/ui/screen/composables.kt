package com.inasweaterpoorlyknit.inknit.ui.screen

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.inasweaterpoorlyknit.inknit.ui.hideSystemUI
import com.inasweaterpoorlyknit.inknit.ui.showSystemUI
import com.inasweaterpoorlyknit.inknit.ui.toast
import kotlin.math.abs

@Composable
@NonRestartableComposable
fun HideSystemUIDisposableEffect() {
  val activity = LocalView.current.context as Activity
  DisposableEffect(Unit) {
    activity.window.hideSystemUI()
    onDispose {
      activity.window.showSystemUI()
    }
  }
}

@Composable
fun Toast(@StringRes msg: Int) = LocalContext.current.toast(msg)

@Composable
/* target value between [0, 360) */
fun animateRotationAsState(
  targetValue: Float,
  label: String = "RotationAnimation",
): State<Float> {
  val (lastRotation, setLastRotation) = remember { mutableFloatStateOf(0.0f) }

  val modLast = lastRotation % 360.0f
  val deltaRotation = targetValue - modLast
  val (degreeDeltaCW, degreeDeltaCCW) = if(targetValue > modLast){
    Pair(deltaRotation - 360.0f, deltaRotation)
  } else {
    Pair(deltaRotation, deltaRotation + 360.0f)
  }

  val newRotation = lastRotation + if(abs(degreeDeltaCW) < degreeDeltaCCW) degreeDeltaCW else degreeDeltaCCW
  setLastRotation(newRotation)

  return animateFloatAsState(
    targetValue = newRotation,
    label = label,
  )
}
