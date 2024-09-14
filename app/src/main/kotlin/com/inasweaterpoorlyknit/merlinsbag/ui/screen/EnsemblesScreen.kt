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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.LazyUriStrings
import com.inasweaterpoorlyknit.core.ui.ARTICLE_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.DevicePreviews
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.accessoryDrawables
import com.inasweaterpoorlyknit.core.ui.bottomDrawables
import com.inasweaterpoorlyknit.core.ui.component.HorizontalOverlappingLayout
import com.inasweaterpoorlyknit.core.ui.component.IconButtonData
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomEndButtonContainer
import com.inasweaterpoorlyknit.core.ui.component.NoopBottomSheetDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopExpandingIconButton
import com.inasweaterpoorlyknit.core.ui.component.NoopImage
import com.inasweaterpoorlyknit.core.ui.component.NoopSearchBox
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.SelectableNoopImage
import com.inasweaterpoorlyknit.core.ui.component.shimmerBrush
import com.inasweaterpoorlyknit.core.ui.currentWindowAdaptiveInfo
import com.inasweaterpoorlyknit.core.ui.lazyRepeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.repeatedPlaceholderDrawables
import com.inasweaterpoorlyknit.core.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.core.ui.shoeDrawables
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.core.ui.topDrawables
import com.inasweaterpoorlyknit.merlinsbag.Constants
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesComposeViewModel
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEffect
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIEvent.*
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIState.*
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.EnsemblesUIState.DialogState.*
import kotlinx.serialization.Serializable
import kotlin.math.min

private val thumbnailsPadding = 10.dp
private val maxThumbnailSize = 70.dp
private const val overlapPercentage = 0.4f
private val minRowHeight = thumbnailsPadding * 4
private val rowVerticalSpacing = 2.dp
private val rowVerticalPadding = 8.dp
private val rowStartPadding = 16.dp
private val rowEndPadding = 4.dp
private val overlapTitleSpacing = 4.dp

@Serializable
object EnsemblesRouteArgs

fun NavController.navigateToEnsembles(navOptions: NavOptions? = null) = navigate(EnsemblesRouteArgs, navOptions)

@Composable
fun EnsemblesRoute(
    navigateToEnsembleDetail: (ensembleId: String) -> Unit,
    navigateToSettings: () -> Unit,
    windowSizeClass: WindowSizeClass,
    ensemblesViewModel: EnsemblesComposeViewModel = hiltViewModel(),
) {
  val lifecycle = LocalLifecycleOwner.current.lifecycle

  LaunchedEffect(Unit) {
    lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
      ensemblesViewModel.uiEffect.collect { uiEvent ->
        when(uiEvent){
          EnsemblesUIEffect.NavigateToSettings -> navigateToSettings()
          is EnsemblesUIEffect.NavigateToEnsembleDetail -> navigateToEnsembleDetail(uiEvent.ensembleId)
        }
      }
    }
  }

  val uiState by ensemblesViewModel.uiState.collectAsStateWithLifecycle()
  EnsemblesScreen(
    windowSizeClass = windowSizeClass,
    uiState = uiState,
    onUiEvent = ensemblesViewModel::onUiEvent,
  )
}


@Composable
fun EnsemblesScreen(
    windowSizeClass: WindowSizeClass,
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    uiState: EnsemblesUIState,
    onUiEvent: (EnsemblesUIEvent) -> Unit,
) {
  val layoutDir = LocalLayoutDirection.current

  BackHandler(enabled = uiState.handleBackPress){ onUiEvent(Back) }

  Surface(
    modifier = Modifier
        .fillMaxSize()
        .padding(
          top = systemBarPaddingValues.calculateTopPadding(),
          start = systemBarPaddingValues.calculateStartPadding(layoutDir),
          end = systemBarPaddingValues.calculateEndPadding(layoutDir)
        ),
  ) {
    EnsembleScreensMainContent(
      showPlaceholder = uiState.showPlaceholder,
      windowSizeClass = windowSizeClass,
      onUpdateSearchQuery = { onUiEvent(UpdateSearchQuery(it)) },
      onClickSettings = { onUiEvent(ClickSettings) },
      selectedEnsembleIndices = uiState.selectedEnsembleIndices,
      editMode = uiState.editMode,
      lazyEnsembleThumbnails = uiState.lazyEnsembles,
      onClickEnsemble = { onUiEvent(ClickEnsemble(it)) },
      onLongPressEnsemble = { onUiEvent(LongPressEnsemble(it)) },
      onClickAddEnsemble = { onUiEvent(ClickAddEnsemble) },
    )
    NoopBottomEndButtonContainer {
      EditEnsemblesExpandingActionButton(
        expanded = uiState.editMode,
        onClickAddEnsemble = { onUiEvent(ClickAddEnsemble) },
        onClickMinimizeButtonControl = { onUiEvent(ClickMinimizeButtonControl) },
        onClickDeleteSelected = { onUiEvent(ClickDeleteSelectedEnsembles) },
      )
    }
  }

  AddEnsembleDialog(
    visible = uiState.dialogState == AddEnsemble,
    articleThumbnails = uiState.addArticleThumbnails,
    onClickSave = { onUiEvent(ClickSaveAddEnsembleDialog(it)) },
    onClickClose = { onUiEvent(ClickCloseAddEnsembleDialog) },
    ensembleTitleError = uiState.newEnsembleTitleError,
    onClickArticle = { onUiEvent(ClickNewEnsembleArticle(it)) },
    selectedArticleIndices = uiState.selectedNewEnsembleArticles,
  )

  DeleteEnsemblesAlertDialog(
    visible = uiState.dialogState == DeleteEnsembleAlert,
    onDismiss = { onUiEvent(DismissDeleteEnsemblesAlertDialog) },
    onConfirm = { onUiEvent(ConfirmDeleteEnsemblesAlertDialog) },
  )
}

@Composable fun EnsembleScreensMainContent(
    showPlaceholder: Boolean,
    windowSizeClass: WindowSizeClass,
    onUpdateSearchQuery: (String) -> Unit,
    onClickSettings: () -> Unit,
    selectedEnsembleIndices: Set<Int>,
    editMode: Boolean,
    lazyEnsembleThumbnails: List<Pair<String, LazyUriStrings>>,
    onClickEnsemble: (index: Int) -> Unit,
    onLongPressEnsemble: (index: Int) -> Unit,
    onClickAddEnsemble: () -> Unit,
) {
  val sidePadding = 8.dp
  val bottomPadding = 4.dp
  val placeholderVisibilityAnimatedFloat by animateFloatAsState(
    targetValue = if(showPlaceholder) 1.0f else 0.0f,
    animationSpec = tween(durationMillis = 1000),
    label = "placeholder ensemble grid visibility"
  )
  Column(modifier = Modifier.padding(horizontal = sidePadding)) {
    if(placeholderVisibilityAnimatedFloat == 0.0f) {
      EnsemblesScreenSearchBox(
        windowSizeClass = windowSizeClass,
        onClickSettings = onClickSettings,
        onUpdateSearchQuery = onUpdateSearchQuery,
      )
      Spacer(modifier = Modifier.height(bottomPadding).fillMaxWidth())
      EnsemblesOverlappingImageRowColumn(
        windowSizeClass,
        selectedEnsembleIndices,
        selectable = editMode,
        lazyEnsembleThumbnails,
        onClickEnsemble,
        onLongPress = onLongPressEnsemble,
      )
    } else {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.alpha(placeholderVisibilityAnimatedFloat)
      ) {
        EnsemblesOverlappingPlaceholderRowColumn(windowSizeClass = windowSizeClass)
        if(lazyEnsembleThumbnails.isEmpty()){
          val buttonAlpha = 0.9f
          Button(
            onClick = onClickAddEnsemble,
            modifier = Modifier.alpha(buttonAlpha * placeholderVisibilityAnimatedFloat)
          ){
            Text(text = stringResource(R.string.add_ensemble))
          }
        }
      }
    }
  }
}

@Composable
private fun EnsemblesScreenSearchBox(
    windowSizeClass: WindowSizeClass,
    onClickSettings: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
){
  Row(verticalAlignment = Alignment.CenterVertically){
    if(windowSizeClass.compactWidth()){
      IconButton(onClick = onClickSettings) {
        Icon(
          imageVector = NoopIcons.Settings,
          contentDescription = stringResource(R.string.cog),
        )
      }
    }
    NoopSearchBox(
      placeholder = stringResource(R.string.search),
      onQueryChange = onUpdateSearchQuery,
      modifier = Modifier.fillMaxWidth(),
      maxQueryLength = Constants.MAX_ENSEMBLE_TITLE_LENGTH
    )
  }
}

@Composable
private fun EditEnsemblesExpandingActionButton(
    expanded: Boolean,
    onClickAddEnsemble: () -> Unit,
    onClickMinimizeButtonControl: () -> Unit,
    onClickDeleteSelected: () -> Unit,
) {
  NoopExpandingIconButton(
    expanded = expanded,
    collapsedIcon = IconData(NoopIcons.Add, stringResource(R.string.add_ensemble)),
    expandedIcon = IconData(NoopIcons.Remove, stringResource(R.string.exit_editing_mode)),
    onClick = { if(expanded) onClickMinimizeButtonControl() else onClickAddEnsemble() },
    verticalExpandedButtons = listOf(
      IconButtonData(IconData(NoopIcons.DeleteForever, stringResource(R.string.delete_selected_ensembles)), onClick = onClickDeleteSelected),
    )
  )
}

@Composable
private fun AddEnsembleDialog(
    visible: Boolean,
    articleThumbnails: LazyUriStrings,
    selectedArticleIndices: Set<Int>,
    onClickArticle: (index: Int) -> Unit,
    onClickSave: (title: String) -> Unit,
    onClickClose: () -> Unit,
    ensembleTitleError: Int?,
) {
  val (userInputTitle, setUserInputTitle) = rememberSaveable { mutableStateOf("") }
  NoopBottomSheetDialog(
    visible = visible,
    title = stringResource(id = R.string.add_ensemble),
    positiveButtonText = stringResource(id = R.string.save),
    positiveButtonEnabled = selectedArticleIndices.isNotEmpty() || userInputTitle.isNotEmpty(),
    onClose = {
      setUserInputTitle("")
      onClickClose()
    },
    onPositive = {
      setUserInputTitle("")
      onClickSave(userInputTitle)
    },
  ) {
    Column {
      OutlinedTextField(
        value = userInputTitle,
        placeholder = { Text(text = stringResource(id = R.string.goth_2_boss)) },
        onValueChange = { value ->
          setUserInputTitle(value.substring(0..(min(Constants.MAX_ENSEMBLE_TITLE_LENGTH, value.lastIndex))))
        },
        label = { Text(text = stringResource(id = R.string.ensemble_title)) },
        singleLine = true,
      )
      if(ensembleTitleError != null) {
        Text(
          text = "* ${stringResource(id = ensembleTitleError)}",
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
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
          val articleThumbnailUriString = articleThumbnails.getUriStrings(articleIndex)
          Box(contentAlignment = Alignment.Center) {
            SelectableNoopImage(
              selectable = true,
              selected = selectedArticleIndices.contains(articleIndex),
              uriString = articleThumbnailUriString.first(), // TODO: Animate between thumbnails?
              contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
              modifier = Modifier
                  .padding(padding)
                  .clickable{ onClickArticle(articleIndex) }
            )
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
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
  visible = visible,
  headerIcon = { Icon(NoopIcons.DeleteForever, REDUNDANT_CONTENT_DESCRIPTION) },
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
  val ensembleOverlappingImagesDescription = stringResource(R.string.ensemble_overlapping)
  RowCard(modifier = modifier.semantics { contentDescription = ensembleOverlappingImagesDescription }) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .heightIn(min = minRowHeight)
            .padding(
              start = rowStartPadding,
              end = rowEndPadding,
              top = rowVerticalPadding,
              bottom = rowVerticalPadding
            ),
      ) {
        HorizontalOverlappingLayout(overlapPercentage = overlapPercentage) {
          repeat(lazyUriStrings.size) { index ->
            NoopImage(
              uriString = lazyUriStrings.getUriStrings(index).first(), // TODO: Animate between thumbnails?
              contentDescription = ARTICLE_IMAGE_CONTENT_DESCRIPTION,
              modifier = Modifier.sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
            )
          }
        }
        if(title.isNotEmpty()) {
          if(lazyUriStrings.isNotEmpty()) Spacer(modifier = Modifier.height(overlapTitleSpacing))
          RowText(text = title, modifier = Modifier.fillMaxWidth())
        }
      }
      if(selectable) {
        Icon(
          imageVector = if(selected) NoopIcons.SelectedIndicator else NoopIcons.SelectableIndicator,
          contentDescription = stringResource(if(selected) com.inasweaterpoorlyknit.core.ui.R.string.selected else com.inasweaterpoorlyknit.core.ui.R.string.selectable),
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
        )
      }
    }
  }
}

@Composable
fun EnsemblesOverlappingImageRowColumn(
    windowSizeClass: WindowSizeClass,
    selectedEnsembleIndices: Set<Int>,
    selectable: Boolean,
    lazyTitleAndUriStrings: List<Pair<String, LazyUriStrings>>,
    onClick: (index: Int) -> Unit,
    onLongPress: (index: Int) -> Unit,
) {
  val lazyGridState = rememberLazyStaggeredGridState()
  // TODO: RowSize could be more versatile to accommodate better for tablet size screens
  val rowSize = if(windowSizeClass.compactWidth()) 1 else 2
  val widthPercent = 1.0 / rowSize
  LazyVerticalStaggeredGrid(
    columns = StaggeredGridCells.Fixed(rowSize),
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalItemSpacing = 2.dp,
    modifier = Modifier.fillMaxSize(),
    state = lazyGridState,
  ){
    items(lazyTitleAndUriStrings.size) { index ->
      val (rowTitle, lazyStrings) = lazyTitleAndUriStrings[index]
      key(rowTitle){ // ensemble titles are constrained to be unique
        EnsembleOverlappingImageRow(
          title = rowTitle,
          lazyUriStrings = lazyStrings,
          selected = selectedEnsembleIndices.contains(index),
          selectable = selectable,
          modifier = Modifier
              .combinedClickable(
                onClick = { onClick(index) },
                onLongClick = { onLongPress(index) }
              )
              .fillMaxWidth(widthPercent.toFloat())
        )
      }
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
            contentDescription = REDUNDANT_CONTENT_DESCRIPTION,
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
        Pair(R.string.tops, listOf(*topDrawables, *topDrawables)),
        Pair(R.string.shoes, listOf(*shoeDrawables, *shoeDrawables)),
        Pair(R.string.goth, listOf(*bottomDrawables, *topDrawables, *shoeDrawables, *accessoryDrawables)),
        Pair(R.string.hiking, thumbnails.slice(4..9)),
        Pair(R.string.accessories, thumbnails.slice(7..12)),
        Pair(R.string.vintage, listOf(*accessoryDrawables, *accessoryDrawables))
      )
    }

@Composable
fun EnsemblesOverlappingPlaceholderRowColumn(windowSizeClass: WindowSizeClass) {
  val maxPlaceholderRows = 20
  val rowSize = if(windowSizeClass.compactWidth()) 1 else 2
  val widthPercent = 1.0 / rowSize
  val ensemblesPlaceholderDescription = stringResource(R.string.ensemble_overlapping)
  Box(modifier = Modifier.semantics { contentDescription = ensemblesPlaceholderDescription }){
    val lazyGridState = rememberLazyStaggeredGridState()
    LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Fixed(rowSize),
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      verticalItemSpacing = 2.dp,
      modifier = Modifier.fillMaxSize(),
      state = lazyGridState,
    ){
      items(maxPlaceholderRows) { index ->
        val repeatedIndex = index % drawablePlaceholders.size
        val ensembles = drawablePlaceholders[repeatedIndex]
        EnsembleOverlappingPlaceholderRow(
          drawables = ensembles.second,
          title = stringResource(ensembles.first),
          modifier = Modifier
              .fillMaxWidth(widthPercent.toFloat())
              .padding(
                top = if(repeatedIndex != 0) rowVerticalSpacing else 0.dp,
                bottom = if(repeatedIndex != (maxPlaceholderRows - 1)) rowVerticalSpacing else 0.dp,
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
            override fun getUriStrings(index: Int): List<String> = listOf(thumbnailUriStrings[index])
          }
        )
      }
    }

@Composable
fun PreviewUtilEnsembleScreen(
    ensembles: List<Pair<String, LazyUriStrings>> = previewEnsembles,
    darkMode: Boolean = false,
    dialogState: DialogState = None,
    showPlaceholder: Boolean = false,
) = NoopTheme(darkMode = if(darkMode) DarkMode.DARK else DarkMode.LIGHT) {
  EnsemblesScreen(
    windowSizeClass = currentWindowAdaptiveInfo(),
    uiState = EnsemblesUIState(
      showPlaceholder = showPlaceholder,
      dialogState = dialogState,
      editMode = false,
      newEnsembleTitleError = null,
      selectedNewEnsembleArticles = setOf(0, 2),
      selectedEnsembleIndices = setOf(1, 3),
      addArticleThumbnails = lazyRepeatedThumbnailResourceIdsAsStrings,
      lazyEnsembles = ensembles,
    ),
    onUiEvent = {}
  )
}

@Composable
fun PreviewUtilAddEnsembleDialog(
    thumbnails: LazyUriStrings = lazyRepeatedThumbnailResourceIdsAsStrings,
    ensembleTitleError: Int? = null,
) = NoopTheme(DarkMode.DARK) {
  AddEnsembleDialog(
    visible = true,
    articleThumbnails = thumbnails,
    ensembleTitleError = ensembleTitleError,
    selectedArticleIndices = setOf(0, 1),
    onClickSave = {}, onClickClose = {}, onClickArticle = {}
  )
}

@Preview @Composable fun PreviewEnsembleOverlappingImageRow() = NoopTheme(darkMode = DarkMode.DARK) {
  EnsembleOverlappingImageRow(title = "Road Warrior", lazyUriStrings = previewEnsembles[1].second, selected = false, selectable = false, modifier = Modifier.fillMaxWidth())
}

@Preview @Composable fun PreviewEnsembleOverlappingImageRowOverflow() = NoopTheme {
  EnsembleOverlappingImageRow(title = "Road Warrior", lazyUriStrings = previewEnsembles[2].second, selected = true, selectable = true, modifier = Modifier.fillMaxWidth())
}

@Preview
@Composable
fun PreviewEnsembleOverlappingPlaceholderRow() = NoopTheme(darkMode = DarkMode.DARK) {
  EnsembleOverlappingPlaceholderRow(title = "Road Warrior", drawables = drawablePlaceholders[1].second, modifier = Modifier.fillMaxWidth())
}

@Preview
@Composable
fun PreviewEnsembleOverlappingPlaceholderRowOverflow() = NoopTheme {
  EnsembleOverlappingPlaceholderRow(title = "Road Warrior", drawables = drawablePlaceholders[2].second, modifier = Modifier.fillMaxWidth())
}

@DevicePreviews @Composable fun PreviewEnsembleScreen() = PreviewUtilEnsembleScreen(darkMode = true)
@DevicePreviews @Composable fun PreviewEnsembleScreen_Placeholders() = PreviewUtilEnsembleScreen(darkMode = true, showPlaceholder = true)

@Preview @Composable fun PreviewEnsemblesScreenAddEnsembleDialog() = PreviewUtilEnsembleScreen()

@Preview @Composable fun PreviewAddEnsembleDialog() = PreviewUtilAddEnsembleDialog()
@Preview @Composable fun PreviewAddEnsembleDialog_NoArticles() = PreviewUtilAddEnsembleDialog(thumbnails = LazyUriStrings.Empty)
@Preview @Composable fun PreviewAddEnsembleDialog_EnsembleTitleError() = PreviewUtilAddEnsembleDialog(ensembleTitleError = R.string.ensemble_with_title_already_exists)

@Preview @Composable fun PreviewDeleteEnsemblesAlertDialog() = NoopTheme { DeleteEnsemblesAlertDialog(visible = true, onConfirm = {}, onDismiss = {}) }
//endregion