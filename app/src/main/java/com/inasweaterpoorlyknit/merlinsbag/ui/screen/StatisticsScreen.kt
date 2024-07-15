package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.database.model.EnsembleArticleCount
import com.inasweaterpoorlyknit.core.ui.ARTICLE_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.allTestThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.component.NoopImage
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.StatisticsViewModel

const val STATISTICS_ROUTE = "statistics_route"

fun NavController.navigateToStatistics(navOptions: NavOptions? = null) = navigate(STATISTICS_ROUTE, navOptions)

@Composable
fun StatisticsRoute(
    navController: NavController,
    statisticsViewModel: StatisticsViewModel = hiltViewModel(),
) {
  val articleCount by statisticsViewModel.articleCount.collectAsStateWithLifecycle()
  val articleImageCount by statisticsViewModel.articleImagesCount.collectAsStateWithLifecycle()
  val ensembleCount by statisticsViewModel.ensembleCount.collectAsStateWithLifecycle()
  val popularEnsembles by statisticsViewModel.topEnsembles.collectAsStateWithLifecycle()
  val topArticleMostEnsemblesCount by statisticsViewModel.topArticleMostEnsemblesCount.collectAsStateWithLifecycle()
  val topArticleMostImagesUriStrings by statisticsViewModel.topArticleMostImagesCount.collectAsStateWithLifecycle()

  StatisticsScreen(
    articleCount = articleCount,
    articleImageCount = articleImageCount,
    ensembleCount = ensembleCount,
    popularEnsembles = popularEnsembles,
    topArticleMostEnsemblesCount = topArticleMostEnsemblesCount,
    topArticleMostImagesUriStrings = topArticleMostImagesUriStrings,
  )
}

@Composable
fun StatisticsRow(
    text: String,
    modifier: Modifier = Modifier,
){
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = modifier.fillMaxWidth()
  ){
    Text(text = text)
  }
}

@Composable
fun StatisticsSlideShow(uriStrings: List<String>, modifier: Modifier) {
  val thumbnailSize = 80.dp
  var animateTopImagesIndex by remember { mutableStateOf(false) }
  val animateIndex = animateIntAsState(
    targetValue =
    if(!animateTopImagesIndex) 0
    else if(uriStrings.isNotEmpty()) uriStrings.lastIndex
    else 0,
    label = "TopArticleMostImages",
    animationSpec = repeatable(
      iterations = Int.MAX_VALUE,
      animation = tween(
        durationMillis = 3000 * uriStrings.size,
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    )
  )
  LaunchedEffect(Unit) { animateTopImagesIndex = true }
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier.size(width = thumbnailSize, height = thumbnailSize)
  ){
    NoopImage(
      uriString = uriStrings[animateIndex.value],
      contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
      crossFadeMs = 0,
      modifier = Modifier
          .sizeIn(maxWidth = thumbnailSize, maxHeight = thumbnailSize)
    )
  }
}

@Composable
fun StatisticsScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    articleCount: Int,
    articleImageCount: Int,
    ensembleCount: Int,
    popularEnsembles: List<EnsembleArticleCount>,
    topArticleMostEnsemblesCount: Pair<Int, List<String>>,
    topArticleMostImagesUriStrings: List<String>,
){
  val layoutDir = LocalLayoutDirection.current
  val padding = 16.dp
  val sublistIndentation = 32.dp
  val subListModifier = Modifier.padding(start = sublistIndentation)
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(
      start = systemBarPaddingValues.calculateStartPadding(layoutDir) + padding,
      end = systemBarPaddingValues.calculateEndPadding(layoutDir) + padding,
    ),
  ) {
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding() + padding)) }
    item { StatisticsRow("${stringResource(R.string.article_count)}: $articleCount") }
    item { StatisticsRow("${stringResource(R.string.article_image_count)}: $articleImageCount") }
    item { StatisticsRow("${stringResource(R.string.ensemble_count)}: $ensembleCount") }
    item { StatisticsRow("${stringResource(R.string.top_articles)}:") }
    item { StatisticsRow("${stringResource(R.string.most_ensembles)}: ${topArticleMostEnsemblesCount.first}", modifier = subListModifier) }
    if(topArticleMostImagesUriStrings.isNotEmpty()){
      item{
        StatisticsSlideShow(
          uriStrings = topArticleMostEnsemblesCount.second,
          modifier = subListModifier.padding(start = sublistIndentation)
        )
      }
    }
    item { StatisticsRow("${stringResource(R.string.most_images)}: ${topArticleMostImagesUriStrings.size}", modifier = subListModifier) }
    if(topArticleMostImagesUriStrings.isNotEmpty()){
      item{
        StatisticsSlideShow(
          uriStrings = topArticleMostImagesUriStrings,
          modifier = subListModifier.padding(start = sublistIndentation)
        )
      }
    }
    item { StatisticsRow("${stringResource(R.string.top_ensembles)}:") }
    if(popularEnsembles.isNotEmpty()){
      popularEnsembles.forEach { popularEnsemble ->
        item { StatisticsRow("${popularEnsemble.title}: ${popularEnsemble.count}", modifier = subListModifier) }
      }
    } else {
      item { StatisticsRow("[${stringResource(R.string.no_ensembles_available)}]", modifier = subListModifier) }
    }
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateBottomPadding() + padding)) }
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilStatisticsScreen(popularEnsembles: List<EnsembleArticleCount>) = NoopTheme {
  StatisticsScreen(
    articleCount = 12_345,
    articleImageCount = 300_000,
    ensembleCount = 67_890,
    popularEnsembles = popularEnsembles,
    topArticleMostEnsemblesCount = Pair(allTestThumbnailResourceIdsAsStrings.size, allTestThumbnailResourceIdsAsStrings.toList()),
    topArticleMostImagesUriStrings = allTestThumbnailResourceIdsAsStrings.toList(),
  )
}


@Preview
@Composable
fun PreviewStatisticsScreen() = PreviewUtilStatisticsScreen(
    popularEnsembles = listOf(
      EnsembleArticleCount(title = "Goth 2 Boss", 12),
      EnsembleArticleCount(title = "Hiking", 8),
      EnsembleArticleCount(title = "Vinyl", 4),
    ),
  )

@Preview
@Composable
fun PreviewStatisticsScreen_NoEnsembleArticles() = PreviewUtilStatisticsScreen(popularEnsembles = emptyList())
//endregion