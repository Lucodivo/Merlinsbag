package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.component.NoopImage
import com.inasweaterpoorlyknit.inknit.ui.component.HorizontalOverlappingLayout
import com.inasweaterpoorlyknit.inknit.ui.component.IconData
import com.inasweaterpoorlyknit.inknit.ui.component.NoopAddEnsembleDialog
import com.inasweaterpoorlyknit.inknit.ui.component.NoopFloatingActionButton
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme

const val ENSEMBLES_ROUTE = "ensembles_route"

fun NavController.navigateToEnsembles(navOptions: NavOptions? = null) = navigate(ENSEMBLES_ROUTE, navOptions)

@Composable
fun EnsembleRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    ensemblesViewModel: EnsemblesViewModel = hiltViewModel()
){
    val state = ensemblesViewModel.state
    EnsemblesScreen(
        ensembles = state.value.ensembles,
        showAddEnsembleForm = state.value.showAddEnsembleDialog,
        onClickAddEnsemble = ensemblesViewModel::onClickAddEnsemble,
        onClickSaveEnsemble = ensemblesViewModel::onClickSaveAddEnsembleDialog,
        onClickOutsideAddEnsembleDialog = ensemblesViewModel::onClickOutsideAddEnsembleDialog,
        onCloseAddEnsembleDialog = ensemblesViewModel::onClickCloseAddEnsembleDialog ,
    )
}

@Composable
fun EnsembleRow(
    ensemble: Ensemble,
    modifier: Modifier = Modifier,
){
    val padding = 10.dp
    val maxThumbnailSize = 70.dp
    val overlapPercentage = 0.4f
    Card(
        modifier = modifier
    ){
        HorizontalOverlappingLayout(
            modifier = Modifier.padding(horizontal = padding),
            overlapPercentage = overlapPercentage,
        ) {
            for (thumbnailUriString in ensemble.thumbnailUriStrings) {
                NoopImage(
                    uriString = thumbnailUriString,
                    contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
                    modifier = Modifier
                        .sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
                        .padding(top = padding)
                )
            }
        }
        Text(
            text = ensemble.name,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun EnsemblesScreen(
    ensembles: List<Ensemble>,
    showAddEnsembleForm: Boolean,
    onClickAddEnsemble: () -> Unit,
    onClickSaveEnsemble: (SaveEnsembleData) -> Unit,
    onClickOutsideAddEnsembleDialog: () -> Unit,
    onCloseAddEnsembleDialog: () -> Unit,
) {
    val sidePadding = 10.dp
    val ensembleSpacing = 5.dp
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
                val topPadding = if (index == 0) ensembleSpacing * 2 else ensembleSpacing
                val bottomPadding = if (index == ensembles.lastIndex) ensembleSpacing * 2 else ensembleSpacing
                EnsembleRow(
                    ensemble = ensembles[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = topPadding, bottom = bottomPadding)
                )
            }
        }
        NoopFloatingActionButton(
            iconData = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
            onClick = onClickAddEnsemble,
        )
        val dialogSlideIn by animateFloatAsState(
            targetValue = if (showAddEnsembleForm) 1.0f else 0.0f,
            label = "Add article dialog slidein animation float",
        )

        if (dialogSlideIn != 0.0f) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f * dialogSlideIn))
                    .clickable {
                        onClickOutsideAddEnsembleDialog()
                    }
            ) {
                NoopAddEnsembleDialog(
                    onClose = onCloseAddEnsembleDialog,
                    onPositive = { userInputData ->
                        onClickSaveEnsemble(
                            SaveEnsembleData(
                                title = userInputData.title,
                            )
                        )
                    },
                )
            }
        }
    }
}

//region COMPOSABLE PREVIEWS
@Composable
fun __PreviewUtilEnsembleScreen(
    ensembles: List<Ensemble>,
    showAddEnsembleForm: Boolean,
) = EnsemblesScreen(ensembles = ensembles, showAddEnsembleForm = showAddEnsembleForm, onClickAddEnsemble = {},
        onClickSaveEnsemble = {}, onClickOutsideAddEnsembleDialog = {}, onCloseAddEnsembleDialog = {})

@Preview
@Composable
fun PreviewEnsembleScreen(){
    val thumbnails = repeatedThumbnailResourceIdsAsStrings
    NoopTheme {
        __PreviewUtilEnsembleScreen(
            ensembles = listOf(
                Ensemble(name = "Ensemble 1",
                    thumbnailUriStrings = thumbnails.slice(0..5)),
                Ensemble(name = "Ensemble 2",
                    thumbnailUriStrings = thumbnails.slice(1..7)),
                Ensemble(name = "Ensemble 3",
                    thumbnailUriStrings = thumbnails.slice(3..5)),
                Ensemble(name = "Ensemble 4",
                    thumbnailUriStrings = thumbnails.slice(4..4)),
                Ensemble(name = "Ensemble 5",
                    thumbnailUriStrings = thumbnails.slice(5..11)),
                Ensemble(name = "Ensemble 6",
                    thumbnailUriStrings = thumbnails.slice(6..16)),
                Ensemble(name = "Ensemble 7",
                    thumbnailUriStrings = thumbnails.slice(7..11)),
            ),
            showAddEnsembleForm = false
        )
    }
}

@Preview
@Composable
fun PreviewEnsemblesScreenAddEnsembleDialog(){
    val thumbnails = repeatedThumbnailResourceIdsAsStrings
    NoopTheme {
        __PreviewUtilEnsembleScreen(
            ensembles = listOf(
                Ensemble(name = "Ensemble 1",
                    thumbnailUriStrings = thumbnails.slice(0..5)),
                Ensemble(name = "Ensemble 2",
                    thumbnailUriStrings = thumbnails.slice(1..7)),
                Ensemble(name = "Ensemble 3",
                    thumbnailUriStrings = thumbnails.slice(3..5)),
                Ensemble(name = "Ensemble 4",
                    thumbnailUriStrings = thumbnails.slice(4..4)),
                Ensemble(name = "Ensemble 5",
                    thumbnailUriStrings = thumbnails.slice(5..11)),
                Ensemble(name = "Ensemble 6",
                    thumbnailUriStrings = thumbnails.slice(6..16)),
                Ensemble(name = "Ensemble 7",
                    thumbnailUriStrings = thumbnails.slice(7..11)),
            ),
            showAddEnsembleForm = true,
        )
    }
}
//endregion