package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.viewmodels.AddArticleViewModel

const val ENSEMBLE_ID_ARG = "ensembleId"
const val ENSEMBLE_DETAIL_ROUTE_BASE = "ensembles_route"
const val ENSEMBLE_DETAIL_ROUTE = "$ENSEMBLE_DETAIL_ROUTE_BASE?$ENSEMBLE_ID_ARG={$ENSEMBLE_ID_ARG}"

fun NavController.navigateToEnsembleDetail(ensembleId: String, navOptions: NavOptions? = null){
  val route = "${ENSEMBLE_DETAIL_ROUTE_BASE}?${ENSEMBLE_ID_ARG}=$ensembleId"
  navigate(route, navOptions)
}

@Composable
fun EnsembleDetailRoute(
  navController: NavController,
  ensembleId: String,
  modifier: Modifier = Modifier,
){
  val ensembleDetailViewModel =
    hiltViewModel<EnsembleDetailViewModel, EnsembleDetailViewModel.EnsembleDetailViewModelFactory> { factory ->
      factory.create(ensembleId)
    }
  val ensembleUiState by ensembleDetailViewModel.ensembleUiState.collectAsStateWithLifecycle()
  EnsembleDetailScreen(
    ensembleUiState.title,
    ensembleUiState.thumbnailUriStrings,
    modifier = modifier,
  )
}

@Composable
fun EnsembleDetailScreen(
  title: String,
  thumbnailUriStrings: List<String>,
  modifier: Modifier = Modifier,
){
  Surface(
    modifier = modifier.fillMaxSize()
  ) {
    Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
    ){
      Text(
        text = title,
        textAlign = TextAlign.Center,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        modifier = Modifier.padding(20.dp)
      )
    }
  }
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen(){
  NoopTheme{
    EnsembleDetailScreen(
      title = "Ensemble Title",
      thumbnailUriStrings = emptyList(),
    )
  }
}