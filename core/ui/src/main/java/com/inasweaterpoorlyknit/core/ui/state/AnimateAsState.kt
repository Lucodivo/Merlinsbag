package com.inasweaterpoorlyknit.core.ui.state

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import kotlin.math.abs

/*
  An animated float built specifically for rotations. When updated with a target rotation,
  it will take the shortest path to that rotation.
  Ex: 270° -> 0°, should rotate in the range of [270°, 360°).
    It should *not*, for instance, interpolate between 270° and 0°.
 */
@Composable
fun animateClosestRotationAsState(
    targetDegrees: Float,
    label: String = "RotationAnimation",
): State<Float> {
  val (lastRotation, setLastRotation) = remember { mutableFloatStateOf(0.0f) }

  val modLast = lastRotation % 360.0f
  val deltaRotation = targetDegrees - modLast
  val (degreeDeltaCW, degreeDeltaCCW) = if(targetDegrees > modLast) {
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