package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.component.ArticleThumbnailGrid
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.viewmodel.EnsembleDetailViewModel

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
  val (editingTitle, setEditingTitle) = remember { mutableStateOf(false) }
  EnsembleDetailScreen(
    title = ensembleUiState.title,
    editingTitle = editingTitle,
    articleThumbnailUris = ensembleUiState.articleThumbnailUris,
    onClickArticle = { i -> navController.navigateToArticleDetail(i, ensembleId) },
    onTitleClicked = { setEditingTitle(true) },
    onTitleChanged = { newTitle ->
      ensembleDetailViewModel.onTitleChanged(newTitle)
      setEditingTitle(false)
    },
    onAbandonEditTitle = { setEditingTitle(false) },
    modifier = modifier,
  )
}

@Composable
fun EnsembleDetailScreen(
  title: String,
  editingTitle: Boolean,
  articleThumbnailUris: List<String>,
  onClickArticle: (index: Int) -> Unit,
  onTitleClicked: () -> Unit,
  onTitleChanged: (String) -> Unit,
  onAbandonEditTitle: () -> Unit,
  modifier: Modifier = Modifier,
){
  Surface(
    modifier = modifier.fillMaxSize()
  ) {
    Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
    ){
      val titleRowInteractionSource = remember { MutableInteractionSource() }
      val outsideKeyboardRowInteractionSource = remember { MutableInteractionSource() }
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .height(height = 90.dp)
          .fillMaxWidth()
          .clickable(
            interactionSource = titleRowInteractionSource,
            indication = null,
            onClick = { if(!editingTitle) onTitleClicked() }
          ),
      ){
        val titleModifier = Modifier.fillMaxWidth()
        val titleAlign = TextAlign.Center
        val titleFontSize = MaterialTheme.typography.titleLarge.fontSize
        if(editingTitle) {
          val (editTitle, setEditTitle) = remember { mutableStateOf("") }
          val focusRequester = FocusRequester()
          OutlinedTextField(
            value = editTitle,
            placeholder = { Text(text = title) },
            onValueChange = setEditTitle,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.Ensemble_title)) },
            keyboardActions = KeyboardActions(onDone = { onTitleChanged(editTitle) }),
            modifier = titleModifier
              .padding(start = 20.dp, end = 20.dp)
              .focusRequester(focusRequester)
          )
          LaunchedEffect(Unit) { focusRequester.requestFocus() }
          BackHandler { onAbandonEditTitle() }
        } else {
          Text(
            text = title,
            textAlign = titleAlign,
            fontSize = titleFontSize,
            modifier = titleModifier
          )
        }
      }
      Box(modifier = Modifier.fillMaxSize()){
        ArticleThumbnailGrid(
          articleThumbnailUris = articleThumbnailUris,
          onClickArticle = onClickArticle,
        )
        if(editingTitle) {
          Box(
            modifier = Modifier
              .testTag("DialogSheetScrim")
              .fillMaxSize()
              .clickable(interactionSource = outsideKeyboardRowInteractionSource, indication = null, onClick = onAbandonEditTitle)
          )
        }
      }
    }
  }
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen(){
  NoopTheme{
    EnsembleDetailScreen(
      title = "Ensemble Title",
      editingTitle = false,
      articleThumbnailUris = repeatedThumbnailResourceIdsAsStrings,
      onClickArticle = {},
      onTitleClicked = {},
      onTitleChanged = {},
      onAbandonEditTitle = {},
    )
  }
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen_EditingTitle(){
  NoopTheme{
    EnsembleDetailScreen(
      title = "Ensemble Title",
      editingTitle = true,
      articleThumbnailUris = repeatedThumbnailResourceIdsAsStrings,
      onClickArticle = {},
      onTitleClicked = {},
      onTitleChanged = {},
      onAbandonEditTitle = {},
    )
  }
}
