package com.inasweaterpoorlyknit.merlinsbag.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.data.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.LandscapePreview
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme

val articleGridColumnMinWidth = 90.dp
val articleGridItemPadding = 8.dp

val placeholderDrawables = arrayOf(
  R.drawable.army_jacket,
  R.drawable.boot,
  R.drawable.shirt,
  R.drawable.hat,
  R.drawable.cat,
  R.drawable.denim_jacket,
  R.drawable.guitar,
  R.drawable.guy,
  R.drawable.kid,
  R.drawable.pants,
  R.drawable.phone,
  R.drawable.wallet,
  R.drawable.shoe,
)
val repeatedPlaceholderDrawables = arrayListOf(
  *placeholderDrawables,
  *placeholderDrawables,
  *placeholderDrawables,
  *placeholderDrawables,
  *placeholderDrawables,
)

@Composable
fun shimmerBrush(
    color: Color = Color.LightGray,
): Brush {
  val shimmerColors = listOf(
    color.copy(alpha = 0.6f),
    color.copy(alpha = 0.55f),
    color.copy(alpha = 0.6f),
  )

  val transition = rememberInfiniteTransition(label = "shimmer brush transition")
  val translateAnimation = transition.animateFloat(
    initialValue = 0f,
    targetValue = 1300f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = 800,
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "shimmer brush animation"
  )
  return Brush.linearGradient(
    colors = shimmerColors,
    start = Offset.Zero,
    end = Offset(x = translateAnimation.value, y = translateAnimation.value)
  )
}

@Composable
fun PlaceholderArticleThumbnailGrid(){
  val articlesGridState = rememberLazyStaggeredGridState()
  val shimmerBrush = shimmerBrush(color = MaterialTheme.colorScheme.onSurface)
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = articleGridColumnMinWidth),
    content = {
      items(count = repeatedPlaceholderDrawables.size) { thumbnailGridItemIndex ->
        val placeholderDrawable = repeatedPlaceholderDrawables[thumbnailGridItemIndex]
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(articleGridItemPadding)
        ) {
          Icon(
            painter = painterResource(placeholderDrawable),
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
            modifier = Modifier
                .alpha(0.8f)
                .drawWithContent {
                  drawContent()
                  drawRect(shimmerBrush, blendMode = BlendMode.SrcIn)
                }
                .sizeIn(maxHeight = articleGridColumnMinWidth),
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize(),
    state = articlesGridState,
  )
  // disable interactions with grid by placing a transparent interactable scrim on top
  val scrimInteractionSource = remember { MutableInteractionSource() }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .clickable(interactionSource = scrimInteractionSource, indication = null, onClick = {})
  )
}

@Composable
fun SelectableArticleThumbnailGrid(
    selectable: Boolean,
    onSelected: (index: Int) -> Unit,
    thumbnailUris: LazyUriStrings,
    selectedThumbnails: Set<Int>,
) {
  val articlesGridState = rememberLazyStaggeredGridState()
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = articleGridColumnMinWidth),
    content = {
      items(count = thumbnailUris.size) { thumbnailGridItemIndex ->
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
              .clickable { onSelected(thumbnailGridItemIndex) }
              .padding(articleGridItemPadding)
        ) {
          val uriString = thumbnailUris.getUriString(thumbnailGridItemIndex)
          SelectableNoopImage(
            uriString = uriString,
            contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
            selected = selectedThumbnails.contains(thumbnailGridItemIndex),
            selectable = selectable,
            modifier = Modifier.sizeIn(maxHeight = articleGridColumnMinWidth),
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize(),
    state = articlesGridState,
  )
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilArticleThumbnailGrid(selectable: Boolean) = NoopTheme {
  SelectableArticleThumbnailGrid(
    selectable = selectable,
    onSelected = {},
    thumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
  )
}

@Preview @Composable fun PreviewSelectableArticleThumbnailGrid_selectable() = PreviewUtilArticleThumbnailGrid(selectable = true)
@Preview @Composable fun PreviewSelectableArticleThumbnailGrid_notSelectable() = PreviewUtilArticleThumbnailGrid(selectable = false)
@LandscapePreview @Composable fun PreviewSelectableArticleThumbnailGrid_landscape() = PreviewUtilArticleThumbnailGrid(selectable = false)
@Preview @Composable fun PreviewPlaceholderArticleThumbnailGrid_notSelectable() = PlaceholderArticleThumbnailGrid()
@LandscapePreview @Composable fun PreviewPlaceholderArticleThumbnailGrid_landscape() = PlaceholderArticleThumbnailGrid()
//endregion