package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.StatisticsUIState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.StatisticsNoopViewModel
import kotlinx.serialization.Serializable
import staggeredHorizontallyAnimatedComposables
import kotlin.math.min

@Serializable
object StatisticsRouteArgs

fun NavController.navigateToStatistics(navOptions: NavOptions? = null) = navigate(StatisticsRouteArgs, navOptions)

@Composable
fun StatisticsRoute(statisticsViewModel: StatisticsNoopViewModel = hiltViewModel()) {
  val uiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()
  StatisticsScreen(uiState = uiState)
}

@Composable
fun StatisticsRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
){
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = modifier.fillMaxWidth()
  ) {
    Text(title)
    Text(value)
  }
}

@Composable
fun StatisticsSlideShow(uriStrings: List<String>, modifier: Modifier) {
  val thumbnailSize = 120.dp
  val lastIndex = uriStrings.size - 1
  var targetValue by remember { mutableStateOf(0) }
  val animateIndex by animateIntAsState(
    targetValue = targetValue,
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
  LaunchedEffect(Unit) { if(uriStrings.isNotEmpty()) targetValue = lastIndex + 1}
  NoopImage(
    uriString = if(animateIndex < uriStrings.size) uriStrings[min(animateIndex, lastIndex)] else null,
    contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
    crossFadeMs = 0,
    modifier = modifier
        .padding(vertical = 8.dp)
        .size(width = thumbnailSize, height = thumbnailSize)
  )
}

@Composable
fun StatisticsScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    uiState: StatisticsUIState,
){

  val layoutDir = LocalLayoutDirection.current
  val padding = 32.dp
  val subListModifier = Modifier.padding(start = padding)


  val items = when(uiState) {
    StatisticsUIState.Loading -> emptyList()
    is StatisticsUIState.Success -> staggeredHorizontallyAnimatedComposables(
      content = mutableListOf<@Composable AnimatedVisibilityScope.() -> Unit>(
        { StatisticsRow(stringResource(R.string.article_count), uiState.articleCount.toString()) },
        { StatisticsRow(stringResource(R.string.article_image_count), uiState.articleImageCount.toString()) },
        { StatisticsRow(stringResource(R.string.ensemble_count), uiState.ensembleCount.toString()) },
      ).apply {
        val showMostEnsembles = uiState.articleWithMostEnsembles.count > 0
        val showMostImages = uiState.articleWithMostImagesUriStrings.isNotEmpty()
        val showTopArticles = showMostEnsembles || showMostImages
        val showTopEnsembles = uiState.ensemblesWithMostArticles.isNotEmpty()

        if(showTopArticles) {
          add { StatisticsRow(stringResource(R.string.top_articles), "") }
          if(showMostEnsembles) {
            add { StatisticsRow(stringResource(R.string.most_ensembles), uiState.articleWithMostEnsembles.count.toString(), modifier = subListModifier) }
            add { StatisticsSlideShow(uriStrings = uiState.articleWithMostEnsembles.uriStrings, modifier = subListModifier.padding(start = padding)) }
          }
          if(showMostImages) {
            add { StatisticsRow(stringResource(R.string.most_images), uiState.articleWithMostImagesUriStrings.size.toString(), modifier = subListModifier) }
            add { StatisticsSlideShow(uriStrings = uiState.articleWithMostImagesUriStrings, modifier = subListModifier.padding(start = padding)) }
          }
        }

        if(showTopEnsembles) {
          add { StatisticsRow(stringResource(R.string.top_ensembles), "") }
          addAll(uiState.ensemblesWithMostArticles.map { popularEnsemble ->
            { StatisticsRow(popularEnsemble.title, popularEnsemble.count.toString(), modifier = subListModifier) }
          })
        }
      }
    )
  }
  LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(
          start = systemBarPaddingValues.calculateStartPadding(layoutDir) + padding,
          end = systemBarPaddingValues.calculateEndPadding(layoutDir) + padding,
        ),
  ) {
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding() + padding)) }
    items(items.size){ index -> items[index]() }
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateBottomPadding() + padding)) }
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilStatisticsScreen(popularEnsembles: List<EnsembleArticleCount>) = NoopTheme {
  StatisticsScreen(
    uiState = StatisticsUIState.Success(
      articleCount = 12_345,
      articleImageCount = 300_000,
      ensembleCount = 67_890,
      ensemblesWithMostArticles = popularEnsembles,
      articleWithMostEnsembles = StatisticsUIState.ArticleWithMostEnsembles(allTestThumbnailResourceIdsAsStrings.size, allTestThumbnailResourceIdsAsStrings.toList()),
      articleWithMostImagesUriStrings = allTestThumbnailResourceIdsAsStrings.toList(),
    )
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