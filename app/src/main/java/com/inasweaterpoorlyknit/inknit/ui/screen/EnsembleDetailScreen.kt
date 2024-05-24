package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.component.IconData
import com.inasweaterpoorlyknit.inknit.ui.component.NoopAddEnsembleDialog
import com.inasweaterpoorlyknit.inknit.ui.component.NoopExpandingFloatingActionButton
import com.inasweaterpoorlyknit.inknit.ui.component.SelectableArticleThumbnailGrid
import com.inasweaterpoorlyknit.inknit.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.inknit.ui.component.TextIconButtonData
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings_EveryOtherIndexSet
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.viewmodel.EnsembleDetailViewModel

const val ENSEMBLE_ID_ARG = "ensembleId"
const val ENSEMBLE_DETAIL_ROUTE_BASE = "ensembles_route"
const val ENSEMBLE_DETAIL_ROUTE = "$ENSEMBLE_DETAIL_ROUTE_BASE?$ENSEMBLE_ID_ARG={$ENSEMBLE_ID_ARG}"

fun NavController.navigateToEnsembleDetail(ensembleId: String, navOptions: NavOptions? = null) {
  val route = "${ENSEMBLE_DETAIL_ROUTE_BASE}?${ENSEMBLE_ID_ARG}=$ensembleId"
  navigate(route, navOptions)
}

@Composable
fun EnsembleDetailRoute(
  navController: NavController,
  ensembleId: String,
  modifier: Modifier = Modifier,
) {
  val ensembleDetailViewModel =
    hiltViewModel<EnsembleDetailViewModel, EnsembleDetailViewModel.EnsembleDetailViewModelFactory> { factory ->
      factory.create(ensembleId)
    }
  val ensembleTitle by ensembleDetailViewModel.ensembleTitle.collectAsStateWithLifecycle()
  val ensembleUiState by ensembleDetailViewModel.ensembleUiState.collectAsStateWithLifecycle()
  var editingTitle by remember { mutableStateOf(false) }
  var editMode by remember { mutableStateOf(false) }
  val selectedEditArticleIndices = remember { mutableStateMapOf<Int, Unit>() }
  var showAddArticlesDialog by remember { mutableStateOf(false) }
  val selectedAddArticleIndices = remember { mutableStateMapOf<Int, Unit>() }
  EnsembleDetailScreen(
    title = ensembleTitle,
    editingTitle = editingTitle,
    editEnsemblesMode = editMode,
    ensembleArticleThumbnailUris = ensembleUiState.ensembleArticleThumbnailUris,
    addArticleThumbnailUris = ensembleUiState.addArticleThumbnailUris,
    selectedEditArticleIndices = selectedEditArticleIndices.keys,
    selectedAddArticleIndices = selectedAddArticleIndices.keys,
    onTitleClicked = { editingTitle = true },
    onTitleChanged = { newTitle ->
      ensembleDetailViewModel.onTitleChanged(newTitle)
      editingTitle = false
    },
    onClickEdit = {
      if (editMode) selectedEditArticleIndices.clear()
      editMode = !editMode
    },
    onSelectedEditArticle = { index ->
      if (editMode) {
        if (selectedEditArticleIndices.containsKey(index)) selectedEditArticleIndices.remove(index) else selectedEditArticleIndices[index] = Unit
      } else {
        navController.navigateToArticleDetail(index, ensembleId)
      }
    },
    onSelectedAddArticle = { index ->
      if (selectedAddArticleIndices.containsKey(index)) selectedAddArticleIndices.remove(index) else selectedAddArticleIndices[index] = Unit
    },
    onClickCancelSelection = {
      selectedEditArticleIndices.clear()
    },
    onClickDelete = {
      ensembleDetailViewModel.removeEnsembleArticles(selectedEditArticleIndices.keys.toList())
      selectedEditArticleIndices.clear()
    },
    onAbandonEditTitle = { editingTitle = false },
    showAddArticlesDialog = showAddArticlesDialog,
    onClickConfirmAddArticles = {
      if(selectedAddArticleIndices.isNotEmpty()){
        ensembleDetailViewModel.addEnsembleArticles(selectedAddArticleIndices.keys.toList())
        selectedAddArticleIndices.clear()
      }
      showAddArticlesDialog = false
    },
    onCloseAddArticlesDialog = {
      showAddArticlesDialog = false
      selectedAddArticleIndices.clear()
    },
    onClickAddArticles = { showAddArticlesDialog = true },
    modifier = modifier,
  )
}

@Composable
fun EnsembleDetailScreen(
  title: String,
  editEnsemblesMode: Boolean,
  editingTitle: Boolean,
  ensembleArticleThumbnailUris: List<String>,
  addArticleThumbnailUris: List<String>,
  selectedEditArticleIndices: Set<Int>,
  selectedAddArticleIndices: Set<Int>,
  onTitleClicked: () -> Unit,
  onTitleChanged: (String) -> Unit,
  onClickEdit: () -> Unit,
  onClickAddArticles: () -> Unit,
  onSelectedEditArticle: (index: Int) -> Unit,
  onClickDelete: () -> Unit,
  onClickCancelSelection: () -> Unit,
  onAbandonEditTitle: () -> Unit,
  showAddArticlesDialog: Boolean,
  onSelectedAddArticle: (articleIndex: Int) -> Unit,
  onClickConfirmAddArticles: () -> Unit,
  onCloseAddArticlesDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxSize()
  ) {
    Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            onClick = { if (!editingTitle) onTitleClicked() }
          ),
      ) {
        val titleModifier = Modifier.fillMaxWidth()
        val titleAlign = TextAlign.Center
        val titleFontSize = MaterialTheme.typography.titleLarge.fontSize
        if (editingTitle) {
          val (editTitle, setEditTitle) = remember { mutableStateOf("") }
          val focusRequester = FocusRequester()
          OutlinedTextField(
            value = editTitle,
            placeholder = { Text(text = title.ifEmpty { "Goth 2 Boss" }) },
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
          if (title.isNotEmpty()) {
            Text(text = title, textAlign = titleAlign, fontSize = titleFontSize, modifier = titleModifier)
          } else {
            Text(text = "[untitled]", textAlign = titleAlign, fontSize = titleFontSize, color = Color.Gray, modifier = titleModifier)
          }
        }
      }
      Box(modifier = Modifier.fillMaxSize()) {
        SelectableArticleThumbnailGrid(
          selectable = editEnsemblesMode,
          onSelected = onSelectedEditArticle,
          thumbnailUris = ensembleArticleThumbnailUris,
          selectedThumbnails = selectedEditArticleIndices,
        )
        if (editingTitle) {
          Box(
            modifier = Modifier
              .testTag("DialogSheetScrim")
              .fillMaxSize()
              .clickable(
                interactionSource = outsideKeyboardRowInteractionSource,
                indication = null,
                onClick = onAbandonEditTitle
              )
          )
        }
      }
    }
  }
  NoopExpandingFloatingActionButton(
    expanded = editEnsemblesMode,
    collapsedIcon = IconData(NoopIcons.Edit, TODO_ICON_CONTENT_DESCRIPTION),
    expandedIcon = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
    expandedButtons =
    if (selectedEditArticleIndices.isNotEmpty()) {
      listOf(
        TextIconButtonData(
          text = "",
          icon = IconData(
            icon = NoopIcons.Cancel,
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION
          ),
          onClick = onClickCancelSelection
        ),
        TextIconButtonData(
          text = "",
          icon = IconData(
            icon = NoopIcons.Delete,
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION
          ),
          onClick = onClickDelete
        ),
      )
    } else {
      listOf(
        TextIconButtonData(
          text = "",
          icon = IconData(
            icon = NoopIcons.Attachment,
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION
          ),
          onClick = onClickAddArticles
        ),
      )
    },
    onClickExpandCollapse = onClickEdit,
  )
  AddArticlesDialog(
    visible = showAddArticlesDialog,
    articleThumbnailUris = addArticleThumbnailUris,
    selectedArticleIndices = selectedAddArticleIndices,
    onSelectedArticle = onSelectedAddArticle,
    onClose = onCloseAddArticlesDialog,
    onConfirm = onClickConfirmAddArticles,
  )
}

@Composable
fun AddArticlesDialog(
  visible: Boolean,
  articleThumbnailUris: List<String>,
  selectedArticleIndices: Set<Int>,
  onSelectedArticle: (articleIndex: Int) -> Unit,
  onClose: () -> Unit,
  onConfirm: () -> Unit,
) {
  val addArticlesAvailable = articleThumbnailUris.isNotEmpty()
  NoopAddEnsembleDialog(
    visible = visible,
    title = stringResource(id = R.string.Add_article),
    positiveButtonLabel = if(addArticlesAvailable) stringResource(id = R.string.Save) else "",
    onClose = onClose,
    onPositive = onConfirm ,
  ) {
    if(addArticlesAvailable){
      LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.height(110.dp)
      ) {
        val padding = 10.dp
        items(count = articleThumbnailUris.size) { articleIndex ->
          val addArticleThumbnailUri = articleThumbnailUris[articleIndex]
          val selected = selectedArticleIndices.contains(articleIndex)
          Box(contentAlignment = Alignment.Center) {
            SelectableNoopImage(
              selectable = true,
              selected = selected,
              uriString = addArticleThumbnailUri,
              contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
              modifier = Modifier
                .padding(padding)
                .clickable { onSelectedArticle(articleIndex) }
            )
            if (selected) {
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
      Text(text = stringResource(id = R.string.all_articles_already_in_ensemble))
    }
  }
}

@Composable
fun __PreviewEnsembleDetailScreen(
  editingTitle: Boolean = false,
  editMode: Boolean = false,
  showAddArticlesDialog: Boolean = false,
  selectedArticleIndices: Set<Int> = emptySet(),
  selectedAddArticleIndices: Set<Int> = emptySet(),
) = EnsembleDetailScreen(
  title = "Ensemble Title",
  editEnsemblesMode = editMode,
  showAddArticlesDialog = showAddArticlesDialog,
  editingTitle = editingTitle,
  ensembleArticleThumbnailUris = repeatedThumbnailResourceIdsAsStrings,
  selectedEditArticleIndices = selectedArticleIndices,
  addArticleThumbnailUris = repeatedThumbnailResourceIdsAsStrings,
  selectedAddArticleIndices = selectedAddArticleIndices,
  onTitleClicked = {}, onTitleChanged = {}, onClickEdit = {}, onSelectedEditArticle = {}, onClickDelete = {}, onClickCancelSelection = {}, onAbandonEditTitle = {},
  onSelectedAddArticle = {}, onClickConfirmAddArticles = {}, onCloseAddArticlesDialog = {}, onClickAddArticles = {},
)

@Preview
@Composable
fun PreviewEnsembleDetailScreen() {
  NoopTheme {
    __PreviewEnsembleDetailScreen(editingTitle = false)
  }
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen_Editing() {
  NoopTheme {
    __PreviewEnsembleDetailScreen(
      editMode = true,
      editingTitle = true,
      selectedArticleIndices = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet()
    )
  }
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen_AddArticlesDialog() {
  NoopTheme {
    __PreviewEnsembleDetailScreen(
      editMode = false,
      editingTitle = false,
      showAddArticlesDialog = true,
      selectedArticleIndices = repeatedThumbnailResourceIdsAsStrings_EveryOtherIndexSet,
    )
  }
}

@Preview
@Composable
fun PreviewAddArticlesDialog() {
  NoopTheme {
    AddArticlesDialog(
      visible = true,
      articleThumbnailUris = repeatedThumbnailResourceIdsAsStrings,
      selectedArticleIndices = repeatedThumbnailResourceIdsAsStrings_EveryOtherIndexSet,
      onSelectedArticle = {}, onClose = {}, onConfirm = {},
      )
  }
}

@Preview
@Composable
fun PreviewAddArticlesDialog_noAddArticles() {
  NoopTheme {
    AddArticlesDialog(
      visible = true,
      articleThumbnailUris = emptyList(),
      selectedArticleIndices = emptySet(),
      onSelectedArticle = {}, onClose = {}, onConfirm = {},
    )
  }
}
