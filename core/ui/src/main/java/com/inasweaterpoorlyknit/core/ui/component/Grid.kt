package com.inasweaterpoorlyknit.core.ui.component

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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.LandscapePreview
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedPlaceholderDrawables
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

val staggeredGridColumnMinWidth = 90.dp
val staggeredGridItemPadding = 8.dp

@Composable
fun PlaceholderThumbnailGrid(modifier: Modifier = Modifier){
  val lazyGridState = rememberLazyStaggeredGridState()
  val shimmerBrush = shimmerBrush(color = MaterialTheme.colorScheme.onSurface)
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = staggeredGridColumnMinWidth),
    content = {
      items(count = repeatedPlaceholderDrawables.size) { thumbnailGridItemIndex ->
        val placeholderDrawable = repeatedPlaceholderDrawables[thumbnailGridItemIndex]
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(staggeredGridItemPadding)
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
                .sizeIn(maxHeight = staggeredGridColumnMinWidth),
          )
        }
      }
    },
    modifier = modifier.fillMaxSize(),
    state = lazyGridState,
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
fun SelectableStaggeredThumbnailGrid(
    selectable: Boolean,
    onSelected: (index: Int) -> Unit,
    thumbnailUris: LazyUriStrings,
    selectedThumbnails: Set<Int>,
) {
  val staggeredGridState = rememberLazyStaggeredGridState()
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = staggeredGridColumnMinWidth),
    content = {
      items(count = thumbnailUris.size) { thumbnailGridItemIndex ->
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
              .clickable { onSelected(thumbnailGridItemIndex) }
              .padding(staggeredGridItemPadding)
        ) {
          val uriString = thumbnailUris.getUriString(thumbnailGridItemIndex)
          SelectableNoopImage(
            uriString = uriString,
            contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
            selected = selectedThumbnails.contains(thumbnailGridItemIndex),
            selectable = selectable,
            modifier = Modifier.sizeIn(maxHeight = staggeredGridColumnMinWidth),
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize(),
    state = staggeredGridState,
  )
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilStaggeredThumbnailGrid(selectable: Boolean) = NoopTheme {
  SelectableStaggeredThumbnailGrid(
    selectable = selectable,
    onSelected = {},
    thumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
  )
}

@Preview @Composable fun PreviewSelectableStaggeredThumbnailGrid_selectable() = PreviewUtilStaggeredThumbnailGrid(selectable = true)
@Preview @Composable fun PreviewSelectableStaggeredThumbnailGrid_notSelectable() = PreviewUtilStaggeredThumbnailGrid(selectable = false)
@LandscapePreview @Composable fun PreviewSelectableStaggeredThumbnailGrid_landscape() = PreviewUtilStaggeredThumbnailGrid(selectable = false)
@Preview @Composable fun PreviewPlaceholderStaggeredThumbnailGrid_notSelectable() = PlaceholderThumbnailGrid()
@LandscapePreview @Composable fun PreviewPlaceholderStaggeredThumbnailGrid_landscape() = PlaceholderThumbnailGrid()
//endregion