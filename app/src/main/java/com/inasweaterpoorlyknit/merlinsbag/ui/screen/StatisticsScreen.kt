package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.annotation.StringRes
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
  )
}

@Composable
fun StatisticsRow(
    @StringRes title: Int,
    value: String,
){
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
  ){
    Text(text = "${stringResource(title)}: $value")
  }
}

@Composable
fun StatisticsScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    articleCount: Int,
    ensembleCount: Int,
){
  val layoutDir = LocalLayoutDirection.current
  val padding = 16.dp
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(
      start = systemBarPaddingValues.calculateStartPadding(layoutDir) + padding,
      end = systemBarPaddingValues.calculateEndPadding(layoutDir) + padding,
    ),
  ) {
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding() + padding)) }
    item { StatisticsRow(R.string.article_count, articleCount.toString()) }
    item { StatisticsRow(R.string.ensemble_count, ensembleCount.toString()) }
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding() + padding)) }
  }
}

//region COMPOSABLE PREVIEWS
@Preview
@Composable
fun PreviewStatisticsScreen() = StatisticsScreen(
  articleCount = 12_345,
  ensembleCount = 67_890,
)
//endregion