package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
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
import kotlin.math.max

const val ENSEMBLES_ROUTE = "ensembles_route"

fun NavController.navigateToEnsembles(navOptions: NavOptions? = null) = navigate(ENSEMBLES_ROUTE, navOptions)

@Composable
fun EnsembleRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    ensemblesViewModel: EnsemblesViewModel = hiltViewModel()
){
    val ensemblesState by ensemblesViewModel.ensemblesUiState.collectAsStateWithLifecycle()
    val showAddEnsembleDialog by ensemblesViewModel.showAddEnsembleDialog
    EnsemblesScreen(
        ensembles = ensemblesState,
        showAddEnsembleDialog = showAddEnsembleDialog,
        onClickAddEnsemble = ensemblesViewModel::onClickAddEnsemble,
        onClickSaveEnsemble = ensemblesViewModel::onClickSaveAddEnsembleDialog,
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
    showAddEnsembleDialog: Boolean,
    onClickAddEnsemble: () -> Unit,
    onClickSaveEnsemble: (SaveEnsembleData) -> Unit,
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

        val (userInputTitle, setUserInputTitle) = remember { mutableStateOf("") }
        NoopAddEnsembleDialog(
            visible = showAddEnsembleDialog,
            title = stringResource(id = R.string.Add_article_ensemble),
            positiveButtonLabel = stringResource(id = R.string.Save),
            modifier = Modifier,
            onClose = onCloseAddEnsembleDialog,
            onPositive = { onClickSaveEnsemble(SaveEnsembleData(title = userInputTitle)) },
        ){
            Row {
                OutlinedTextField(
                    value = userInputTitle,
                    placeholder = { Text(text = stringResource(id = R.string.Goth_2_Boss)) },
                    onValueChange = { setUserInputTitle(it) },
                    label = { Text(text = stringResource(id = R.string.Ensemble_title)) },
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
) = EnsemblesScreen(ensembles = ensembles, showAddEnsembleDialog = showAddEnsembleForm, onClickAddEnsemble = {},
        onClickSaveEnsemble = {}, onCloseAddEnsembleDialog = {})

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