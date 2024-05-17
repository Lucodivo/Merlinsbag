package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.inasweaterpoorlyknit.core.database.dao.ArticleWithImages
import com.inasweaterpoorlyknit.core.database.model.ArticleThumbnail
import com.inasweaterpoorlyknit.core.database.model.ArticleImageEntity
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.component.HorizontalOverlappingLayout
import com.inasweaterpoorlyknit.inknit.ui.component.IconData
import com.inasweaterpoorlyknit.inknit.ui.component.NoopAddEnsembleDialog
import com.inasweaterpoorlyknit.inknit.ui.component.NoopFloatingActionButton
import com.inasweaterpoorlyknit.inknit.ui.component.NoopImage
import com.inasweaterpoorlyknit.inknit.ui.isComposePreview
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.inknit.viewmodel.EnsemblesViewModel.Companion.MAX_ENSEMBLE_TITLE_LENGTH
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.inknit.viewmodel.Ensemble
import com.inasweaterpoorlyknit.inknit.viewmodel.EnsemblesViewModel
import com.inasweaterpoorlyknit.inknit.viewmodel.SaveEnsembleData

const val ENSEMBLES_ROUTE = "ensembles_route"

fun NavController.navigateToEnsembles(navOptions: NavOptions? = null) = navigate(ENSEMBLES_ROUTE, navOptions)

@Composable
fun EnsemblesRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    ensemblesViewModel: EnsemblesViewModel = hiltViewModel()
){
    val ensemblesUiState by ensemblesViewModel.ensemblesUiState.collectAsStateWithLifecycle()
    EnsemblesScreen (
        ensembles = ensemblesUiState.ensembles,
        showAddEnsembleDialog = ensemblesUiState.showAddEnsembleDialog,
        addEnsembleDialogArticles = ensemblesUiState.addEnsembleDialogArticles,
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
            for (article in ensemble.articles) {
                NoopImage(
                    uriString = article.thumbUri,
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
    addEnsembleDialogArticles: List<ArticleWithImages>,
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
        AddEnsembleDialog(
            visible = showAddEnsembleDialog,
            articles = addEnsembleDialogArticles,
            onClickSave = onClickSaveEnsemble,
            onClickClose = onCloseAddEnsembleDialog,
        )
    }
}

@Composable
fun AddEnsembleDialog(
    visible: Boolean,
    articles: List<ArticleWithImages>,
    onClickSave: (SaveEnsembleData) -> Unit,
    onClickClose: () -> Unit,
){
    BackHandler(enabled = visible){ onClickClose() }
    val (userInputTitle, setUserInputTitle) = remember { mutableStateOf("") }
    val selectedArticleIds = if(isComposePreview) remember { mutableSetOf("0", "1") } else remember { mutableSetOf() }
    NoopAddEnsembleDialog(
        visible = visible,
        title = stringResource(id = R.string.Add_article_ensemble),
        positiveButtonLabel = stringResource(id = R.string.Save),
        modifier = Modifier,
        onClose = onClickClose,
        onPositive = {
            onClickSave(
                SaveEnsembleData(
                    title = userInputTitle,
                    articleIds = selectedArticleIds.toList(),
                )
            )
            setUserInputTitle("")
        },
    ){
        Row {
            OutlinedTextField(
                value = userInputTitle,
                placeholder = { Text(text = stringResource(id = R.string.Goth_2_Boss)) },
                onValueChange = { updatedTitle ->
                    if(updatedTitle.length <= MAX_ENSEMBLE_TITLE_LENGTH){
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
            items(count = articles.size) { articleIndex ->
                val article = articles[articleIndex]
                Box(contentAlignment = Alignment.Center){
                    val (selected, setSelected) = remember { mutableStateOf(selectedArticleIds.contains(article.articleId)) }
                    NoopImage(
                        uriString = article.images[0].thumbUri,
                        contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
                        modifier = Modifier
                            .padding(padding)
                            .clickable {
                                if(selected) {
                                    selectedArticleIds.remove(article.articleId)
                                    setSelected(false)
                                } else {
                                    selectedArticleIds.add(article.articleId)
                                    setSelected(true)
                                }
                            }
                    )
                    if(selected) {
                        Icon(
                            imageVector = NoopIcons.SelectIndicator,
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
@Composable
fun __PreviewUtilEnsembleScreen(
    ensembles: List<Ensemble>,
    showAddEnsembleForm: Boolean,
) = EnsemblesScreen(ensembles = ensembles, showAddEnsembleDialog = showAddEnsembleForm, addEnsembleDialogArticles = emptyList(),
    onClickEnsemble = {}, onClickAddEnsemble = {}, onClickSaveEnsemble = {}, onCloseAddEnsembleDialog = {})

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
                articles = thumbnailUriStrings.map { ArticleThumbnail(articleId = "", thumbUri = it) },
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

@Preview
@Composable
fun PreviewAddEnsembleDialog(){
    fun createArticleImageEntity(index: Int, thumbnailUri: String) =
        ArticleWithImages(articleId = "$index", images = listOf(ArticleImageEntity(id = "img$index", articleId = "$index", uri = "", thumbUri = thumbnailUri)))

    NoopTheme{
        AddEnsembleDialog(
            visible = true,
            articles = listOf(
                createArticleImageEntity(0, R.raw.test_thumb_1.toString()),
                createArticleImageEntity(1, R.raw.test_thumb_2.toString()),
                createArticleImageEntity(2, R.raw.test_thumb_3.toString()),
                createArticleImageEntity(3, R.raw.test_thumb_4.toString()),
                createArticleImageEntity(4, R.raw.test_thumb_5.toString()),
                createArticleImageEntity(5, R.raw.test_thumb_6.toString()),
                createArticleImageEntity(6, R.raw.test_thumb_7.toString()),
                createArticleImageEntity(7, R.raw.test_thumb_8.toString()),
                createArticleImageEntity(8, R.raw.test_thumb_9.toString()),
            ),
            onClickSave = {},
            onClickClose = {},
        )
    }
}
//endregion