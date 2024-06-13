package com.inasweaterpoorlyknit.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
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
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.R
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.repeatedPlaceholderDrawables
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme

private val thumbnailsPadding = 10.dp
private val maxThumbnailSize = 70.dp
private val overlapPercentage = 0.4f
private val minRowHeight = thumbnailsPadding * 4
private val rowVerticalSpacing = 2.dp
private val rowVerticalPadding = 8.dp
private val rowStartPadding = 16.dp
private val rowEndPadding = 4.dp
private val overlapTitleSpacing = 4.dp

@Composable
private fun RowCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
  return ElevatedCard(
    modifier = modifier,
    shape = MaterialTheme.shapes.large,
    content = content
  )
}

@Composable
private fun RowText(text: String) = Text(text = text, color = MaterialTheme.colorScheme.primary)

@Composable
fun NoopOverlappingImageRow(
    title: String,
    lazyUriStrings: LazyUriStrings,
    modifier: Modifier = Modifier,
) {
  RowCard(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
          .heightIn(min = minRowHeight)
          .padding(start = rowStartPadding, end = rowEndPadding, top = rowVerticalPadding, bottom = rowVerticalPadding),
    ) {
      HorizontalOverlappingLayout(
        overlapPercentage = overlapPercentage,
      ) {
        repeat(lazyUriStrings.size) { index ->
          NoopImage(
            uriString = lazyUriStrings.getUriString(index),
            contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
            modifier = Modifier.sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
          )
        }
      }
      if(title.isNotEmpty()) {
        if(lazyUriStrings.isNotEmpty()){
          Spacer(modifier = Modifier.height(overlapTitleSpacing))
        }
        RowText(text = title)
      }
    }
  }
}

@Composable
fun NoopOverlappingImageRowColumn(
    lazyTitleAndUriStrings: List<Pair<String, LazyUriStrings>>,
    onClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyColumn(
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    items(lazyTitleAndUriStrings.size) { index ->
      val (rowTitle, lazyStrings) = lazyTitleAndUriStrings[index]
      NoopOverlappingImageRow(
        title = rowTitle,
        lazyUriStrings = lazyStrings,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
              top = if(index != 0) rowVerticalSpacing else 0.dp,
              bottom = if(index != lazyTitleAndUriStrings.lastIndex) rowVerticalSpacing else 0.dp,
            )
            .clickable { onClick(index) }
      )
    }
  }
}

@Composable
fun NoopOverlappingPlaceholderRow(
    drawables: List<Int>,
    title: String,
    modifier: Modifier = Modifier,
) {
  val shimmerBrush = shimmerBrush(color = MaterialTheme.colorScheme.onSurface)
  RowCard(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
          .heightIn(min = minRowHeight)
          .padding(start = rowStartPadding, end = rowEndPadding, top = rowVerticalPadding, bottom = rowVerticalPadding),
    ) {
      HorizontalOverlappingLayout(
        modifier = Modifier
            .alpha(0.8f)
            .drawWithContent {
              drawContent()
              drawRect(shimmerBrush, blendMode = BlendMode.SrcIn)
            },
        overlapPercentage = overlapPercentage,
      ) {
        repeat(drawables.size) { index ->
          Icon(
            painter = painterResource(drawables[index]),
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
            modifier = Modifier
                .sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize),
          )
        }
      }
      Spacer(modifier = Modifier.height(overlapTitleSpacing))
      RowText(text = title)
    }
  }
}

private val drawablePlaceholders: List<Pair<Int, List<Int>>> =
    repeatedPlaceholderDrawables.let { thumbnails ->
      listOf(
        Pair(R.string.goth_2_boss, thumbnails.slice(0..5)),
        Pair(R.string.sporty_spice, thumbnails.slice(6..12)),
        Pair(R.string.derelicte, thumbnails.slice(1..13)),
        Pair(R.string.bowie_nite, thumbnails.slice(3..5)),
        Pair(R.string.road_warrior, thumbnails.slice(5..11)),
        Pair(R.string.chrome_country, thumbnails.slice(7..11)),
        Pair(R.string.rain_steam_and_speed, thumbnails.slice(12..17)),
        Pair(R.string.joseph_mallord_william_turner, thumbnails.slice(11..15)),
      )
    }

@Composable
fun NoopOverlappingPlaceholderRowColumn(
    modifier: Modifier = Modifier,
){
  LazyColumn(
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    items(drawablePlaceholders.size) { index ->
      val ensembles = drawablePlaceholders[index]
      NoopOverlappingPlaceholderRow(
        drawables = ensembles.second,
        title = stringResource(ensembles.first),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
              top = if(index != 0) rowVerticalSpacing else 0.dp,
              bottom = if(index != drawablePlaceholders.lastIndex) rowVerticalSpacing else 0.dp,
            )
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
val previewEnsembles: List<Pair<String, LazyUriStrings>> =
    repeatedThumbnailResourceIdsAsStrings.let { thumbnails ->
      listOf(
        thumbnails.slice(4..4),
        thumbnails.slice(0..5),
        thumbnails.slice(6..18),
        thumbnails.slice(1..12),
        emptyList(),
        emptyList(),
        thumbnails.slice(3..5),
        thumbnails.slice(5..11),
        thumbnails.slice(7..11),
        thumbnails.slice(12..17),
      ).mapIndexed { index, thumbnailUriStrings ->
        Pair(
          if(index == 3 || index == 4) "" else "Row ${index + 1}",
          object : LazyUriStrings {
            override val size: Int = thumbnailUriStrings.size
            private val articleThumbnailPaths: List<String> = thumbnailUriStrings
            override fun getUriString(index: Int): String = articleThumbnailPaths[index]
          }
        )
      }
    }

@Preview
@Composable
fun PreviewNoopOverlappingImageRow() = NoopTheme(darkMode = DarkMode.DARK) {
  NoopOverlappingImageRow(
    title = "Road Warrior",
    lazyUriStrings = previewEnsembles[1].second,
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
fun PreviewNoopOverlappingImageRowOverflow() = NoopTheme {
  NoopOverlappingImageRow(
    title = "Road Warrior",
    lazyUriStrings = previewEnsembles[2].second,
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
fun PreviewNoopOverlappingPlaceholderRow() = NoopTheme(darkMode = DarkMode.DARK) {
  NoopOverlappingPlaceholderRow(
    title = "Road Warrior",
    drawables = drawablePlaceholders[1].second,
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
fun PreviewNoopOverlappingPlaceholderRowOverflow() = NoopTheme {
  NoopOverlappingPlaceholderRow(
    title = "Road Warrior",
    drawables = drawablePlaceholders[2].second,
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
fun PreviewNoopOverlappingImageRowColumn() = NoopTheme(darkMode = DarkMode.DARK) {
  NoopOverlappingImageRowColumn(
    lazyTitleAndUriStrings = previewEnsembles,
    onClick = {}
  )
}

@Preview
@Composable
fun PreviewNoopOverlappingPlaceholderRowColumn() = NoopTheme {
  NoopOverlappingPlaceholderRowColumn()
}
//endregion