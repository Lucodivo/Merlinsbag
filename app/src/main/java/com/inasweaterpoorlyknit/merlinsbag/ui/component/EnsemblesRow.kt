package com.inasweaterpoorlyknit.merlinsbag.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.data.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme

@Composable
fun EnsemblesRow(
    lazyEnsembleThumbnails: LazyEnsembleThumbnails,
    modifier: Modifier = Modifier,
) {
  val thumbnailsPadding = 10.dp
  val maxThumbnailSize = 80.dp
  val titleVerticalPadding = 5.dp
  val overlapPercentage = 0.4f
  val minRowHeight = thumbnailsPadding * 4
  Card(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
      modifier = Modifier.heightIn(min = minRowHeight),
    ) {
      HorizontalOverlappingLayout(
        modifier = Modifier.padding(horizontal = thumbnailsPadding),
        overlapPercentage = overlapPercentage,
      ) {
        repeat(lazyEnsembleThumbnails.thumbnails.size) { index ->
          NoopImage(
            uriString = lazyEnsembleThumbnails.thumbnails.getUriString(index),
            contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
            modifier = Modifier
                .sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
                .padding(vertical = thumbnailsPadding)
          )
        }
      }
      if(lazyEnsembleThumbnails.title.isNotEmpty()) {
        Text(
          text = lazyEnsembleThumbnails.title,
          modifier = Modifier.padding(
            top = 0.dp,
            end = thumbnailsPadding,
            start = thumbnailsPadding,
            bottom = if(lazyEnsembleThumbnails.thumbnails.isEmpty()) 0.dp else titleVerticalPadding
          )
        )
      }
    }
  }
}

@Composable
fun EnsemblesColumn(
    lazyEnsembleThumbnails: List<LazyEnsembleThumbnails>,
    onClickEnsemble: (id: String) -> Unit,
) {
  val sidePadding = 10.dp
  val ensembleSpacing = 3.dp
  LazyColumn(
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(horizontal = sidePadding),
    modifier = Modifier.fillMaxWidth()
  ) {
    items(lazyEnsembleThumbnails.size) { index ->
      val topPadding = if(index == 0) sidePadding else ensembleSpacing
      val bottomPadding = if(index == lazyEnsembleThumbnails.lastIndex) sidePadding else ensembleSpacing
      val ensembles = lazyEnsembleThumbnails[index]
      EnsemblesRow(
        lazyEnsembleThumbnails = ensembles,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = bottomPadding)
            .clickable { onClickEnsemble(ensembles.id) }
      )
    }
  }
}

@Composable
fun EnsemblePlaceholderRow(
    ensembleDrawables: List<Int>,
    title: String,
    modifier: Modifier = Modifier,
) {
  val thumbnailsPadding = 10.dp
  val maxThumbnailSize = 80.dp
  val titleVerticalPadding = 5.dp
  val overlapPercentage = 0.4f
  val minRowHeight = thumbnailsPadding * 4
  val shimmerBrush = shimmerBrush(color = MaterialTheme.colorScheme.onSurface)
  Card(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
      modifier = Modifier.heightIn(min = minRowHeight),
    ) {
      HorizontalOverlappingLayout(
        modifier = Modifier
            .padding(horizontal = thumbnailsPadding)
            .alpha(0.8f)
            .drawWithContent {
              drawContent()
              drawRect(shimmerBrush, blendMode = BlendMode.SrcIn)
            },
        overlapPercentage = overlapPercentage,
      ) {
        repeat(ensembleDrawables.size) { index ->
          Icon(
            painter = painterResource(ensembleDrawables[index]),
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
            modifier = Modifier
                .sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
                .padding(vertical = thumbnailsPadding)
          )
        }
      }
      Text(
        text = title,
        modifier = Modifier.padding(
          end = thumbnailsPadding,
          start = thumbnailsPadding,
          bottom = titleVerticalPadding
        )
      )
    }
  }
}

@Composable
fun EnsemblesPlaceholderColumn(
    modifier: Modifier = Modifier,
){
  val sidePadding = 10.dp
  val ensembleSpacing = 3.dp
  LazyColumn(
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    contentPadding = PaddingValues(horizontal = sidePadding),
    modifier = modifier.fillMaxSize()
  ) {
    items(previewEnsemblesPlaceholders.size) { index ->
      val topPadding = if(index == 0) sidePadding else ensembleSpacing
      val bottomPadding = if(index == previewEnsembles.lastIndex) sidePadding else ensembleSpacing
      val ensembles = previewEnsemblesPlaceholders[index]
      EnsemblePlaceholderRow(
        ensembleDrawables = ensembles.second,
        title = stringResource(ensembles.first),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = bottomPadding)
      )
    }
  }
  // disable interactions with column by placing a transparent interactable scrim on top
  val scrimInteractionSource = remember { MutableInteractionSource() }
  Box(
    modifier = Modifier
        .fillMaxSize()
        .clickable(interactionSource = scrimInteractionSource, indication = null, onClick = {})
  )
}

//region COMPOSABLE PREVIEWS
val previewEnsembles: List<LazyEnsembleThumbnails> =
    repeatedThumbnailResourceIdsAsStrings.let { thumbnails ->
      listOf(
        thumbnails.slice(4..4),
        thumbnails.slice(0..5),
        thumbnails.slice(6..16),
        thumbnails.slice(1..12),
        emptyList(),
        emptyList(),
        thumbnails.slice(3..5),
        thumbnails.slice(5..11),
        thumbnails.slice(7..11),
        thumbnails.slice(12..17),
      ).mapIndexed { index, thumbnailUriStrings ->
        LazyEnsembleThumbnails(
          id = index.toString(),
          title = if(index == 3 || index == 4) "" else "Ensemble ${index + 1}",
          thumbnails =
          LazyArticleThumbnails("",
            articleThumbnailPaths = thumbnailUriStrings.mapIndexed { i, it ->
              ArticleWithThumbnails(articleId = i.toString(), thumbnailPaths = listOf(ThumbnailFilename(filenameThumb = it)))
            }
          )
        )
      }
    }
val previewEnsemblesPlaceholders: List<Pair<Int, List<Int>>> =
    repeatedPlaceholderDrawables.let { thumbnails ->
      listOf(
        Pair(R.string.Goth_2_Boss, thumbnails.slice(0..5)),
        Pair(R.string.sporty_spice, thumbnails.slice(6..12)),
        Pair(R.string.derelicte, thumbnails.slice(1..10)),
        Pair(R.string.bowie_nite, thumbnails.slice(3..5)),
        Pair(R.string.road_warrior, thumbnails.slice(5..11)),
        Pair(R.string.chrome_country, thumbnails.slice(7..11)),
        Pair(R.string.rain_steam_and_speed, thumbnails.slice(12..17)),
        Pair(R.string.joseph_mallord_william_turner, thumbnails.slice(11..15)),
      )
    }

@Preview
@Composable
fun PreviewEnsembleRows() = NoopTheme {
  EnsemblesColumn(
    lazyEnsembleThumbnails = previewEnsembles,
    onClickEnsemble = {}
  )
}

@Preview
@Composable
fun PreviewEnsemblePlaceholder() = NoopTheme {
  EnsemblesPlaceholderColumn()
}
//endregion