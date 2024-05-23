package com.inasweaterpoorlyknit.inknit.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme

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
  thumbnailUris: List<String>,
  selectedThumbnails: Set<Int>,
){
  val gridMinWidth = 100.dp
  val gridItemPadding = 16.dp
  val articlesGridState = rememberLazyStaggeredGridState()
  LazyVerticalStaggeredGrid(
    // typical dp width of a smart phone is 320dp-480dp
    columns = StaggeredGridCells.Adaptive(minSize = gridMinWidth),
    content = {
      val gridItemModifier = Modifier
        .padding(gridItemPadding)
        .fillMaxSize()
      items(count = thumbnailUris.size){ thumbnailGridItemIndex ->
        Box(contentAlignment = Alignment.Center) {
          SelectableNoopImage(
            uriString = thumbnailUris[thumbnailGridItemIndex],
            contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
            selected = selectedThumbnails.contains(thumbnailGridItemIndex),
            selectable = selectable,
            modifier = gridItemModifier.clickable { onSelected(thumbnailGridItemIndex) },

          )
        }
      }
    },
    modifier = Modifier.fillMaxSize(),
    state = articlesGridState,
  )
}

@Preview
@Composable
fun PreviewSelectableArticleThumbnailGrid_selectable(
){
  NoopTheme{
    SelectableArticleThumbnailGrid(
      selectable = true,
      onSelected = {},
      thumbnailUris = repeatedThumbnailResourceIdsAsStrings,
      selectedThumbnails = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet(),
    )
  }
}

@Preview
@Composable
fun PreviewSelectableArticleThumbnailGrid_notSelectable(
){
  NoopTheme{
    SelectableArticleThumbnailGrid(selectable = false, onSelected = {}, thumbnailUris = repeatedThumbnailResourceIdsAsStrings, selectedThumbnails = emptySet())
  }
}
