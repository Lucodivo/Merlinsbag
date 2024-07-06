package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.ARTICLE_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.LandscapePreview
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndButtonContainer
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomSheetDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.core.ui.component.SelectableStaggeredThumbnailGrid
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings_EveryOtherIndexSet
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesViewModel.Companion.MAX_ENSEMBLE_TITLE_LENGTH

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
    windowSizeClass: WindowSizeClass,
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
  var showDeleteEnsembleDialog by remember { mutableStateOf(false) }
  val selectedEditArticleIndices = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??
  var showAddArticlesDialog by remember { mutableStateOf(false) }
  val selectedAddArticleIndices = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??
  ensembleDetailViewModel.titleChangeError.value.getContentIfNotHandled()?.let { Toast(it) }
  EnsembleDetailScreen(
    windowSizeClass = windowSizeClass,
    title = ensembleTitle,
    editingTitle = editingTitle,
    editEnsemblesMode = editMode,
    ensembleArticleThumbnailUris = ensembleUiState.ensembleArticleThumbnailUris,
    addArticleThumbnailUris = ensembleUiState.addArticleThumbnailUris,
    selectedEditArticleIndices = selectedEditArticleIndices.keys,
    selectedAddArticleIndices = selectedAddArticleIndices.keys,
    onTitleClicked = { editingTitle = true },
    onTitleChanged = { newTitle ->
      if(newTitle != ensembleTitle) ensembleDetailViewModel.onTitleChanged(newTitle)
      editingTitle = false
    },
    onClickEdit = {
      if(editMode) selectedEditArticleIndices.clear()
      editMode = !editMode
    },
    onSelectArticle = { index ->
      if(editMode) {
        if(selectedEditArticleIndices.containsKey(index)) selectedEditArticleIndices.remove(index) else selectedEditArticleIndices[index] = Unit
      } else {
        navController.navigateToArticleDetail(index, ensembleId)
      }
    },
    onLongSelectArticle = { index ->
      if(!editMode) editMode = true
      if(selectedEditArticleIndices.containsKey(index)) selectedEditArticleIndices.remove(index) else selectedEditArticleIndices[index] = Unit
    },
    onSelectedAddArticle = { index ->
      if(selectedAddArticleIndices.containsKey(index)) selectedAddArticleIndices.remove(index) else selectedAddArticleIndices[index] = Unit
    },
    onClickCancelSelection = { selectedEditArticleIndices.clear() },
    onClickRemoveArticles = {
      ensembleDetailViewModel.removeEnsembleArticles(selectedEditArticleIndices.keys.toList())
      selectedEditArticleIndices.clear()
    },
    onClickDeleteEnsemble = { showDeleteEnsembleDialog = true },
    onAbandonEditTitle = { editingTitle = false },
    showAddArticlesDialog = showAddArticlesDialog,
    onClickConfirmAddArticles = {
      if(selectedAddArticleIndices.isNotEmpty()) {
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
    showDeleteEnsembleAlertDialog = showDeleteEnsembleDialog,
    onDismissDeleteEnsembleDialog = { showDeleteEnsembleDialog = false },
    onClickPositiveDeleteEnsembleDialog = {
      showDeleteEnsembleDialog = false
      ensembleDetailViewModel.deleteEnsemble()
      navController.popBackStack()
    },
    modifier = modifier,
  )
}

@Composable
fun DeleteEnsembleAlertDialog(
    onDismiss: () -> Unit,
    onClickPositive: () -> Unit,
) {
  NoopSimpleAlertDialog(
    title = stringResource(id = R.string.delete_ensemble),
    text = stringResource(id = R.string.are_you_sure),
    headerIcon = { Icon(NoopIcons.DeleteForever, REDUNDANT_CONTENT_DESCRIPTION) },
    confirmText = stringResource(id = R.string.delete),
    cancelText = stringResource(id = R.string.cancel),
    onDismiss = onDismiss,
    onConfirm = onClickPositive,
  )
}

@Composable
fun EnsembleDetailFloatingActionButtons(
    editEnsemblesMode: Boolean,
    selectedEditArticleIndices: Set<Int>,
    onClickEdit: () -> Unit,
    onClickAddArticles: () -> Unit,
    onClickCancelSelection: () -> Unit,
    onClickRemoveArticles: () -> Unit,
    onClickDeleteEnsemble: () -> Unit,
    modifier: Modifier = Modifier,
) {
  NoopBottomEndButtonContainer(modifier) {
    NoopExpandingIconButton(
      expanded = editEnsemblesMode,
      collapsedIcon = IconData(NoopIcons.Edit, stringResource(R.string.enter_editing_mode)),
      expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
      verticalExpandedButtons =
      if(selectedEditArticleIndices.isNotEmpty()) {
        listOf(
          IconButtonData(
            icon = IconData(
              icon = NoopIcons.Cancel,
              contentDescription = stringResource(R.string.clear_selected_articles)
            ),
            onClick = onClickCancelSelection
          ),
          IconButtonData(
            icon = IconData(
              icon = NoopIcons.attachmentRemove(),
              contentDescription = stringResource(R.string.remove_selected_articles_from_ensemble)
            ),
            onClick = onClickRemoveArticles
          ),
        )
      } else {
        listOf(
          IconButtonData(
            icon = IconData(
              icon = NoopIcons.DeleteForever,
              contentDescription = stringResource(R.string.delete_ensemble)
            ),
            onClick = onClickDeleteEnsemble
          ),
          IconButtonData(
            icon = IconData(
              icon = NoopIcons.Attachment,
              contentDescription = stringResource(R.string.attach_articles_to_ensemble)
            ),
            onClick = onClickAddArticles
          ),
        )
      },
      onClick = onClickEdit,
    )
  }
}

@Composable
fun EnsembleDetailScreen(
    windowSizeClass: WindowSizeClass,
    title: String,
    editEnsemblesMode: Boolean,
    editingTitle: Boolean,
    ensembleArticleThumbnailUris: LazyUriStrings,
    addArticleThumbnailUris: LazyUriStrings,
    selectedEditArticleIndices: Set<Int>,
    selectedAddArticleIndices: Set<Int>,
    onTitleClicked: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onClickEdit: () -> Unit,
    onClickAddArticles: () -> Unit,
    onSelectArticle: (index: Int) -> Unit,
    onLongSelectArticle: (index: Int) -> Unit,
    onClickRemoveArticles: () -> Unit,
    onClickCancelSelection: () -> Unit,
    onClickDeleteEnsemble: () -> Unit,
    onAbandonEditTitle: () -> Unit,
    showAddArticlesDialog: Boolean,
    onSelectedAddArticle: (articleIndex: Int) -> Unit,
    onClickConfirmAddArticles: () -> Unit,
    onCloseAddArticlesDialog: () -> Unit,
    showDeleteEnsembleAlertDialog: Boolean,
    onDismissDeleteEnsembleDialog: () -> Unit,
    onClickPositiveDeleteEnsembleDialog: () -> Unit,
    modifier: Modifier = Modifier,
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
) {
  val layoutDir = LocalLayoutDirection.current
  val compactWidth = windowSizeClass.compactWidth()
  val systemBarTopPadding = systemBarPaddingValues.calculateTopPadding()
  val systemBarStartPadding = systemBarPaddingValues.calculateStartPadding(layoutDir)
  val systemBarEndPadding = systemBarPaddingValues.calculateEndPadding(layoutDir)
  Surface(
    modifier = modifier.fillMaxSize()
  ) {
    Column(
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
          .padding(
            top = systemBarTopPadding,
            start = systemBarStartPadding,
            end = systemBarEndPadding,
          )
    ) {
      val titleRowInteractionSource = remember { MutableInteractionSource() }
      val outsideKeyboardRowInteractionSource = remember { MutableInteractionSource() }
      Box(
        contentAlignment = if(compactWidth) Alignment.TopCenter else Alignment.TopStart,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
              interactionSource = titleRowInteractionSource,
              indication = null,
              onClick = { if(!editingTitle) onTitleClicked() }
            ),
      ) {
        if(editingTitle) {
          val (editTitle, setEditTitle) = remember { mutableStateOf("") }
          val focusRequester = FocusRequester()
          OutlinedTextField(
            value = editTitle,
            placeholder = { Text(text = title.ifEmpty { "Goth 2 Boss" }) },
            onValueChange = { if(it.length <= MAX_ENSEMBLE_TITLE_LENGTH) setEditTitle(it) },
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.ensemble_title)) },
            keyboardActions = KeyboardActions(onDone = { onTitleChanged(editTitle) }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .focusRequester(focusRequester)
          )
          LaunchedEffect(Unit) { focusRequester.requestFocus() }
          BackHandler { onAbandonEditTitle() }
        } else {
          val titleModifier = Modifier
          val iconModifier = Modifier.padding(start = if(compactWidth) 0.dp else 16.dp, end = 4.dp)
          val titleAlign = if(compactWidth) TextAlign.Center else TextAlign.Start
          val titleFontSize = MaterialTheme.typography.titleLarge.fontSize
          Row{
            Icon(NoopIcons.ensembles(), stringResource(R.string.hashtag), modifier = iconModifier)
            if(title.isNotEmpty()) {
              Text(text = title, textAlign = titleAlign, fontSize = titleFontSize, modifier = titleModifier)
            } else {
              Text(text = "[untitled]", textAlign = titleAlign, fontSize = titleFontSize, color = Color.Gray, modifier = titleModifier)
            }
          }
        }
      }
      Box(modifier = Modifier.fillMaxSize()) {
        SelectableStaggeredThumbnailGrid(
          selectable = editEnsemblesMode,
          onSelect = onSelectArticle,
          onLongSelect = onLongSelectArticle,
          thumbnailUris = ensembleArticleThumbnailUris,
          selectedThumbnails = selectedEditArticleIndices,
        )
        if(editingTitle) {
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
  EnsembleDetailFloatingActionButtons(
    editEnsemblesMode = editEnsemblesMode,
    selectedEditArticleIndices = selectedEditArticleIndices,
    onClickEdit = onClickEdit,
    onClickAddArticles = onClickAddArticles,
    onClickCancelSelection = onClickCancelSelection,
    onClickRemoveArticles = onClickRemoveArticles,
    onClickDeleteEnsemble = onClickDeleteEnsemble,
    modifier = Modifier.padding(start = systemBarStartPadding, end = systemBarEndPadding),
  )
  AddArticlesDialog(
    visible = showAddArticlesDialog,
    articleThumbnailUris = addArticleThumbnailUris,
    selectedArticleIndices = selectedAddArticleIndices,
    onSelectedArticle = onSelectedAddArticle,
    onClose = onCloseAddArticlesDialog,
    onConfirm = onClickConfirmAddArticles,
  )
  if(showDeleteEnsembleAlertDialog) {
    DeleteEnsembleAlertDialog(
      onDismiss = onDismissDeleteEnsembleDialog,
      onClickPositive = onClickPositiveDeleteEnsembleDialog,
    )
  }
}

@Composable
fun AddArticlesDialog(
    visible: Boolean,
    articleThumbnailUris: LazyUriStrings,
    selectedArticleIndices: Set<Int>,
    onSelectedArticle: (articleIndex: Int) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
  val addArticlesAvailable = articleThumbnailUris.isNotEmpty()
  NoopBottomSheetDialog(
    visible = visible,
    title = stringResource(id = R.string.add_article),
    positiveButtonText = if(addArticlesAvailable) stringResource(id = R.string.save) else "",
    onClose = onClose,
    onPositive = onConfirm,
  ) {
    if(addArticlesAvailable) {
      LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.height(110.dp)
      ) {
        val padding = 10.dp
        items(count = articleThumbnailUris.size) { articleIndex ->
          val addArticleThumbnailUri = articleThumbnailUris.getUriString(articleIndex)
          val selected = selectedArticleIndices.contains(articleIndex)
          Box(contentAlignment = Alignment.Center) {
            SelectableNoopImage(
              selectable = true,
              selected = selected,
              uriString = addArticleThumbnailUri,
              contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
              modifier = Modifier
                  .padding(padding)
                  .clickable { onSelectedArticle(articleIndex) }
            )
          }
        }
      }
    } else {
      Text(text = stringResource(id = R.string.all_articles_already_in_ensemble))
    }
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilEnsembleDetailScreen(
    editingTitle: Boolean = false,
    editMode: Boolean = false,
    showAddArticlesDialog: Boolean = false,
    selectedArticleIndices: Set<Int> = emptySet(),
    selectedAddArticleIndices: Set<Int> = emptySet(),
    showDeleteEnsembleAlertDialog: Boolean = false,
) = EnsembleDetailScreen(
  title = "Ensemble Title",
  editEnsemblesMode = editMode,
  editingTitle = editingTitle,
  ensembleArticleThumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
  addArticleThumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
  selectedEditArticleIndices = selectedArticleIndices,
  selectedAddArticleIndices = selectedAddArticleIndices,
  onTitleClicked = {},
  onTitleChanged = {}, onClickEdit = {}, onClickAddArticles = {}, onSelectArticle = {}, onLongSelectArticle = {}, onClickRemoveArticles = {}, onClickCancelSelection = {},
  onClickDeleteEnsemble = {}, onAbandonEditTitle = {}, showAddArticlesDialog = showAddArticlesDialog, onSelectedAddArticle = {}, onClickConfirmAddArticles = {}, onCloseAddArticlesDialog = {},
  showDeleteEnsembleAlertDialog = showDeleteEnsembleAlertDialog, onDismissDeleteEnsembleDialog = {}, onClickPositiveDeleteEnsembleDialog = {}, windowSizeClass = currentWindowAdaptiveInfo(),
)

@Composable
fun PreviewUtilEnsembleDetailFloatingActionButtons(
    editEnsemblesMode: Boolean,
    selectedArticleIndices: Set<Int> = emptySet(),
) = NoopTheme {
  EnsembleDetailFloatingActionButtons(
    editEnsemblesMode = editEnsemblesMode,
    selectedEditArticleIndices = selectedArticleIndices,
    onClickEdit = {}, onClickAddArticles = {}, onClickCancelSelection = {}, onClickRemoveArticles = {}, onClickDeleteEnsemble = {}
  )
}

@Preview @Composable fun PreviewEnsembleDetailScreen() = NoopTheme { PreviewUtilEnsembleDetailScreen() }

@LandscapePreview @Composable fun PreviewEnsembleDetailScreen_EditingLandscape() = NoopTheme { PreviewUtilEnsembleDetailScreen(editMode = true) }

@Preview
@Composable
fun PreviewEnsembleDetailScreen_Editing() = NoopTheme {
  PreviewUtilEnsembleDetailScreen(
    editMode = true,
    editingTitle = true,
    selectedArticleIndices = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet()
  )
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen_DeleteEnsembleAlertDialog() = NoopTheme {
  PreviewUtilEnsembleDetailScreen(
    editMode = true,
    showDeleteEnsembleAlertDialog = true,
  )
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen_AddArticlesDialog() = NoopTheme {
  PreviewUtilEnsembleDetailScreen(
    editMode = false,
    editingTitle = false,
    showAddArticlesDialog = true,
    selectedArticleIndices = repeatedThumbnailResourceIdsAsStrings_EveryOtherIndexSet,
  )
}

@Preview
@Composable
fun PreviewAddArticlesDialog() = NoopTheme {
  AddArticlesDialog(
    visible = true,
    articleThumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
    selectedArticleIndices = repeatedThumbnailResourceIdsAsStrings_EveryOtherIndexSet,
    onSelectedArticle = {}, onClose = {}, onConfirm = {},
  )
}

@Preview
@Composable
fun PreviewAddArticlesDialog_noAddArticles() = NoopTheme {
  AddArticlesDialog(
    visible = true,
    articleThumbnailUris = LazyUriStrings.Empty,
    selectedArticleIndices = emptySet(),
    onSelectedArticle = {}, onClose = {}, onConfirm = {},
  )
}

@Preview @Composable fun PreviewEnsembleDetailFloatingActionButtons_Collapsed() = PreviewUtilEnsembleDetailFloatingActionButtons(false)
@Preview @Composable fun PreviewEnsembleDetailFloatingActionButtons_Expanded() = PreviewUtilEnsembleDetailFloatingActionButtons(true)
@Preview @Composable fun PreviewEnsembleDetailFloatingActionButtons_EditingArticles() = PreviewUtilEnsembleDetailFloatingActionButtons(true, setOf(0))
@Preview @Composable fun PreviewDeleteEnsembleAlertDialog() = NoopTheme { DeleteEnsembleAlertDialog(onDismiss = {}, onClickPositive = {}) }
//endregion