package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.repository.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyEnsembleThumbnails
import com.inasweaterpoorlyknit.core.repository.model.LazyUriStrings
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.component.HorizontalOverlappingLayout
import com.inasweaterpoorlyknit.merlinsbag.ui.component.IconData
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopAddEnsembleDialog
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopFloatingActionButton
import com.inasweaterpoorlyknit.merlinsbag.ui.component.NoopImage
import com.inasweaterpoorlyknit.merlinsbag.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.merlinsbag.ui.isComposePreview
import com.inasweaterpoorlyknit.merlinsbag.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
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
  val ensemblesUiState by ensemblesViewModel.ensemblesUiState.collectAsStateWithLifecycle()
  EnsemblesScreen(
    ensembles = ensemblesUiState.ensembles,
    showAddEnsembleDialog = ensemblesUiState.showAddEnsembleDialog,
    addEnsembleDialogArticles = ensemblesUiState.articleImages,
    onClickEnsemble = { id ->
      navController.navigateToEnsembleDetail(ensembleId = id)
    },
    onClickAddEnsemble = ensemblesViewModel::onClickAddEnsemble,
    onClickSaveEnsemble = ensemblesViewModel::onClickSaveAddEnsembleDialog,
    onCloseAddEnsembleDialog = ensemblesViewModel::onClickCloseAddEnsembleDialog,
  )
}

@Composable
fun EnsemblesRow(
    ensemble: LazyEnsembleThumbnails,
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
        repeat(ensemble.thumbnails.size) { index ->
          NoopImage(
            uriString = ensemble.thumbnails.getUriString(index),
            contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
            modifier = Modifier
                .sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
                .padding(vertical = thumbnailsPadding)
          )
        }
      }
      if(ensemble.title.isNotEmpty()) {
        Text(
          text = ensemble.title,
          modifier = Modifier.padding(
            top = 0.dp,
            end = thumbnailsPadding,
            start = thumbnailsPadding,
            bottom = if(ensemble.thumbnails.isEmpty()) 0.dp else titleVerticalPadding
          )
        )
      }
    }
  }
}

@Composable
fun EnsemblesScreen(
    ensembles: List<LazyEnsembleThumbnails>,
    showAddEnsembleDialog: Boolean,
    addEnsembleDialogArticles: LazyUriStrings,
    onClickEnsemble: (id: String) -> Unit,
    onClickAddEnsemble: () -> Unit,
    onClickSaveEnsemble: (SaveEnsembleData) -> Unit,
    onCloseAddEnsembleDialog: () -> Unit,
) {
  val sidePadding = 10.dp
  val ensembleSpacing = 3.dp
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    LazyColumn(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
      contentPadding = PaddingValues(horizontal = sidePadding),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(ensembles.size) { index ->
        val topPadding = if(index == 0) sidePadding else ensembleSpacing
        val bottomPadding = if(index == ensembles.lastIndex) sidePadding else ensembleSpacing
        val ensemble = ensembles[index]
        EnsemblesRow(
          ensemble = ensemble,
          modifier = Modifier
              .fillMaxWidth()
              .padding(top = topPadding, bottom = bottomPadding)
              .clickable { onClickEnsemble(ensemble.id) }
        )
      }
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
  NoopAddEnsembleDialog(
    visible = visible,
    title = stringResource(id = R.string.Add_ensemble),
    positiveButtonLabel = stringResource(id = R.string.Save),
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
        placeholder = { Text(text = stringResource(id = R.string.Goth_2_Boss)) },
        onValueChange = { updatedTitle ->
          if(updatedTitle.length <= MAX_ENSEMBLE_TITLE_LENGTH) {
            setUserInputTitle(updatedTitle)
          }
        },
        label = { Text(text = stringResource(id = R.string.Ensemble_title)) },
        singleLine = true,
      )
    }
    Text(
      text = stringResource(id = R.string.Articles),
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
  }
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
              ArticleWithThumbnails(articleId = i.toString(), thumbnailPaths = listOf(ThumbnailFilename(uri = it)))
            }
          )
        )
      }
    }

@Composable
fun PreviewUtilEnsembleScreen(
    ensembles: List<LazyEnsembleThumbnails>,
    showAddEnsembleForm: Boolean,
) = NoopTheme {
  EnsemblesScreen(
    ensembles = ensembles,
    showAddEnsembleDialog = showAddEnsembleForm,
    addEnsembleDialogArticles = lazyRepeatedThumbnailResourceIdsAsStrings,
    onClickEnsemble = {}, onClickAddEnsemble = {}, onClickSaveEnsemble = {}, onCloseAddEnsembleDialog = {}
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
//endregion