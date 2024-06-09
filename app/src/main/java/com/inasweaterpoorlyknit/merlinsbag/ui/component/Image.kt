package com.inasweaterpoorlyknit.merlinsbag.ui.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.COMPOSE_PREVIEW_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.composePreviewArticleAsset
import com.inasweaterpoorlyknit.merlinsbag.ui.degToRad
import com.inasweaterpoorlyknit.merlinsbag.ui.isComposePreview
import com.inasweaterpoorlyknit.merlinsbag.ui.pixelsToDp
import com.inasweaterpoorlyknit.merlinsbag.ui.previewAssetBitmap
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import java.io.File
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NoopImage(
    uriString: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
  ) {
    if(isComposePreview) {
      Image(
        painter = painterResource(id = uriString!!.toInt()),
        contentDescription = contentDescription,
      )
      return
    }

    // Although images are all local to the device, Coil's AsyncImage is still useful as it pulls
    // loading of images off the main thread but it also offers image caching and bitmap pooling
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
          .data(uriString)
          .crossfade(300)
          .fallback(R.drawable.broken_image)
          .error(R.drawable.broken_image)
          .build(),
      contentDescription = contentDescription,
    )
  }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun rememberImageBitmap(imageUriString: String): ImageBitmap? {
  val context = LocalContext.current
  val imageUri = Uri.parse(imageUriString)
  return try {
    val inputStream = File(imageUriString).inputStream()
    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
  } catch (e: Exception) {
    Log.e("NoopImage", "Failed to load image", e)
    null
  }
}

@Composable
fun SelectableNoopImage(
    uriString: String?,
    contentDescription: String,
    selected: Boolean,
    selectable: Boolean,
    modifier: Modifier = Modifier,
) {
  Box(contentAlignment = Alignment.Center) {
    NoopImage(
      uriString = uriString,
      contentDescription = contentDescription,
      modifier = modifier
    )
    if(selectable) {
      Icon(
        imageVector = if(selected) NoopIcons.SelectedIndicator else NoopIcons.SelectableIndicator,
        contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
        modifier = Modifier.align(Alignment.BottomEnd),
        tint = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

// TODO: Implement using custom layouts instead of onSizeChanged()
//   less redraw
//   stable composable previews
@Composable
fun NoopRotatableImage(
    bitmap: Bitmap?, // displays progress indicator if null
    ccwRotationAngle: Float, // degrees
    modifier: Modifier = Modifier,
) {
  var maxBoxSize by remember { mutableStateOf(IntSize(0, 0)) }
  Box(contentAlignment = Alignment.Center,
    modifier = modifier
        .fillMaxSize()
        .onSizeChanged { boxSize ->
          if(boxSize.width != maxBoxSize.width || boxSize.height != maxBoxSize.height) {
            maxBoxSize = boxSize
          }
        }) {
    if(bitmap != null) {
      val absSin = abs(sin(ccwRotationAngle.degToRad()))
      val absCos = abs(cos(ccwRotationAngle.degToRad()))
      val maxImageSize = DpSize(
        pixelsToDp(((maxBoxSize.width * absCos) + (maxBoxSize.height * absSin)).toInt()),
        pixelsToDp(((maxBoxSize.height * absCos) + (maxBoxSize.width * absSin)).toInt()),
      )
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
        modifier = Modifier
            .rotate(ccwRotationAngle)
            .sizeIn(maxWidth = maxImageSize.width, maxHeight = maxImageSize.height)
      )
    } else {
      CircularProgressIndicator()
    }
  }
}

//region COMPOSABLE PREVIEWS
@Preview
@Composable
fun PreviewSelectableNoopImage() = NoopTheme {
  Column {
    SelectableNoopImage(
      uriString = R.raw.test_thumb_1.toString(), contentDescription = COMPOSE_PREVIEW_CONTENT_DESCRIPTION,
      selected = false, selectable = false, modifier = Modifier,
    )
    SelectableNoopImage(
      uriString = R.raw.test_thumb_1.toString(), contentDescription = COMPOSE_PREVIEW_CONTENT_DESCRIPTION,
      selected = false, selectable = true, modifier = Modifier,
    )
    SelectableNoopImage(
      uriString = R.raw.test_thumb_1.toString(), contentDescription = COMPOSE_PREVIEW_CONTENT_DESCRIPTION,
      selected = true, selectable = true, modifier = Modifier,
    )
  }
}

@Preview
@Composable
fun PreviewNoopImage() = NoopTheme {
  NoopImage(uriString = R.raw.test_full_1.toString(), contentDescription = null)
}

@Preview
@Composable
fun PreviewNoopRotateableImage180() = NoopTheme {
  NoopRotatableImage(
    bitmap = previewAssetBitmap(filename = composePreviewArticleAsset),
    ccwRotationAngle = 180.0f
  )
}

@Preview
@Composable
fun PreviewNoopRotateableImage90() = NoopTheme {
  NoopRotatableImage(
    bitmap = previewAssetBitmap(filename = composePreviewArticleAsset),
    ccwRotationAngle = 90.0f
  )
}

@Preview
@Composable
fun PreviewNoopRotateableImage45() = NoopTheme {
  NoopRotatableImage(
    bitmap = previewAssetBitmap(filename = composePreviewArticleAsset),
    ccwRotationAngle = 45.0f
  )
}
//endregion