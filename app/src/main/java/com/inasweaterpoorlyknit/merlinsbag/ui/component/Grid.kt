package com.inasweaterpoorlyknit.merlinsbag.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.LandscapePreview
import com.inasweaterpoorlyknit.merlinsbag.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme

@Composable
fun ArticleThumbnailGrid(
  articleThumbnailUris: List<String>,
  onClickArticle: (index: Int) -> Unit,
){
  val gridMinWidth = 100.dp
  val gridItemPadding = 16.dp
  val articlesGridState = rememberLazyStaggeredGridState()
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = gridMinWidth),
    content = {
      items(count = articleThumbnailUris.size){ thumbnailGridItemIndex ->
        NoopImage(
          uriString = articleThumbnailUris[thumbnailGridItemIndex],
          contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
          modifier = Modifier
            .padding(gridItemPadding)
            .clickable { onClickArticle(thumbnailGridItemIndex) }
            .fillMaxSize(),
        )
      }
    },
    modifier = Modifier.fillMaxSize(),
    state = articlesGridState,
  )
}

@Composable
fun SelectableArticleThumbnailGrid(
  selectable: Boolean,
  onSelected: (index: Int) -> Unit,
  thumbnailUris: LazyUriStrings,
  selectedThumbnails: Set<Int>,
){
  val gridMinWidth = 90.dp
  val gridItemPadding = 8.dp
  val articlesGridState = rememberLazyStaggeredGridState()
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = gridMinWidth),
    content = {
      items(count = thumbnailUris.size){ thumbnailGridItemIndex ->
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .clickable { onSelected(thumbnailGridItemIndex) }
            .padding(gridItemPadding)
        ) {
          val uriString = thumbnailUris.getUriString(thumbnailGridItemIndex)
          SelectableNoopImage(
            uriString = uriString,
            contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
            selected = selectedThumbnails.contains(thumbnailGridItemIndex),
            selectable = selectable,
            modifier = Modifier.sizeIn(maxWidth = gridMinWidth),
          )
        }
      }
    },
    modifier = Modifier.fillMaxSize(),
    state = articlesGridState,
  )
}

@Composable
fun __PreviewArticleThumbnailGrid(selectable: Boolean) = NoopTheme {
  SelectableArticleThumbnailGrid(
    selectable = selectable,
    onSelected = {},
    thumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
  )
}
@Preview @Composable fun PreviewSelectableArticleThumbnailGrid_selectable() = __PreviewArticleThumbnailGrid(selectable = true)
@Preview @Composable fun PreviewSelectableArticleThumbnailGrid_notSelectable() = __PreviewArticleThumbnailGrid(selectable = false)
@LandscapePreview @Composable fun PreviewSelectableArticleThumbnailGrid_landscape() = __PreviewArticleThumbnailGrid(selectable = false)
