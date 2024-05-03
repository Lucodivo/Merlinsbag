package com.inasweaterpoorlyknit.inknit.ui.screen

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.inasweaterpoorlyknit.inknit.ui.hideSystemUI
import com.inasweaterpoorlyknit.inknit.ui.showSystemUI
import com.inasweaterpoorlyknit.inknit.ui.toast

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
/*
  target value between [0, 360)
  output in degrees of shortest path
 */
fun animateRotationAsState(
  targetValue: Float,
  label: String = "RotationAnimation",
): State<Float> {
  val (lastRotation, setLastRotation) = remember { mutableFloatStateOf(0.0f) } // this keeps last rotation
  var newRotation = lastRotation // newRotation will be updated in proper way
  val modLast = if (lastRotation > 0) lastRotation % 360.0f else 360.0f - (-lastRotation % 360.0f) // last rotation converted to range [-359; 359]

  if (modLast != targetValue) // if modLast isn't equal rotation retrieved as function argument it means that newRotation has to be updated
  {
    val backward = if (targetValue > modLast) modLast + 360.0f - targetValue else modLast - targetValue // distance in degrees between modLast and targetValue going backward
    val forward = if (targetValue > modLast) targetValue - modLast else 360.0f - modLast + targetValue // distance in degrees between modLast and targetValue going forward

    // update newRotation so it will change rotation in the shortest way
    newRotation = if (backward < forward) {
      // backward rotation is shorter
      lastRotation - backward
    } else {
      // forward rotation is shorter (or they are equal)
      lastRotation + forward
    }

    setLastRotation(newRotation)
  }

  return animateFloatAsState(
    targetValue = -newRotation,
    label = label,
  )
}
