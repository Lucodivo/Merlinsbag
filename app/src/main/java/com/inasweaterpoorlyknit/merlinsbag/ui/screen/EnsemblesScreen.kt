@file:OptIn(ExperimentalFoundationApi::class)

package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.component.HorizontalOverlappingLayout
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndButtonContainer
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomSheetDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopImage
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.SearchBox
import com.inasweaterpoorlyknit.core.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.core.ui.component.shimmerBrush
import com.inasweaterpoorlyknit.core.ui.isComposePreview
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedPlaceholderDrawables
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesViewModel
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesViewModel.Companion.MAX_ENSEMBLE_TITLE_LENGTH
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SaveEnsembleData

private val thumbnailsPadding = 10.dp
private val maxThumbnailSize = 70.dp
private const val overlapPercentage = 0.4f
private val minRowHeight = thumbnailsPadding * 4
private val rowVerticalSpacing = 2.dp
private val rowVerticalPadding = 8.dp
private val rowStartPadding = 16.dp
private val rowEndPadding = 4.dp
private val overlapTitleSpacing = 4.dp
const val ENSEMBLES_ROUTE = "ensembles_route"

fun NavController.navigateToEnsembles(navOptions: NavOptions? = null) = navigate(ENSEMBLES_ROUTE, navOptions)

@Composable
fun EnsemblesRoute(
    navController: NavController,
    ensemblesViewModel: EnsemblesViewModel = hiltViewModel(),
) {
  val lazyEnsembleThumbnails by ensemblesViewModel.lazyEnsembles.collectAsStateWithLifecycle()
  var showAddEnsembleDialog by remember { mutableStateOf(false) }
  val showPlaceholder by ensemblesViewModel.showPlaceholder.collectAsStateWithLifecycle()
  val addEnsembleDialogArticles by ensemblesViewModel.addArticleThumbnails.collectAsStateWithLifecycle()
  val selectedEnsembleIndices = remember { mutableStateMapOf<Int, Unit>() } // TODO: No mutableStateSetOf ??
  var showDeleteEnsembleAlertDialog by remember { mutableStateOf(false) }
  var editMode by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf(ensemblesViewModel.searchQuery) }
  fun toggleSelectedEnsemble(index: Int){
    if(selectedEnsembleIndices.contains(index)) selectedEnsembleIndices.remove(index)
    else selectedEnsembleIndices[index] = Unit
    if(selectedEnsembleIndices.isEmpty()) editMode = false
  }
  EnsemblesScreen(
    lazyEnsembleThumbnails = lazyEnsembleThumbnails,
    showAddEnsembleDialog = showAddEnsembleDialog,
    showDeleteEnsembleAlertDialog = showDeleteEnsembleAlertDialog,
    editMode = editMode,
    selectedEnsembleIndices = selectedEnsembleIndices.keys,
    showPlaceholder = showPlaceholder,
    searchQuery = searchQuery,
    addEnsembleDialogArticles = addEnsembleDialogArticles,
    onLongPressEnsemble = { index ->
      if(!editMode) {
        editMode = true
        selectedEnsembleIndices.clear()
      }
      toggleSelectedEnsemble(index)
    },
    onClickEnsemble = { index ->
      if(editMode) toggleSelectedEnsemble(index)
      else navController.navigateToEnsembleDetail(ensemblesViewModel.onClickEnsemble(index))
    },
    onClickActionButton = {
      if(editMode) editMode = false
      else showAddEnsembleDialog = true
    },
    onClickSaveEnsemble = { saveEnsembleData ->
      ensemblesViewModel.onClickSaveAddEnsembleDialog(saveEnsembleData)
      showAddEnsembleDialog = false
    },
    onCloseAddEnsembleDialog = { showAddEnsembleDialog = false },
    onUpdateSearchQuery = { newSearchQuery ->
      ensemblesViewModel.onSearchQueryUpdate(newSearchQuery)
      searchQuery = newSearchQuery
      editMode = false
    },
    onClearSearchQuery = {
      ensemblesViewModel.onSearchQueryUpdate("")
      searchQuery = ""
      editMode = false
    },
    onClickDeleteSelected = {
      showDeleteEnsembleAlertDialog = true
      editMode = false
    },
    onAlertDialogDismiss = { showDeleteEnsembleAlertDialog = false },
    onAlertDialogPositive = {
      ensemblesViewModel.deleteEnsembles(selectedEnsembleIndices.keys.toList())
      showDeleteEnsembleAlertDialog = false
    }
  )
}

@Composable
fun EnsemblesScreen(
    lazyEnsembleThumbnails: List<Pair<String, LazyUriStrings>>,
    showAddEnsembleDialog: Boolean,
    editMode: Boolean,
    showDeleteEnsembleAlertDialog: Boolean,
    selectedEnsembleIndices: Set<Int>,
    showPlaceholder: Boolean,
    addEnsembleDialogArticles: LazyUriStrings,
    searchQuery: String,
    onClickEnsemble: (index: Int) -> Unit,
    onLongPressEnsemble: (index: Int) -> Unit,
    onClickActionButton: () -> Unit,
    onClickSaveEnsemble: (SaveEnsembleData) -> Unit,
    onCloseAddEnsembleDialog: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onClearSearchQuery: () -> Unit,
    onClickDeleteSelected: () -> Unit,
    onAlertDialogDismiss: () -> Unit,
    onAlertDialogPositive: () -> Unit,
) {
  val sidePadding = 8.dp
  val topPadding = 4.dp
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    val placeholderVisibilityAnimatedFloat by animateFloatAsState(
      targetValue = if(showPlaceholder) 1.0f else 0.0f,
      animationSpec = tween(durationMillis = 1000),
      label = "placeholder ensemble grid visibility"
    )
    if(placeholderVisibilityAnimatedFloat == 0.0f) {
      Column {
        SearchBox(
          query = searchQuery,
          placeholder = stringResource(R.string.search_ensembles),
          onQueryChange = onUpdateSearchQuery,
          onClearQuery = onClearSearchQuery,
          modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = topPadding, horizontal = sidePadding),
        )
        EnsemblesOverlappingImageRowColumn(
          selectedEnsembleIndices,
          selectable = editMode,
          lazyEnsembleThumbnails,
          onClickEnsemble,
          onLongPress = onLongPressEnsemble,
          modifier = Modifier.padding(horizontal = sidePadding)
        )
      }
    } else {
      EnsemblesOverlappingPlaceholderRowColumn(modifier = Modifier.alpha(placeholderVisibilityAnimatedFloat))
    }
    NoopBottomEndButtonContainer {
      EditEnsemblesExpandingActionButton(
        expanded = editMode,
        onClickAddEnsemble = onClickActionButton,
        onClickDeleteSelected = onClickDeleteSelected,
      )
    }
    AddEnsembleDialog(
      visible = showAddEnsembleDialog,
      articleThumbnails = addEnsembleDialogArticles,
      onClickSave = onClickSaveEnsemble,
      onClickClose = onCloseAddEnsembleDialog,
    )
    if(showDeleteEnsembleAlertDialog) {
      DeleteEnsemblesAlertDialog(onDismiss = onAlertDialogDismiss, onConfirm = onAlertDialogPositive)
    }
  }
}

@Composable
private fun EditEnsemblesExpandingActionButton(
    expanded: Boolean,
    onClickAddEnsemble: () -> Unit,
    onClickDeleteSelected: () -> Unit,
) {
  NoopExpandingIconButton(
    expanded = expanded,
    collapsedIcon = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
    expandedIcon = IconData(NoopIcons.Remove, TODO_ICON_CONTENT_DESCRIPTION),
    onClick = onClickAddEnsemble,
    verticalExpandedButtons = listOf(
      IconButtonData(IconData(NoopIcons.Delete, TODO_ICON_CONTENT_DESCRIPTION)) { onClickDeleteSelected() },
    )
  )
}

@Composable
private fun AddEnsembleDialog(
    visible: Boolean,
    articleThumbnails: LazyUriStrings,
    onClickSave: (SaveEnsembleData) -> Unit,
    onClickClose: () -> Unit,
) {
  BackHandler(enabled = visible) { onClickClose() }
  val (userInputTitle, setUserInputTitle) = remember { mutableStateOf("") }
  val selectedArticleIndices =
      if(isComposePreview) remember { mutableStateMapOf(Pair(0,Unit), Pair(1,Unit)) }
      else remember { mutableStateMapOf() }
  NoopBottomSheetDialog(
    visible = visible,
    title = stringResource(id = R.string.add_ensemble),
    positiveButtonText = stringResource(id = R.string.save),
    positiveButtonEnabled = selectedArticleIndices.isNotEmpty() || userInputTitle.isNotEmpty(),
    onClose = onClickClose,
    onPositive = {
      onClickSave(
        SaveEnsembleData(
          title = userInputTitle,
          articleIndices = selectedArticleIndices.keys.toList(),
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
    if(articleThumbnails.isNotEmpty()) {
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
                      selectedArticleIndices[articleIndex] = Unit
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

@Composable
fun DeleteEnsemblesAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
  headerIcon = { Icon(NoopIcons.DeleteForever, TODO_ICON_CONTENT_DESCRIPTION) },
  title = stringResource(R.string.delete_ensembles),
  text = stringResource(R.string.are_you_sure),
  confirmText = stringResource(R.string.delete),
  cancelText = stringResource(R.string.cancel),
  onDismiss = onDismiss,
  onConfirm = onConfirm,
)

@Composable
private fun RowCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = ElevatedCard(modifier = modifier, shape = MaterialTheme.shapes.large, content = content)

@Composable
private fun RowText(text: String, modifier: Modifier = Modifier) = Text(text = text, color = MaterialTheme.colorScheme.primary, modifier = modifier)

@Composable
fun EnsembleOverlappingImageRow(
    title: String,
    lazyUriStrings: LazyUriStrings,
    selected: Boolean,
    selectable: Boolean,
    modifier: Modifier = Modifier,
) {
  RowCard(modifier = modifier) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .heightIn(min = minRowHeight)
            .padding(start = rowStartPadding, end = rowEndPadding, top = rowVerticalPadding, bottom = rowVerticalPadding),
      ) {
        HorizontalOverlappingLayout(
          overlapPercentage = overlapPercentage,
        ) {
          repeat(lazyUriStrings.size) { index ->
            NoopImage(
              uriString = lazyUriStrings.getUriString(index),
              contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
              modifier = Modifier.sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
            )
          }
        }
        if(title.isNotEmpty()) {
          if(lazyUriStrings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(overlapTitleSpacing))
          }
          RowText(text = title, modifier = Modifier.fillMaxWidth())
        }
      }
      if(selectable) {
        Icon(
          imageVector = if(selected) NoopIcons.SelectedIndicator else NoopIcons.SelectableIndicator,
          contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
        )
      }
    }
  }
}

@Composable
fun EnsemblesOverlappingImageRowColumn(
    selectedEnsembleIndices: Set<Int>,
    selectable: Boolean,
    lazyTitleAndUriStrings: List<Pair<String, LazyUriStrings>>,
    onClick: (index: Int) -> Unit,
    onLongPress: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyColumn(
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    items(lazyTitleAndUriStrings.size) { index ->
      val (rowTitle, lazyStrings) = lazyTitleAndUriStrings[index]
      EnsembleOverlappingImageRow(
        title = rowTitle,
        lazyUriStrings = lazyStrings,
        selected = selectedEnsembleIndices.contains(index),
        selectable = selectable,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
              top = if(index != 0) rowVerticalSpacing else 0.dp,
              bottom = if(index != lazyTitleAndUriStrings.lastIndex) rowVerticalSpacing else 0.dp,
            )
            .combinedClickable(
              onClick = { onClick(index) },
              onLongClick = { onLongPress(index) }
            )
      )
    }
  }
}

@Composable
fun EnsembleOverlappingPlaceholderRow(
    drawables: List<Int>,
    title: String,
    modifier: Modifier = Modifier,
) {
  val shimmerBrush = shimmerBrush(color = MaterialTheme.colorScheme.onSurface)
  RowCard(modifier = modifier) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.Start,
      modifier = Modifier
          .heightIn(min = minRowHeight)
          .padding(start = rowStartPadding, end = rowEndPadding, top = rowVerticalPadding, bottom = rowVerticalPadding),
    ) {
      HorizontalOverlappingLayout(
        modifier = Modifier
            .alpha(0.8f)
            .drawWithContent {
              drawContent()
              drawRect(shimmerBrush, blendMode = BlendMode.SrcIn)
            },
        overlapPercentage = overlapPercentage,
      ) {
        repeat(drawables.size) { index ->
          Icon(
            painter = painterResource(drawables[index]),
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
            modifier = Modifier
                .sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize),
          )
        }
      }
      Spacer(modifier = Modifier.height(overlapTitleSpacing))
      RowText(text = title)
    }
  }
}

private val drawablePlaceholders: List<Pair<Int, List<Int>>> =
    repeatedPlaceholderDrawables.let { thumbnails ->
      listOf(
        Pair(R.string.goth_2_boss, thumbnails.slice(0..5)),
        Pair(R.string.sporty_spice, thumbnails.slice(6..12)),
        Pair(R.string.derelicte, thumbnails.slice(1..13)),
        Pair(R.string.bowie_nite, thumbnails.slice(3..5)),
        Pair(R.string.road_warrior, thumbnails.slice(5..11)),
        Pair(R.string.chrome_country, thumbnails.slice(7..11)),
        Pair(R.string.rain_steam_and_speed, thumbnails.slice(12..17)),
        Pair(R.string.joseph_mallord_william_turner, thumbnails.slice(11..15)),
      )
    }

@Composable
fun EnsemblesOverlappingPlaceholderRowColumn(
    modifier: Modifier = Modifier,
) {
  LazyColumn(
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    items(drawablePlaceholders.size) { index ->
      val ensembles = drawablePlaceholders[index]
      EnsembleOverlappingPlaceholderRow(
        drawables = ensembles.second,
        title = stringResource(ensembles.first),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
              top = if(index != 0) rowVerticalSpacing else 0.dp,
              bottom = if(index != drawablePlaceholders.lastIndex) rowVerticalSpacing else 0.dp,
            )
      )
    }
  }
  // disable interactions with column by placing a transparent interactable scrim on top
  val scrimInteractionSource = remember { MutableInteractionSource() }
  Box(
    modifier = Modifier
        .fillMaxSize()
        .clickable(interactionSource = scrimInteractionSource, indication = null, onClick = {})
  )
}

//region COMPOSABLE PREVIEWS
val previewEnsembles: List<Pair<String, LazyUriStrings>> =
    repeatedThumbnailResourceIdsAsStrings.let { thumbnails ->
      listOf(
        thumbnails.slice(4..4),
        thumbnails.slice(0..5),
        thumbnails.slice(6..18),
        thumbnails.slice(1..12),
        emptyList(),
        emptyList(),
        thumbnails.slice(3..5),
        thumbnails.slice(5..11),
        thumbnails.slice(7..11),
        thumbnails.slice(12..17),
      ).mapIndexed { index, thumbnailUriStrings ->
        Pair(
          if(index == 3 || index == 4) "" else "Row ${index + 1}",
          object: LazyUriStrings {
            override val size: Int = thumbnailUriStrings.size
            private val articleThumbnailPaths: List<String> = thumbnailUriStrings
            override fun getUriString(index: Int): String = articleThumbnailPaths[index]
          }
        )
      }
    }

@Composable
fun PreviewUtilEnsembleScreen(
    ensembles: List<Pair<String, LazyUriStrings>>,
    showAddEnsembleForm: Boolean,
) = NoopTheme {
  EnsemblesScreen(
    lazyEnsembleThumbnails = ensembles,
    showAddEnsembleDialog = showAddEnsembleForm,
    editMode = false,
    showDeleteEnsembleAlertDialog = false,
    selectedEnsembleIndices = emptySet(),
    showPlaceholder = false, addEnsembleDialogArticles = lazyRepeatedThumbnailResourceIdsAsStrings, searchQuery = "Goth 2 Boss", onClickEnsemble = {},
    onClickActionButton = {}, onClickSaveEnsemble = {}, onCloseAddEnsembleDialog = {},
    onUpdateSearchQuery = {}, onClearSearchQuery = {},
    onClickDeleteSelected = {}, onAlertDialogDismiss = {}, onAlertDialogPositive = {}, onLongPressEnsemble = {},
  )
}

@Preview
@Composable
fun PreviewEnsembleOverlappingImageRow() = NoopTheme(darkMode = DarkMode.DARK) {
  EnsembleOverlappingImageRow(
    title = "Road Warrior",
    lazyUriStrings = previewEnsembles[1].second,
    selected = false,
    selectable = false,
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
fun PreviewEnsembleOverlappingImageRowOverflow() = NoopTheme {
  EnsembleOverlappingImageRow(
    title = "Road Warrior",
    lazyUriStrings = previewEnsembles[2].second,
    selected = true,
    selectable = true,
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
fun PreviewEnsembleOverlappingPlaceholderRow() = NoopTheme(darkMode = DarkMode.DARK) {
  EnsembleOverlappingPlaceholderRow(
    title = "Road Warrior",
    drawables = drawablePlaceholders[1].second,
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
fun PreviewEnsembleOverlappingPlaceholderRowOverflow() = NoopTheme {
  EnsembleOverlappingPlaceholderRow(
    title = "Road Warrior",
    drawables = drawablePlaceholders[2].second,
    modifier = Modifier.fillMaxWidth()
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

@Preview @Composable fun PreviewDeleteEnsemblesAlertDialog() = NoopTheme { DeleteEnsemblesAlertDialog(onConfirm = {}, onDismiss = {}) }
//endregion