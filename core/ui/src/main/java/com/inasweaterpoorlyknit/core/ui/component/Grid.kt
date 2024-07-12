@file:OptIn(ExperimentalFoundationApi::class)

package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.ARTICLE_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.LandscapePreview
import com.inasweaterpoorlyknit.core.ui.R
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedPlaceholderDrawables
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

val staggeredGridColumnMinWidth = 90
val staggeredGridColumnMinWidthDp = staggeredGridColumnMinWidth.dp
val staggeredGridColumnMaxHeightDp = (staggeredGridColumnMinWidth.toFloat() * 1.5f).toInt().dp
val staggeredGridItemPadding = 4.dp

@Composable
fun PlaceholderThumbnailGrid(modifier: Modifier = Modifier){
  val shimmerBrush = shimmerBrush(color = MaterialTheme.colorScheme.onSurface)
  Box(modifier = Modifier.fillMaxSize()) {
    val placeholderContentDescription = stringResource(R.string.placeholder_article_thumbnail)
    NoopVerticalStaggeredGrid(modifier = modifier.semantics { contentDescription = placeholderContentDescription }) {
      items(count = repeatedPlaceholderDrawables.size) { thumbnailGridItemIndex ->
        val placeholderDrawable = repeatedPlaceholderDrawables[thumbnailGridItemIndex]
        Box(contentAlignment = Alignment.Center) {
          Icon(
            painter = painterResource(placeholderDrawable),
            contentDescription = REDUNDANT_CONTENT_DESCRIPTION,
            modifier = Modifier
                .alpha(0.8f)
                .drawWithContent {
                  drawContent()
                  drawRect(shimmerBrush, blendMode = BlendMode.SrcIn)
                }
                .sizeIn(maxHeight = staggeredGridColumnMaxHeightDp),
          )
        }
      }
    }

    // disable interactions with grid by placing a transparent interactable scrim on top
    val scrimInteractionSource = remember { MutableInteractionSource() }
    Box(
      modifier = Modifier
          .fillMaxSize()
          .clickable(interactionSource = scrimInteractionSource, indication = null, onClick = {})
    )
  }
}

@Composable
fun NoopVerticalStaggeredGrid(
    modifier: Modifier = Modifier,
    content: LazyStaggeredGridScope.() -> Unit
){
  val staggeredGridState = rememberLazyStaggeredGridState()
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = staggeredGridColumnMinWidthDp),
    modifier = modifier.fillMaxSize(),
    state = staggeredGridState,
    verticalItemSpacing = staggeredGridItemPadding,
    horizontalArrangement = Arrangement.spacedBy(staggeredGridItemPadding),
    content = content
  )
}

@Composable
fun SelectableStaggeredThumbnailGrid(
    selectable: Boolean,
    onSelect: (index: Int) -> Unit,
    onLongSelect: (index: Int) -> Unit,
    thumbnailUris: LazyUriStrings,
    selectedThumbnails: Set<Int>,
) {
  NoopVerticalStaggeredGrid {
    items(count = thumbnailUris.size) { thumbnailGridItemIndex ->
        val uriString = thumbnailUris.getUriStrings(thumbnailGridItemIndex)
        SelectableNoopImage(
          uriString = uriString.first(), // TODO: Animate between items?
          contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
          selected = selectedThumbnails.contains(thumbnailGridItemIndex),
          selectable = selectable,
          modifier = Modifier
              .combinedClickable(
                onClick = { onSelect(thumbnailGridItemIndex) },
                onLongClick = { onLongSelect(thumbnailGridItemIndex) }
              )
              .fillMaxWidth()
              .sizeIn(maxHeight = staggeredGridColumnMaxHeightDp),
        )
    }
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilStaggeredThumbnailGrid(selectable: Boolean) = NoopTheme {
  SelectableStaggeredThumbnailGrid(
    selectable = selectable,
    thumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
    onSelect = {}, onLongSelect = {},
  )
}

@Preview @Composable fun PreviewSelectableStaggeredThumbnailGrid_selectable() = PreviewUtilStaggeredThumbnailGrid(selectable = true)
@Preview @Composable fun PreviewSelectableStaggeredThumbnailGrid_notSelectable() = PreviewUtilStaggeredThumbnailGrid(selectable = false)
@LandscapePreview @Composable fun PreviewSelectableStaggeredThumbnailGrid_landscape() = PreviewUtilStaggeredThumbnailGrid(selectable = false)
@Preview @Composable fun PreviewPlaceholderStaggeredThumbnailGrid_notSelectable() = PlaceholderThumbnailGrid()
@LandscapePreview @Composable fun PreviewPlaceholderStaggeredThumbnailGrid_landscape() = PlaceholderThumbnailGrid()
//endregion