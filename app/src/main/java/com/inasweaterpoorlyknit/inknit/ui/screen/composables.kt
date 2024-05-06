package com.inasweaterpoorlyknit.inknit.ui.screen

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State.Loading
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.degToRad
import com.inasweaterpoorlyknit.inknit.ui.pixelsToDp
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitIcons
import com.inasweaterpoorlyknit.inknit.ui.toast
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
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

@Composable
fun ArticleThumbnailImage(
  uriString: String,
  modifier: Modifier = Modifier,
){
  val composePreview = LocalInspectionMode.current
  var isLoading by remember { mutableStateOf(true) }
  Box(contentAlignment = Alignment.Center,
    modifier = modifier
  ){
    if(isLoading && !composePreview){
      CircularProgressIndicator()
    }
    if(!composePreview){
      AsyncImage(
        model = uriString,
        contentDescription = "thumbnail desc",
        onState = { state ->
          isLoading = state is Loading
        },
      )
    } else {
      Image(
        painter = painterResource(id = uriString.toInt()),
        contentDescription = "preview thumbnail",
      )
    }
  }
}

@Composable
fun OverlappingCollectionLayout(
  modifier: Modifier = Modifier,
  overlapPercentage: Float = 0.5f,
  content: @Composable () -> Unit
) {
  Layout(
    modifier = modifier,
    content = {
      content()
      Icon(InKnitIcons.MoreHorizontal, contentDescription = null)
    },
  ){ measurables, constraints ->
    val showingPercentage = 1.0f - overlapPercentage
    val iconPlaceable = measurables.last().measure(constraints)
    val thumbnailPlaceables = Array(measurables.size - 1){ index -> measurables[index].measure(constraints) }
    val maxHeight = min(
      constraints.maxHeight,
      max(thumbnailPlaceables.maxOf { it.height }, iconPlaceable.height)
    )
    val maxWidth = min(
      constraints.maxWidth,
      thumbnailPlaceables.sumOf { (it.width * showingPercentage).toInt() } + (thumbnailPlaceables.last().width * overlapPercentage).toInt() + iconPlaceable.width
    )
    layout(width = maxWidth, height = maxHeight) {
      var xPos = 0
      for(i in 0..thumbnailPlaceables.lastIndex) {
        val placeable = thumbnailPlaceables[i]
        val widthInc = (placeable.width * showingPercentage).toInt()
        if((xPos + placeable.width) > (constraints.maxWidth - iconPlaceable.width)) {
          if(i > 0){
            xPos += (thumbnailPlaceables[i-1].width * overlapPercentage).toInt()
            iconPlaceable.placeRelative(x = xPos, y = (maxHeight / 2) - (iconPlaceable.height / 2))
          }
          break
        }
        placeable.placeRelative(x = xPos, y = 0, zIndex = i.toFloat())
        xPos += widthInc
      }
    }
  }
}
