package com.inasweaterpoorlyknit.inknit.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION

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