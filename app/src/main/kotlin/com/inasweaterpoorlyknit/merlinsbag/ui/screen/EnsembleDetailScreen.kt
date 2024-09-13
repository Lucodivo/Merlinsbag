package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.inasweaterpoorlyknit.merlinsbag.Constants.Companion.MAX_ENSEMBLE_TITLE_LENGTH
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel.EditState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel
import kotlinx.serialization.Serializable
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel.EditState.EnabledSelectedArticles
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel.EditState.Disabled
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsembleDetailViewModel.EditState.EnabledGeneral


@Serializable
data class EnsembleDetailRouteArgs(
  val ensembleId: String,
)

fun NavController.navigateToEnsembleDetail(ensembleId: String, navOptions: NavOptions? = null) =
  navigate(EnsembleDetailRouteArgs(ensembleId = ensembleId), navOptions)

@Composable
fun EnsembleDetailRoute(
    ensembleId: String,
    navigateToArticleDetail: (articleIndex: Int, ensembleId: String) -> Unit,
    navigateBack: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
  val ensembleDetailViewModel =
      hiltViewModel<EnsembleDetailViewModel, EnsembleDetailViewModel.EnsembleDetailViewModelFactory> { factory ->
        factory.create(ensembleId)
      }
  val context = LocalContext.current
  BackHandler(enabled = ensembleDetailViewModel.onBackEnabled, onBack = ensembleDetailViewModel::onBack)

  val ensembleTitle by ensembleDetailViewModel.ensembleTitle.collectAsStateWithLifecycle()
  val ensembleUiState by ensembleDetailViewModel.ensembleUiState.collectAsStateWithLifecycle()

  LaunchedEffect(ensembleDetailViewModel.titleChangeError) {
    ensembleDetailViewModel.titleChangeError.getContentIfNotHandled()?.let { context.toast(it) }
  }

  LaunchedEffect(ensembleDetailViewModel.navigationEventState) {
    ensembleDetailViewModel.navigationEventState.getContentIfNotHandled()?.let {
      when(it){
        is EnsembleDetailViewModel.NavigationState.ArticleDetail -> navigateToArticleDetail(it.index, it.ensembleId)
        EnsembleDetailViewModel.NavigationState.Finished -> navigateBack()
      }
    }
  }

  EnsembleDetailScreen(
    windowSizeClass = windowSizeClass,
    title = ensembleTitle,
    editingTitle = ensembleDetailViewModel.editingTitle,
    editState = ensembleDetailViewModel.editState,
    dialogState = ensembleDetailViewModel.dialogState,
    ensembleArticleThumbnailUris = ensembleUiState.ensembleArticleThumbnailUris,
    addArticleThumbnailUris = ensembleUiState.addArticleThumbnailUris,
    selectedEditArticleIndices = ensembleDetailViewModel.selectedEditArticleIndices,
    selectedAddArticleIndices = ensembleDetailViewModel.selectedAddArticleIndices,
    onClickTitle = ensembleDetailViewModel::onClickTitle,
    onTitleChanged = ensembleDetailViewModel::onTitleChanged,
    onClickEdit = ensembleDetailViewModel::onClickEdit,
    onClickMinimizeButtonControl = ensembleDetailViewModel::onClickMinimizeButtonControl,
    onClickArticle = ensembleDetailViewModel::onClickArticle,
    onLongClickArticle = ensembleDetailViewModel::onLongPressArticle,
    onClickArticleAddDialog = ensembleDetailViewModel::onClickArticleAddDialog,
    onClickCancelArticleSelection = ensembleDetailViewModel::onClickCancelArticleSelection,
    onClickRemoveArticles = ensembleDetailViewModel::onClickRemoveArticles,
    onClickDeleteEnsemble = ensembleDetailViewModel::onClickDeleteEnsemble,
    onDismissEditTitle = ensembleDetailViewModel::onDismissEditTitle,
    onClickConfirmAddArticles = ensembleDetailViewModel::onClickConfirmAddArticles,
    onCloseAddArticlesDialog = ensembleDetailViewModel::onDismissAddArticlesDialog,
    onClickAddArticles = ensembleDetailViewModel::onClickAddArticles,
    onDismissDeleteEnsembleDialog = ensembleDetailViewModel::onDismissDeleteEnsembleDialog,
    onClickPositiveDeleteEnsembleDialog = ensembleDetailViewModel::onClickPositiveDeleteEnsembleDialog,
    modifier = modifier,
  )
}

@Composable
fun DeleteEnsembleAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onClickPositive: () -> Unit,
) = NoopSimpleAlertDialog(
  visible = visible,
  title = stringResource(id = R.string.delete_ensemble),
  text = stringResource(id = R.string.are_you_sure),
  headerIcon = { Icon(NoopIcons.DeleteForever, REDUNDANT_CONTENT_DESCRIPTION) },
  confirmText = stringResource(id = R.string.delete),
  cancelText = stringResource(id = R.string.cancel),
  onDismiss = onDismiss,
  onConfirm = onClickPositive,
)

@Composable
fun EnsembleDetailFloatingActionButtons(
    editMode: EditState,
    onClickEdit: () -> Unit,
    onClickMinimizeButtonControl: () -> Unit,
    onClickAddArticles: () -> Unit,
    onClickCancelSelection: () -> Unit,
    onClickRemoveArticles: () -> Unit,
    onClickDeleteEnsemble: () -> Unit,
    modifier: Modifier = Modifier,
) {
  NoopBottomEndButtonContainer(modifier) {
    NoopExpandingIconButton(
      expanded = editMode != Disabled,
      collapsedIcon = IconData(NoopIcons.Edit, stringResource(R.string.enter_editing_mode)),
      expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
      verticalExpandedButtons =
      if(editMode == EnabledSelectedArticles) {
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
      onClick = { if(editMode != Disabled) onClickMinimizeButtonControl() else onClickEdit() } ,
    )
  }
}

@Composable
fun EnsembleDetailScreen(
    windowSizeClass: WindowSizeClass,
    title: String,
    editState: EditState,
    dialogState: EnsembleDetailViewModel.DialogState,
    editingTitle: Boolean,
    ensembleArticleThumbnailUris: LazyUriStrings,
    addArticleThumbnailUris: LazyUriStrings,
    selectedEditArticleIndices: Set<Int>,
    selectedAddArticleIndices: Set<Int>,
    onClickTitle: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onClickEdit: () -> Unit,
    onClickMinimizeButtonControl: () -> Unit,
    onClickAddArticles: () -> Unit,
    onClickArticle: (index: Int) -> Unit,
    onLongClickArticle: (index: Int) -> Unit,
    onClickRemoveArticles: () -> Unit,
    onClickCancelArticleSelection: () -> Unit,
    onClickDeleteEnsemble: () -> Unit,
    onDismissEditTitle: () -> Unit,
    onClickArticleAddDialog: (articleIndex: Int) -> Unit,
    onClickConfirmAddArticles: () -> Unit,
    onCloseAddArticlesDialog: () -> Unit,
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
  BackHandler(enabled = editingTitle, onBack = { onDismissEditTitle() })
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
              onClick = { if(!editingTitle) onClickTitle() }
            ),
      ) {
        if(editingTitle) {
          // TODO: Edit title text should be hoisted
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
          selectable = editState == EnabledSelectedArticles,
          onSelect = onClickArticle,
          onLongSelect = onLongClickArticle,
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
                  onClick = onDismissEditTitle
                )
          )
        }
      }
    }
  }
  EnsembleDetailFloatingActionButtons(
    editMode = editState,
    onClickEdit = onClickEdit,
    onClickAddArticles = onClickAddArticles,
    onClickCancelSelection = onClickCancelArticleSelection,
    onClickRemoveArticles = onClickRemoveArticles,
    onClickDeleteEnsemble = onClickDeleteEnsemble,
    onClickMinimizeButtonControl = onClickMinimizeButtonControl,
    modifier = Modifier.padding(start = systemBarStartPadding, end = systemBarEndPadding),
  )
  AddArticlesDialog(
    visible = dialogState == EnsembleDetailViewModel.DialogState.AddArticles,
    articleThumbnailUris = addArticleThumbnailUris,
    selectedArticleIndices = selectedAddArticleIndices,
    onSelectedArticle = onClickArticleAddDialog,
    onClose = onCloseAddArticlesDialog,
    onConfirm = onClickConfirmAddArticles,
  )
  DeleteEnsembleAlertDialog(
    visible = dialogState == EnsembleDetailViewModel.DialogState.DeleteEnsemble,
    onDismiss = onDismissDeleteEnsembleDialog,
    onClickPositive = onClickPositiveDeleteEnsembleDialog,
  )
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
          val addArticleThumbnailUri = articleThumbnailUris.getUriStrings(articleIndex)
          val selected = selectedArticleIndices.contains(articleIndex)
          Box(contentAlignment = Alignment.Center) {
            SelectableNoopImage(
              selectable = true,
              selected = selected,
              uriString = addArticleThumbnailUri.first(), // TODO: Animate between thumbnails?
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
    editState: EditState = Disabled,
    dialogState: EnsembleDetailViewModel.DialogState = EnsembleDetailViewModel.DialogState.None,
    selectedArticleIndices: Set<Int> = emptySet(),
    selectedAddArticleIndices: Set<Int> = emptySet(),
) = EnsembleDetailScreen(
  title = "Ensemble Title",
  editState = editState,
  dialogState = dialogState,
  editingTitle = editingTitle,
  ensembleArticleThumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
  addArticleThumbnailUris = lazyRepeatedThumbnailResourceIdsAsStrings,
  selectedEditArticleIndices = selectedArticleIndices,
  selectedAddArticleIndices = selectedAddArticleIndices,
  windowSizeClass = currentWindowAdaptiveInfo(),
  onClickTitle = {}, onClickMinimizeButtonControl = {}, onDismissDeleteEnsembleDialog = {}, onClickPositiveDeleteEnsembleDialog = {},
  onTitleChanged = {}, onClickEdit = {}, onClickAddArticles = {}, onClickArticle = {}, onLongClickArticle = {}, onClickRemoveArticles = {}, onClickCancelArticleSelection = {},
  onClickDeleteEnsemble = {}, onDismissEditTitle = {}, onClickArticleAddDialog = {}, onClickConfirmAddArticles = {}, onCloseAddArticlesDialog = {},
)

@Composable
fun PreviewUtilEnsembleDetailFloatingActionButtons(
    editMode: EditState = Disabled,
) = NoopTheme {
  EnsembleDetailFloatingActionButtons(
    editMode = editMode,
    onClickEdit = {}, onClickAddArticles = {}, onClickCancelSelection = {}, onClickRemoveArticles = {}, onClickDeleteEnsemble = {}, onClickMinimizeButtonControl = {},
  )
}

@Preview @Composable fun PreviewEnsembleDetailScreen() = NoopTheme { PreviewUtilEnsembleDetailScreen() }

@LandscapePreview @Composable fun PreviewEnsembleDetailScreen_EditingLandscape() = NoopTheme { PreviewUtilEnsembleDetailScreen(editState = EnabledGeneral) }

@Preview
@Composable
fun PreviewEnsembleDetailScreen_Editing() = NoopTheme {
  PreviewUtilEnsembleDetailScreen(
    editState = EnabledSelectedArticles,
    editingTitle = true,
    selectedArticleIndices = (0..repeatedThumbnailResourceIdsAsStrings.lastIndex step 2).toSet()
  )
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen_DeleteEnsembleAlertDialog() = NoopTheme {
  PreviewUtilEnsembleDetailScreen(
    editState = EnabledGeneral,
    dialogState = EnsembleDetailViewModel.DialogState.DeleteEnsemble,
  )
}

@Preview
@Composable
fun PreviewEnsembleDetailScreen_AddArticlesDialog() = NoopTheme {
  PreviewUtilEnsembleDetailScreen(
    editState = EnabledGeneral,
    editingTitle = false,
    dialogState = EnsembleDetailViewModel.DialogState.AddArticles,
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

@Preview @Composable fun PreviewEnsembleDetailFloatingActionButtons_Collapsed() = PreviewUtilEnsembleDetailFloatingActionButtons(Disabled)
@Preview @Composable fun PreviewEnsembleDetailFloatingActionButtons_Expanded() = PreviewUtilEnsembleDetailFloatingActionButtons(EnabledGeneral)
@Preview @Composable fun PreviewEnsembleDetailFloatingActionButtons_EditingArticles() = PreviewUtilEnsembleDetailFloatingActionButtons(EnabledSelectedArticles)
@Preview @Composable fun PreviewDeleteEnsembleAlertDialog() = NoopTheme { DeleteEnsembleAlertDialog(visible = true, onDismiss = {}, onClickPositive = {}) }
//endregion