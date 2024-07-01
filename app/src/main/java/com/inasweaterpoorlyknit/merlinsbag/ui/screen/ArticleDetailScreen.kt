@file:OptIn(ExperimentalFoundationApi::class)

package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.Manifest.permission
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.data.model.LazyArticleThumbnails
import com.inasweaterpoorlyknit.core.database.model.ArticleWithThumbnails
import com.inasweaterpoorlyknit.core.database.model.Ensemble
import com.inasweaterpoorlyknit.core.database.model.ThumbnailFilename
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.ARTICLE_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.COMPOSE_ID
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndButtonContainer
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomSheetDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopImage
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.SearchBox
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.core.ui.repeatedFullResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.ArticleDetailViewModel

const val ARTICLE_INDEX_ARG = "articleIndex"
const val ARTICLE_DETAIL_ROUTE_BASE = "article_detail_route"
const val ARTICLE_DETAIL_ROUTE = "$ARTICLE_DETAIL_ROUTE_BASE?$ARTICLE_INDEX_ARG={$ARTICLE_INDEX_ARG}?$ENSEMBLE_ID_ARG={$ENSEMBLE_ID_ARG}"

val storagePermissionsRequired = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
private val REQUIRED_STORAGE_PERMISSIONS = if(storagePermissionsRequired) arrayOf(permission.WRITE_EXTERNAL_STORAGE) else emptyArray()

fun articleDetailRoute(articleIndex: Int, ensembleId: String? = null) = "${ARTICLE_DETAIL_ROUTE_BASE}?$ARTICLE_INDEX_ARG=$articleIndex?$ENSEMBLE_ID_ARG=$ensembleId"

fun NavController.navigateToArticleDetail(articleIndex: Int, ensembleId: String? = null, navOptions: NavOptions? = null) {
  navigate(articleDetailRoute(articleIndex, ensembleId), navOptions)
}

@Composable
fun ArticleDetailRoute(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    articleIndex: Int,
    filterEnsembleId: String?,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val articleDetailViewModel =
      hiltViewModel<ArticleDetailViewModel, ArticleDetailViewModel.ArticleDetailViewModelFactory> { factory ->
        factory.create(ensembleId = filterEnsembleId, articleIndex = articleIndex)
      }
  val settingsLauncher = rememberSettingsLauncher()
  val ensembleUiState by articleDetailViewModel.ensembleUiState.collectAsStateWithLifecycle()
  val lazyArticleImagesUris by articleDetailViewModel.articleLazyUriStrings.collectAsStateWithLifecycle()
  var editMode by remember { mutableStateOf(false) }
  var showDeleteArticleAlertDialog by remember { mutableStateOf(false) }
  var showPermissionsAlertDialog by remember { mutableStateOf(false) }
  var showRemoveFromEnsemblesAlertDialog by remember { mutableStateOf(false) }
  var showAddToEnsemblesDialog by remember { mutableStateOf(false) }
  val selectedEnsembles = remember { mutableStateMapOf<Int, Unit>() }
  var ensembleListState by remember { mutableStateOf(LazyListState()) }
  val filter by articleDetailViewModel.filter.collectAsStateWithLifecycle()
  val userSearch = remember { mutableStateOf("") }
  val pagerState = rememberPagerState(
    initialPage = articleIndex,
    initialPageOffsetFraction = 0.0f,
    pageCount = { lazyArticleImagesUris.size },
  )
  val articleBeingExported = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??
  val exportWithPermissionsCheckLauncher = rememberLauncherForActivityResultPermissions(
    onPermissionsGranted = {
      val index = pagerState.currentPage
      articleBeingExported[index] = Unit
      articleDetailViewModel.exportArticle(index)
    },
    onPermissionDenied = { navController.context.toast(R.string.storage_permissions_required) },
    onNeverAskAgain = { showPermissionsAlertDialog = true },
  )
  LaunchedEffect(articleDetailViewModel.articleExported) {
    articleDetailViewModel.articleExported.collect { (index, exportedImageUri) ->
      articleBeingExported.remove(index)
      when(snackbarHostState.showSnackbar(
        message = context.getString(R.string.image_exported),
        actionLabel = context.getString(R.string.open),
        duration = SnackbarDuration.Short,
      )) {
        SnackbarResult.Dismissed -> {}
        SnackbarResult.ActionPerformed -> {
          // TODO: There are other ways to open up an image URI that may need to be explored
          val intent = Intent().apply {
            setAction(Intent.ACTION_VIEW)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(exportedImageUri, "image/webp")
          }
          val pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
          pIntent.send()
        }
      }
    }
  }
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect { page ->
      articleDetailViewModel.onArticleFocus(page)
      ensembleListState = LazyListState()
      selectedEnsembles.clear()
      if(userSearch.value.isNotEmpty()) {
        articleDetailViewModel.searchEnsembles("")
        userSearch.value = ""
      }
    }
  }
  ArticleDetailScreen(
    windowSizeClass = windowSizeClass,
    filter = filter,
    articlesWithImages = lazyArticleImagesUris,
    articleEnsembleTitles = ensembleUiState.articleEnsembles.map { it.title }, // TODO: prevent mapping on every recomposition
    pagerState = pagerState,
    ensembleListState = ensembleListState,
    selectedEnsembles = selectedEnsembles.keys,
    editMode = editMode,
    exportingEnabled = !articleBeingExported.containsKey(pagerState.currentPage),
    showDeleteArticleAlertDialog = showDeleteArticleAlertDialog,
    showPermissionsAlertDialog = showPermissionsAlertDialog,
    showRemoveFromEnsemblesAlertDialog = showRemoveFromEnsemblesAlertDialog,
    showAddToEnsembleDialog = showAddToEnsemblesDialog,
    onClickEdit = {
      if(editMode) selectedEnsembles.clear()
      editMode = !editMode
    },
    onClickExport = { exportWithPermissionsCheckLauncher.launch(REQUIRED_STORAGE_PERMISSIONS) },
    onClickDelete = { showDeleteArticleAlertDialog = true },
    onDismissDeleteDialog = { showDeleteArticleAlertDialog = false },
    onConfirmDeleteDialog = {
      showDeleteArticleAlertDialog = false
      if(pagerState.pageCount == 1) navController.popBackStack()
      articleDetailViewModel.deleteArticle(pagerState.currentPage)
    },
    onDismissRemoveFromEnsemblesDialog = { showRemoveFromEnsemblesAlertDialog = false },
    onConfirmRemoveFromEnsemblesDialog = {
      articleDetailViewModel.removeArticleEnsembles(pagerState.currentPage, selectedEnsembles.keys.toList())
      selectedEnsembles.clear()
      showRemoveFromEnsemblesAlertDialog = false
      ensembleListState = LazyListState()
    },
    onDismissPermissionsDialog = { showPermissionsAlertDialog = false },
    onConfirmPermissionsDialog = {
      showPermissionsAlertDialog = false
      settingsLauncher.launch()
    },
    onClickEnsemble = {
      if(editMode) {
        if(selectedEnsembles.containsKey(it)) selectedEnsembles.remove(it)
        else selectedEnsembles[it] = Unit
      } else navController.navigateToEnsembleDetail(ensembleUiState.articleEnsembles[it].id)
    },
    onLongPressEnsemble = {
      if(!editMode) {
        selectedEnsembles.clear()
        editMode = true
      }
      if(selectedEnsembles.containsKey(it)) selectedEnsembles.remove(it)
      else selectedEnsembles[it] = Unit
    },
    onClickRemoveEnsembles = { showRemoveFromEnsemblesAlertDialog = true },
    onClickCancelEnsemblesSelection = { selectedEnsembles.clear() },
    onClickAddToEnsemble = { showAddToEnsemblesDialog = true },
    addNewEnsemble = { articleDetailViewModel.addArticleToNewEnsemble(pagerState.currentPage, it) },
    addArticleToEnsemble = { articleDetailViewModel.addArticleToEnsemble(pagerState.currentPage, it) },
    onCloseAddToEnsembleDialog = {
      showAddToEnsemblesDialog = false
      userSearch.value = ""
      articleDetailViewModel.searchEnsembles("")
    },
    onSearchQueryUpdateAddToEnsembles = {
      userSearch.value = it
      articleDetailViewModel.searchEnsembles(it)
    },
    ensemblesSearchQuery = userSearch.value,
    searchQueryUniqueTitle = ensembleUiState.searchIsUniqueTitle,
    ensemblesToAdd = ensembleUiState.searchEnsembles,
    modifier = modifier,
  )
}

@Composable
fun ArticleDetailScreen(
    windowSizeClass: WindowSizeClass,
    filter: String,
    articleEnsembleTitles: List<String>,
    pagerState: PagerState,
    ensembleListState: LazyListState,
    articlesWithImages: LazyUriStrings,
    editMode: Boolean,
    showDeleteArticleAlertDialog: Boolean,
    showPermissionsAlertDialog: Boolean,
    showRemoveFromEnsemblesAlertDialog: Boolean,
    showAddToEnsembleDialog: Boolean,
    onClickExport: () -> Unit,
    onClickDelete: () -> Unit,
    onClickRemoveEnsembles: () -> Unit,
    onClickCancelEnsemblesSelection: () -> Unit,
    onClickAddToEnsemble: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteDialog: () -> Unit,
    onConfirmRemoveFromEnsemblesDialog: () -> Unit,
    onDismissRemoveFromEnsemblesDialog: () -> Unit,
    onDismissPermissionsDialog: () -> Unit,
    onConfirmPermissionsDialog: () -> Unit,
    modifier: Modifier = Modifier,
    onClickEnsemble: (index: Int) -> Unit,
    onLongPressEnsemble: (index: Int) -> Unit,
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    exportingEnabled: Boolean,
    onClickEdit: () -> Unit,
    selectedEnsembles: Set<Int>,
    ensemblesToAdd: List<Ensemble>,
    addNewEnsemble: (String) -> Unit,
    onCloseAddToEnsembleDialog: () -> Unit,
    addArticleToEnsemble: (String) -> Unit,
    ensemblesSearchQuery: String,
    searchQueryUniqueTitle: Boolean,
    onSearchQueryUpdateAddToEnsembles: (String) -> Unit,
) {
  val layoutDir = LocalLayoutDirection.current
  val systemBarTopPadding = systemBarPaddingValues.calculateTopPadding()
  HorizontalPager(
    state = pagerState,
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier.sizeIn(minHeight = ButtonDefaults.MinHeight)
  ) { page ->
    NoopImage(
      uriString = articlesWithImages.getUriString(page),
      contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
      modifier = modifier
          .fillMaxSize()
          .padding(16.dp),
    )
  }
  val compactWidth = windowSizeClass.compactWidth()
  val iconModifier = Modifier.padding(start = if(compactWidth) 0.dp else 16.dp, end = 4.dp)
  if(filter.isNotEmpty()) {
    Box(
      contentAlignment = if(compactWidth) Alignment.TopCenter else Alignment.TopStart,
      modifier = Modifier
          .fillMaxSize()
          .padding(top = systemBarTopPadding)
    ) {
      Row {
        Icon(NoopIcons.ensembles(), stringResource(R.string.hashtag), modifier = iconModifier)
        Text(text = filter, textAlign = TextAlign.Start, fontSize = MaterialTheme.typography.titleLarge.fontSize)
      }
    }
  }
  val chipHeight = InputChipDefaults.Height
  Box(
    contentAlignment = if(compactWidth) Alignment.BottomStart else Alignment.TopEnd,
    modifier = Modifier.fillMaxSize()
  ) {
    val items: LazyListScope.() -> Unit = {
      items(articleEnsembleTitles.size) { i ->
        val inputChipInteractionSource = remember { MutableInteractionSource() }
        Box(
          contentAlignment = Alignment.CenterStart,
          modifier = Modifier
              .padding(horizontal = 2.dp)
              .height(chipHeight + 8.dp)
        ) {
          val selected = selectedEnsembles.contains(i)
          InputChip(
            selected = selected,
            label = { Text(text = articleEnsembleTitles[i]) },
            leadingIcon = {
              if(editMode) {
                if(selected) Icon(imageVector = NoopIcons.SelectedIndicator, contentDescription = stringResource(com.inasweaterpoorlyknit.core.ui.R.string.selected))
                else Icon(imageVector = NoopIcons.SelectableIndicator, contentDescription = stringResource(com.inasweaterpoorlyknit.core.ui.R.string.selectable))
              } else {
                Icon(imageVector = NoopIcons.ensembles(), contentDescription = stringResource(R.string.hashtag))
              }
            },
            onClick = {},
            interactionSource = inputChipInteractionSource,
            modifier = Modifier.height(InputChipDefaults.Height)
          )
          Box(
            modifier = Modifier
                .matchParentSize()
                .combinedClickable(
                  onLongClick = { onLongPressEnsemble(i) },
                  onClick = { onClickEnsemble(i) },
                  interactionSource = inputChipInteractionSource,
                  indication = null,
                )
          )
        }
      }
    }
    if(compactWidth) {
      LazyRow(state = ensembleListState) {
        items()
        item { Spacer(modifier = Modifier.fillParentMaxWidth(0.90f)) }
      }
    } else {
      LazyColumn(state = ensembleListState, horizontalAlignment = Alignment.End) {
        item { Spacer(modifier = Modifier.height(systemBarTopPadding)) }
        items()
        item { Spacer(modifier = Modifier.fillParentMaxHeight(0.90f)) }
      }
    }
  }
  FloatingActionButtonDetailScreen(
    expanded = editMode,
    exportingEnabled = exportingEnabled,
    removeEnsemblesEnabled = selectedEnsembles.isNotEmpty(),
    onClickEdit = onClickEdit,
    onClickDelete = onClickDelete,
    onClickExport = onClickExport,
    onClickRemoveEnsembles = onClickRemoveEnsembles,
    onClickCancelEnsemblesSelection = onClickCancelEnsemblesSelection,
    onClickAddToEnsemble = onClickAddToEnsemble,
    modifier = Modifier.padding(
      start = systemBarPaddingValues.calculateStartPadding(layoutDir),
      end = systemBarPaddingValues.calculateEndPadding(layoutDir),
      bottom = if(compactWidth) InputChipDefaults.Height else 0.dp,
    ),
  )
  if(showDeleteArticleAlertDialog) DeleteArticleAlertDialog(onDismiss = onDismissDeleteDialog, onConfirm = onConfirmDeleteDialog)
  if(showPermissionsAlertDialog) ExportPermissionsAlertDialog(onDismiss = onDismissPermissionsDialog, onConfirm = onConfirmPermissionsDialog)
  if(showRemoveFromEnsemblesAlertDialog) RemoveFromEnsemblesAlertDialog(onDismiss = onDismissRemoveFromEnsemblesDialog, onConfirm = onConfirmRemoveFromEnsemblesDialog)
  AddToEnsembleDialog(
    visible = showAddToEnsembleDialog,
    userSearch = ensemblesSearchQuery,
    ensemblesToAdd = ensemblesToAdd,
    addNewEnsemble = addNewEnsemble,
    addArticleToEnsemble = addArticleToEnsemble,
    onClose = onCloseAddToEnsembleDialog,
    onSearchQueryUpdate = onSearchQueryUpdateAddToEnsembles,
    searchQueryUniqueTitle = searchQueryUniqueTitle
  )
}

@Composable
fun FloatingActionButtonDetailScreen(
    expanded: Boolean,
    exportingEnabled: Boolean,
    removeEnsemblesEnabled: Boolean,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit,
    onClickExport: () -> Unit,
    onClickRemoveEnsembles: () -> Unit,
    onClickCancelEnsemblesSelection: () -> Unit,
    onClickAddToEnsemble: () -> Unit,
    modifier: Modifier = Modifier,
) {
  NoopBottomEndButtonContainer(extraPadding = PaddingValues(bottom = 4.dp, end = 8.dp), modifier = modifier) {
    NoopExpandingIconButton(
      expanded = expanded,
      collapsedIcon = IconData(NoopIcons.Edit, stringResource(R.string.enter_editing_mode)),
      expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
      onClick = onClickEdit,
      verticalExpandedButtons = if(removeEnsemblesEnabled) listOf(
        IconButtonData(
          icon = IconData(icon = NoopIcons.Cancel, contentDescription = stringResource(R.string.clear_selected_ensembles)),
          onClick = onClickCancelEnsemblesSelection
        ),
        IconButtonData(
          icon = IconData(NoopIcons.attachmentRemove(), stringResource(R.string.remove_article_from_selected_ensembles)),
          onClick = onClickRemoveEnsembles,
        ),
      ) else listOf(
        IconButtonData(
          icon = IconData(NoopIcons.DeleteForever, stringResource(R.string.delete_article)),
          onClick = { onClickDelete() }
        ),
        IconButtonData(
          icon = IconData(NoopIcons.Download, stringResource(R.string.export_article_image)),
          onClick = onClickExport,
          enabled = exportingEnabled,
        ),
        IconButtonData(
          icon = IconData(NoopIcons.Attachment, stringResource(R.string.attach_to_ensembles)),
          onClick = { onClickAddToEnsemble() }
        ),
      ),
      horizontalExpandedButtons = listOf(),
    )
  }
}

@Composable
fun DeleteArticleAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
      title = stringResource(id = R.string.delete_article),
      text = stringResource(id = R.string.deleted_articles_unrecoverable),
      headerIcon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.delete),
      cancelText = stringResource(id = R.string.cancel),
    )

@Composable
fun RemoveFromEnsemblesAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
      title = stringResource(id = R.string.remove_ensembles),
      text = stringResource(id = R.string.are_you_sure_remove_article_from_ensembles),
      headerIcon = { Icon(imageVector = NoopIcons.attachmentRemove(), contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.remove),
      cancelText = stringResource(id = R.string.cancel),
    )

@Composable
fun ExportPermissionsAlertDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) =
    NoopSimpleAlertDialog(
      title = stringResource(id = R.string.permission_alert_title),
      text = stringResource(id = R.string.export_permission_alert_justification),
      onDismiss = onDismiss,
      onConfirm = onConfirm,
      confirmText = stringResource(id = R.string.permission_alert_positive),
      cancelText = stringResource(id = R.string.permission_alert_negative),
      headerIcon = { Icon(imageVector = NoopIcons.Folder, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
    )

@Composable
private fun AddToEnsembleDialog(
    visible: Boolean,
    ensemblesToAdd: List<Ensemble>,
    addNewEnsemble: (String) -> Unit,
    addArticleToEnsemble: (String) -> Unit,
    onClose: () -> Unit,
    userSearch: String,
    onSearchQueryUpdate: (String) -> Unit,
    searchQueryUniqueTitle: Boolean,
) {
  val ensembleChipsHeight = 50.dp
  val chipRowPadding = PaddingValues(5.dp)
  val chipHorizontalPadding = 2.dp
  BackHandler(enabled = visible, onBack = onClose)
  NoopBottomSheetDialog(
    visible = visible,
    title = stringResource(id = R.string.attach_to_ensemble),
    positiveButtonEnabled = false,
    onClose = onClose,
  ) {
    Text(text = stringResource(id = R.string.ensembles))
    if(ensemblesToAdd.isNotEmpty()) {
      LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        contentPadding = chipRowPadding,
        modifier = Modifier.height(ensembleChipsHeight),
      ) {
        items(count = ensemblesToAdd.size) { ensembleIndex ->
          val ensemble = ensemblesToAdd[ensembleIndex]
          Box(contentAlignment = Alignment.Center) {
            AssistChip(
              label = { Text(text = ensemble.title) },
              leadingIcon = { Icon(imageVector = NoopIcons.ensembles(), contentDescription = stringResource(R.string.ensembles)) },
              onClick = { addArticleToEnsemble(ensemble.id) },
              modifier = Modifier.padding(horizontal = chipHorizontalPadding)
            )
          }
        }
      }
    } else {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(ensembleChipsHeight),
      ) {
        Text(
          text = stringResource(R.string.no_ensembles_available),
        )
      }
    }
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
          .fillMaxWidth()
          .height(ensembleChipsHeight),
    ) {
      if(ensemblesToAdd.isEmpty() || !ensemblesToAdd.first().equals(userSearch))
        InputChip(
          selected = searchQueryUniqueTitle,
          label = {
            Text(
              text = userSearch,
              style = if(searchQueryUniqueTitle) LocalTextStyle.current else TextStyle(textDecoration = TextDecoration.LineThrough),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          },
          leadingIcon = { Icon(imageVector = NoopIcons.ensembles(), contentDescription = stringResource(R.string.hashtag)) },
          trailingIcon = { Icon(imageVector = NoopIcons.Add, contentDescription = stringResource(R.string.add_ensemble)) },
          onClick = { if(searchQueryUniqueTitle) addNewEnsemble(userSearch) },
          modifier = Modifier.padding(horizontal = chipRowPadding.calculateStartPadding(LocalLayoutDirection.current) + chipHorizontalPadding)
        )
    }
    SearchBox(
      query = userSearch,
      placeholder = stringResource(R.string.search_ensembles),
      onQueryChange = { onSearchQueryUpdate(it) },
      onClearQuery = { onSearchQueryUpdate("") },
      modifier = Modifier
          .fillMaxWidth()
          .padding(15.dp),
    )
  }
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilArticleDetailScreen(
    filter: String = "",
    darkMode: Boolean = false,
    floatingActionButtonExpanded: Boolean = false,
    showDeleteArticleAlertDialog: Boolean = false,
    showPermissionsAlertDialog: Boolean = false,
    showRemoveFromEnsemblesAlertDialog: Boolean = false,
    showAddToEnsembleDialog: Boolean = false,
    selectedEnsembles: Set<Int> = emptySet(),
    addEnsembles: List<Ensemble> = emptyList(),
) = NoopTheme(darkMode = if(darkMode) DarkMode.DARK else DarkMode.LIGHT) {
  val articlesWithImages = LazyArticleThumbnails(
    directory = "",
    articleThumbnailPaths = listOf(
      ArticleWithThumbnails(
        articleId = COMPOSE_ID,
        thumbnailPaths = listOf(
          ThumbnailFilename(
            filenameThumb = repeatedFullResourceIdsAsStrings[0],
          ),
        ),
      )
    )
  )
  Surface {
    ArticleDetailScreen(
      windowSizeClass = currentWindowAdaptiveInfo(),
      filter = filter,
      articleEnsembleTitles = listOf("Road Warrior", "Goth 2 Boss", "John Prine", "Townes Van Zandt", "Deafheaven"),
      pagerState = rememberPagerState(initialPage = 0, pageCount = { articlesWithImages.size }),
      ensembleListState = rememberLazyListState(),
      articlesWithImages = articlesWithImages,
      editMode = floatingActionButtonExpanded,
      showDeleteArticleAlertDialog = showDeleteArticleAlertDialog,
      showPermissionsAlertDialog = showPermissionsAlertDialog,
      showRemoveFromEnsemblesAlertDialog = showRemoveFromEnsemblesAlertDialog,
      showAddToEnsembleDialog = showAddToEnsembleDialog, onClickExport = {}, onClickDelete = {}, onClickRemoveEnsembles = {}, onClickCancelEnsemblesSelection = {},
      onClickAddToEnsemble = {}, onDismissDeleteDialog = {}, onConfirmDeleteDialog = {}, onConfirmRemoveFromEnsemblesDialog = {}, onDismissRemoveFromEnsemblesDialog = {},
      onDismissPermissionsDialog = {}, onConfirmPermissionsDialog = {}, onClickEnsemble = {}, onLongPressEnsemble = {}, exportingEnabled = true, onClickEdit = {}, selectedEnsembles = selectedEnsembles,
      ensemblesToAdd = addEnsembles, addNewEnsemble = {}, onCloseAddToEnsembleDialog = {}, addArticleToEnsemble = {},
      ensemblesSearchQuery = "Goth 2 Boss",
      searchQueryUniqueTitle = true,
      onSearchQueryUpdateAddToEnsembles = {}
    )
  }
}

@PreviewScreenSizes @Composable fun PreviewArticleDetailScreen() = PreviewUtilArticleDetailScreen(filter = "Golden Girls")
@Preview @Composable fun PreviewArticleDetailScreen_expandedFAB() = PreviewUtilArticleDetailScreen(floatingActionButtonExpanded = true, darkMode = true)
@Preview @Composable fun PreviewArticleDetailScreen_deleteDialog() = PreviewUtilArticleDetailScreen(showDeleteArticleAlertDialog = true, floatingActionButtonExpanded = true, selectedEnsembles = setOf(1))
@Preview @Composable fun PreviewArticleDetailScreen_permissionsDialog() = PreviewUtilArticleDetailScreen(showPermissionsAlertDialog = true)
@Preview @Composable fun PreviewArticleDetailScreen_removeFromEnsemblesDialog() = PreviewUtilArticleDetailScreen(showRemoveFromEnsemblesAlertDialog = true)
@Preview @Composable fun PreviewArticleDetailScreen_addToEnsembleDialog() = PreviewUtilArticleDetailScreen(
  showAddToEnsembleDialog = true,
  addEnsembles = listOf(
    Ensemble(id = "1", title = "Road Warrior"),
    Ensemble(id = "2", title = "Goth 2 Boss"),
    Ensemble(id = "3", title = "Boy Wonder"),
    Ensemble(id = "4", title = "Deely Stan"),
  )
)

@Preview @Composable fun PreviewAddToEnsembleDialog() = NoopTheme(darkMode = DarkMode.DARK) {
  AddToEnsembleDialog(
    visible = true,
    ensemblesToAdd = listOf(
      Ensemble(id = "1", title = "Road Warrior"),
      Ensemble(id = "2", title = "Goth 2 Boss"),
      Ensemble(id = "3", title = "Boy Wonder"),
      Ensemble(id = "4", title = "Deely Stan"),
    ),
    searchQueryUniqueTitle = true,
    userSearch = "Jurassic Bark ggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg",
    onClose = {}, addArticleToEnsemble = {}, onSearchQueryUpdate = {}, addNewEnsemble = {},
  )
}
//endregion