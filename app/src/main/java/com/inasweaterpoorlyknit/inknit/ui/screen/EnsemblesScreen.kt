package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
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

const val ENSEMBLES_ROUTE = "ensembles_route"

fun NavController.navigateToEnsembles(navOptions: NavOptions? = null) = navigate(ENSEMBLES_ROUTE, navOptions)

@Composable
fun EnsemblesRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    ensemblesViewModel: EnsemblesViewModel = hiltViewModel()
){
    val ensemblesState by ensemblesViewModel.ensemblesUiState.collectAsStateWithLifecycle()
    val showAddEnsembleDialog by ensemblesViewModel.showAddEnsembleDialog
    EnsemblesScreen(
        ensembles = ensemblesState,
        showAddEnsembleDialog = showAddEnsembleDialog,
        onClickEnsemble = { id ->
            navController.navigateToEnsembleDetail(ensembleId = id)
        },
        onClickAddEnsemble = ensemblesViewModel::onClickAddEnsemble,
        onClickSaveEnsemble = ensemblesViewModel::onClickSaveAddEnsembleDialog,
        onCloseAddEnsembleDialog = ensemblesViewModel::onClickCloseAddEnsembleDialog ,
    )
}

@Composable
fun EnsemblesRow(
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
    onClickEnsemble: (id: String) -> Unit,
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
) = EnsemblesScreen(ensembles = ensembles, showAddEnsembleDialog = showAddEnsembleForm, onClickEnsemble = {},
    onClickAddEnsemble = {}, onClickSaveEnsemble = {}, onCloseAddEnsembleDialog = {})

val previewEnsembles: List<Ensemble> =
    repeatedThumbnailResourceIdsAsStrings.let { thumbnails ->
        listOf(
            thumbnails.slice(0..5),
            thumbnails.slice(1..7),
            thumbnails.slice(3..5),
            thumbnails.slice(4..4),
            thumbnails.slice(5..11),
            thumbnails.slice(6..16),
            thumbnails.slice(7..11),
        ).mapIndexed { index, thumbnailUriStrings ->
            Ensemble(
                id = index.toString(),
                name = "Ensemble $index",
                thumbnailUriStrings = thumbnailUriStrings,
            )
        }
    }

@Preview
@Composable
fun PreviewEnsembleScreen(){
    val thumbnails = repeatedThumbnailResourceIdsAsStrings
    NoopTheme {
        __PreviewUtilEnsembleScreen(
            ensembles = previewEnsembles,
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
            ensembles = previewEnsembles,
            showAddEnsembleForm = true,
        )
    }
}
//endregion