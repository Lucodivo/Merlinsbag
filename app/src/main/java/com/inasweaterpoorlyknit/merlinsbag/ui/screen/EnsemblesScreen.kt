package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomSheetDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopFloatingActionButton
import com.inasweaterpoorlyknit.core.ui.isComposePreview
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.core.ui.component.NoopOverlappingImageRowColumn
import com.inasweaterpoorlyknit.core.ui.component.NoopOverlappingPlaceholderRowColumn
import com.inasweaterpoorlyknit.core.ui.component.SearchBox
import com.inasweaterpoorlyknit.core.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.core.ui.component.previewEnsembles
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesViewModel
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesViewModel.Companion.MAX_ENSEMBLE_TITLE_LENGTH
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SaveEnsembleData

const val ENSEMBLES_ROUTE = "ensembles_route"

fun NavController.navigateToEnsembles(navOptions: NavOptions? = null) = navigate(ENSEMBLES_ROUTE, navOptions)

@Composable
fun EnsemblesRoute(
    navController: NavController,
    ensemblesViewModel: EnsemblesViewModel = hiltViewModel(),
) {
  val lazyEnsembleThumbnails by ensemblesViewModel.lazyEnsembles.collectAsStateWithLifecycle()
  val showAddEnsembleDialog by ensemblesViewModel.showAddEnsembleDialog.collectAsStateWithLifecycle()
  val showPlaceholder by ensemblesViewModel.showPlaceholder.collectAsStateWithLifecycle()
  val addEnsembleDialogArticles by ensemblesViewModel.addArticleThumbnails.collectAsStateWithLifecycle()
  var searchQuery by remember { mutableStateOf("") }
  EnsemblesScreen(
    lazyEnsembleThumbnails = lazyEnsembleThumbnails,
    showAddEnsembleDialog = showAddEnsembleDialog,
    showPlaceholder = showPlaceholder,
    searchQuery = searchQuery,
    addEnsembleDialogArticles = addEnsembleDialogArticles,
    onClickEnsemble = { navController.navigateToEnsembleDetail(ensemblesViewModel.onClickEnsemble(it)) },
    onClickAddEnsemble = ensemblesViewModel::onClickAddEnsemble,
    onClickSaveEnsemble = ensemblesViewModel::onClickSaveAddEnsembleDialog,
    onCloseAddEnsembleDialog = ensemblesViewModel::onClickCloseAddEnsembleDialog,
    onUpdateSearchQuery = { newSearchQuery ->
      ensemblesViewModel.searchQuery(newSearchQuery)
      searchQuery = newSearchQuery
    },
    onClearSearchQuery = {
      ensemblesViewModel.searchQuery("")
      searchQuery = ""
    },
  )
}

@Composable
fun EnsemblesScreen(
    lazyEnsembleThumbnails: List<Pair<String, LazyUriStrings>>?,
    showAddEnsembleDialog: Boolean,
    showPlaceholder: Boolean,
    addEnsembleDialogArticles: LazyUriStrings,
    searchQuery: String,
    onClickEnsemble: (index: Int) -> Unit,
    onClickAddEnsemble: () -> Unit,
    onClickSaveEnsemble: (SaveEnsembleData) -> Unit,
    onCloseAddEnsembleDialog: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    val placeholderVisibilityAnimatedFloat by animateFloatAsState(
      targetValue = if(showPlaceholder) 1.0f else 0.0f,
      animationSpec = tween(durationMillis = 1000),
      label = "placeholder ensemble grid visibility"
    )
    if(placeholderVisibilityAnimatedFloat == 0.0f){
      Column {
        SearchBox(
          query = searchQuery,
          placeholder = stringResource(R.string.goth_2_boss),
          onQueryChange = onUpdateSearchQuery,
          onClearQuery = onClearSearchQuery,
          modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, start = 8.dp, end = 8.dp),
        )
        if(lazyEnsembleThumbnails != null ) {
          NoopOverlappingImageRowColumn(lazyEnsembleThumbnails, onClickEnsemble)
        }
      }
    } else {
        NoopOverlappingPlaceholderRowColumn(
          modifier = Modifier.alpha(placeholderVisibilityAnimatedFloat)
        )
    }
    NoopFloatingActionButton(
      iconData = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
      onClick = onClickAddEnsemble,
    )
    AddEnsembleDialog(
      visible = showAddEnsembleDialog,
      articleThumbnails = addEnsembleDialogArticles,
      onClickSave = onClickSaveEnsemble,
      onClickClose = onCloseAddEnsembleDialog,
    )
  }
}

@Composable
fun AddEnsembleDialog(
    visible: Boolean,
    articleThumbnails: LazyUriStrings,
    onClickSave: (SaveEnsembleData) -> Unit,
    onClickClose: () -> Unit,
) {
  BackHandler(enabled = visible) { onClickClose() }
  val (userInputTitle, setUserInputTitle) = remember { mutableStateOf("") }
  val selectedArticleIndices = if(isComposePreview) remember { mutableSetOf(0, 1) } else remember { mutableSetOf() }
  NoopBottomSheetDialog(
    visible = visible,
    title = stringResource(id = R.string.add_ensemble),
    positiveButtonLabel = stringResource(id = R.string.save),
    onClose = onClickClose,
    onPositive = {
      onClickSave(
        SaveEnsembleData(
          title = userInputTitle,
          articleIndices = selectedArticleIndices.toList(),
        )
      )
      selectedArticleIndices.clear()
      setUserInputTitle("")
    },
  ) {
    Row {
      OutlinedTextField(
        value = userInputTitle,
        placeholder = { Text(text = stringResource(id = R.string.goth_2_boss)) },
        onValueChange = { updatedTitle ->
          if(updatedTitle.length <= MAX_ENSEMBLE_TITLE_LENGTH) {
            setUserInputTitle(updatedTitle)
          }
        },
        label = { Text(text = stringResource(id = R.string.ensemble_title)) },
        singleLine = true,
      )
    }
    if(articleThumbnails.isNotEmpty()){
      Text(
        text = stringResource(id = R.string.articles),
        modifier = Modifier.padding(10.dp)
      )
      LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.height(110.dp)
      ) {
        val padding = 10.dp
        items(count = articleThumbnails.size) { articleIndex ->
          val articleThumbnailUriString = articleThumbnails.getUriString(articleIndex)
          Box(contentAlignment = Alignment.Center) {
            val (selected, setSelected) = remember { mutableStateOf(selectedArticleIndices.contains(articleIndex)) }
            SelectableNoopImage(
              selectable = true,
              selected = selected,
              uriString = articleThumbnailUriString,
              contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
              modifier = Modifier
                  .padding(padding)
                  .clickable {
                    if(selected) {
                      selectedArticleIndices.remove(articleIndex)
                      setSelected(false)
                    } else {
                      selectedArticleIndices.add(articleIndex)
                      setSelected(true)
                    }
                  }
            )
            if(selected) {
              Icon(
                imageVector = NoopIcons.SelectedIndicator,
                contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
                modifier = Modifier.align(Alignment.BottomEnd),
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    } else {
      Text(
        text = stringResource(R.string.no_articles_available),
        modifier = Modifier.padding(10.dp),
      )
    }
  }
}

//region COMPOSABLE PREVIEWS

@Composable
fun PreviewUtilEnsembleScreen(
    ensembles: List<Pair<String, LazyUriStrings>>,
    showAddEnsembleForm: Boolean,
) = NoopTheme {
  EnsemblesScreen(
    lazyEnsembleThumbnails = ensembles,
    showAddEnsembleDialog = showAddEnsembleForm,
    showPlaceholder = false,
    searchQuery = "Goth 2 Boss",
    addEnsembleDialogArticles = lazyRepeatedThumbnailResourceIdsAsStrings,
    onClickEnsemble = {}, onClickAddEnsemble = {}, onClickSaveEnsemble = {}, onCloseAddEnsembleDialog = {},
    onUpdateSearchQuery = {}, onClearSearchQuery = {},
  )
}

@Preview
@Composable
fun PreviewEnsembleScreen() = PreviewUtilEnsembleScreen(
  ensembles = previewEnsembles,
  showAddEnsembleForm = false,
)

@Preview
@Composable
fun PreviewEnsemblesScreenAddEnsembleDialog() = PreviewUtilEnsembleScreen(
  ensembles = previewEnsembles,
  showAddEnsembleForm = true,
)

@Preview
@Composable
fun PreviewAddEnsembleDialog() = NoopTheme {
  AddEnsembleDialog(
    visible = true,
    articleThumbnails = lazyRepeatedThumbnailResourceIdsAsStrings,
    onClickSave = {},
    onClickClose = {},
  )
}

@Preview
@Composable
fun PreviewAddEnsembleDialog_NoArticles() = NoopTheme {
  AddEnsembleDialog(
    visible = true,
    articleThumbnails = LazyUriStrings.Companion.Empty,
    onClickSave = {},
    onClickClose = {},
  )
}
//endregion