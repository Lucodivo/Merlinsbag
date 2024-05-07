package com.inasweaterpoorlyknit.inknit.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.degToRad
import com.inasweaterpoorlyknit.inknit.ui.isComposePreview
import com.inasweaterpoorlyknit.inknit.ui.pixelsToDp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArticleThumbnailImage(
  uriString: String?,
  contentDescription: String?,
  modifier: Modifier = Modifier,
){
  var isLoading by remember { mutableStateOf(true) }
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
  ) {
    if(isComposePreview){
      Image(
        painter = painterResource(id = uriString!!.toInt()),
        contentDescription = contentDescription,
      )
      return
    }

    if (isLoading) {
      CircularProgressIndicator()
    }
    AsyncImage(
      model = uriString,
      contentDescription = contentDescription,
      onState = { state ->
        isLoading = state is AsyncImagePainter.State.Loading
      },
    )
  }
}

@Composable
fun RotatableImage(
  bitmap: Bitmap?, // displays progress indicator if null
  modifier: Modifier = Modifier,
  ccwRotaitonAngle: Float = 0.0f,
) {
  var maxBoxSize by remember { mutableStateOf(IntSize(0, 0)) }
  Box(contentAlignment = Alignment.Center,
    modifier = modifier.fillMaxSize().onSizeChanged { boxSize ->
      if (boxSize.width != maxBoxSize.width || boxSize.height != maxBoxSize.height) {
        maxBoxSize = boxSize
      }
    }) {
    if (bitmap != null) {
      val absSin = abs(sin(ccwRotaitonAngle.degToRad()))
      val absCos = abs(cos(ccwRotaitonAngle.degToRad()))
      val maxImageSize = DpSize(
        pixelsToDp(((maxBoxSize.width * absCos) + (maxBoxSize.height * absSin)).toInt()),
        pixelsToDp(((maxBoxSize.height * absCos) + (maxBoxSize.width * absSin)).toInt()),
      )
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
        modifier = Modifier.rotate(ccwRotaitonAngle).sizeIn(maxWidth = maxImageSize.width, maxHeight = maxImageSize.height)
      )
    } else {
      CircularProgressIndicator()
    }
  }
}

@Preview
@Composable
fun PreviewArticleThumbnailImage() {
  ArticleThumbnailImage(uriString = R.raw.test_full_1.toString(), contentDescription = null)
}