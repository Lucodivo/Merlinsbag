package com.inasweaterpoorlyknit.inknit.ui.screen

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.degToRad
import com.inasweaterpoorlyknit.inknit.ui.pixelsToDp
import com.inasweaterpoorlyknit.inknit.ui.toast
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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

@Composable
fun rotatableImage(
  bitmap: Bitmap?, // displays progress indicator if null
  modifier: Modifier = Modifier,
  ccwRotaitonAngle: Float = 0.0f,
) {
  var maxBoxSize by remember { mutableStateOf(IntSize(0, 0)) }
  Box(contentAlignment = Alignment.Center,
    modifier = modifier.fillMaxSize().onSizeChanged { boxSize ->
      if(boxSize.width != maxBoxSize.width || boxSize.height != maxBoxSize.height) {
        maxBoxSize = boxSize
      }
    }){
    if(bitmap != null){
      val absSin = abs(sin(ccwRotaitonAngle.degToRad()))
      val absCos = abs(cos(ccwRotaitonAngle.degToRad()))
      val maxImageSize = DpSize(
        pixelsToDp(((maxBoxSize.width * absCos) + (maxBoxSize.height * absSin)).toInt()),
        pixelsToDp(((maxBoxSize.height * absCos) + (maxBoxSize.width * absSin)).toInt()),
      )
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = stringResource(id = R.string.processed_image),
        modifier = Modifier.rotate(ccwRotaitonAngle).sizeIn(maxWidth = maxImageSize.width, maxHeight = maxImageSize.height)
      )
    } else {
      CircularProgressIndicator()
    }
  }
}