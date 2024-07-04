package com.inasweaterpoorlyknit.merlinsbag.ui.screen

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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.database.model.EnsembleCount
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.StatisticsViewModel

// TODO: Might serve better as a simple pop-up dialog. Depends how expanded the statistics get

const val STATISTICS_ROUTE = "statistics_route"

fun NavController.navigateToStatistics(navOptions: NavOptions? = null) = navigate(STATISTICS_ROUTE, navOptions)

@Composable
fun StatisticsRoute(
    navController: NavController,
    statisticsViewModel: StatisticsViewModel = hiltViewModel(),
) {
  val statisticsUiState by statisticsViewModel.statisticsUiState.collectAsStateWithLifecycle()
  StatisticsScreen(
    articleCount = statisticsUiState.articleCount,
    ensembleCount = statisticsUiState.ensembleCount,
    popularEnsembles = statisticsUiState.topEnsembles,
    topArticleDecorationCount = statisticsUiState.topArticleDecorationCount,
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
fun StatisticsScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    articleCount: Int,
    ensembleCount: Int,
    popularEnsembles: List<EnsembleCount>,
    topArticleDecorationCount: Long,
){
  val layoutDir = LocalLayoutDirection.current
  val padding = 16.dp
  val popularEnsemblesModifier = Modifier.padding(start = 32.dp)
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(
      start = systemBarPaddingValues.calculateStartPadding(layoutDir) + padding,
      end = systemBarPaddingValues.calculateEndPadding(layoutDir) + padding,
    ),
  ) {
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding() + padding)) }
    item { StatisticsRow("${stringResource(R.string.article_count)}: $articleCount") }
    item { StatisticsRow("${stringResource(R.string.ensemble_count)}: $ensembleCount") }
    item { StatisticsRow("${stringResource(R.string.most_decorated_article)}: $topArticleDecorationCount") }
    item { StatisticsRow("${stringResource(R.string.top_ensembles)}:") }
    if(popularEnsembles.isNotEmpty()){
      popularEnsembles.forEach { popularEnsemble ->
        item { StatisticsRow("${popularEnsemble.title}: ${popularEnsemble.count}", modifier = popularEnsemblesModifier) }
      }
    } else {
      item { StatisticsRow("[${stringResource(R.string.no_ensembles_available)}]", modifier = popularEnsemblesModifier) }
    }
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding() + padding)) }
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilStatisticsScreen(popularEnsembles: List<EnsembleCount>) = NoopTheme {
  StatisticsScreen(
    articleCount = 12_345,
    ensembleCount = 67_890,
    popularEnsembles = popularEnsembles,
    topArticleDecorationCount = 15
  )
}


@Preview
@Composable
fun PreviewStatisticsScreen() = PreviewUtilStatisticsScreen(
    popularEnsembles = listOf(
      EnsembleCount(title = "Goth 2 Boss", 12),
      EnsembleCount(title = "Hiking", 8),
      EnsembleCount(title = "Vinyl", 4),
    ),
  )

@Preview
@Composable
fun PreviewStatisticsScreen_NoEnsembleArticles() = PreviewUtilStatisticsScreen(popularEnsembles = emptyList())
//endregion