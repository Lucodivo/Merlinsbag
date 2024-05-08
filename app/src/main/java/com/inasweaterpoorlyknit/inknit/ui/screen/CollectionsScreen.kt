package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.common.TODO_IMAGE_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.inknit.ui.component.ArticleThumbnailImage
import com.inasweaterpoorlyknit.inknit.ui.component.HorizontalOverlappingCollectionLayout
import com.inasweaterpoorlyknit.inknit.ui.component.IconData
import com.inasweaterpoorlyknit.inknit.ui.component.NoopFloatingActionButton
import com.inasweaterpoorlyknit.inknit.ui.repeatedThumbnailResourceIdsAsStrings
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.inknit.ui.theme.NoopTheme

const val COLLECTIONS_ROUTE = "collections_route"

fun NavController.navigateToCollections(navOptions: NavOptions? = null) = navigate(COLLECTIONS_ROUTE, navOptions)

@Composable
fun CollectionsRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
){
    val state = viewModel.state
    CollectionsScreen(
        collections = state.value.collections,
        showAddCollectionForm = state.value.showAddCollectionForm,
        onClickAddCollection = {
            viewModel.addCollection()
        }
    )
}

@Composable
fun CollectionRow(
    collection: Collection,
    modifier: Modifier = Modifier,
){
    val padding = 10.dp
    val maxThumbnailSize = 70.dp
    val overlapPercentage = 0.4f
    Card(
        modifier = modifier
    ){
        HorizontalOverlappingCollectionLayout(
            modifier = Modifier.padding(horizontal = padding),
            overlapPercentage = overlapPercentage,
        ) {
            for (thumbnailUriString in collection.thumbnailUriStrings) {
                ArticleThumbnailImage(
                    uriString = thumbnailUriString,
                    contentDescription = TODO_IMAGE_CONTENT_DESCRIPTION,
                    modifier = Modifier
                        .sizeIn(maxWidth = maxThumbnailSize, maxHeight = maxThumbnailSize)
                        .padding(top = padding)
                )
            }
        }
        Text(
            text = collection.name,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun CollectionsScreen(
    collections: List<Collection>,
    showAddCollectionForm: Boolean,
    onClickAddCollection: () -> Unit = {},
){
    val sidePadding = 10.dp
    val collectionsSpacing = 5.dp
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = sidePadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(collections.size){ index ->
                val topPadding = if(index == 0) collectionsSpacing * 2 else collectionsSpacing
                val bottomPadding = if(index == collections.lastIndex) collectionsSpacing * 2 else collectionsSpacing
                CollectionRow(
                    collection = collections[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = topPadding, bottom = bottomPadding)
                )
            }
        }
        NoopFloatingActionButton(
            iconData = IconData(NoopIcons.Add, TODO_ICON_CONTENT_DESCRIPTION),
            onClick = onClickAddCollection,
        )
        if (showAddCollectionForm) {

        }
    }
}

@Preview
@Composable
fun PreviewCollectionsScreen(){
    val thumbnails = repeatedThumbnailResourceIdsAsStrings
    NoopTheme {
        CollectionsScreen(
            collections = listOf(
                Collection(name = "Collection 1",
                    thumbnailUriStrings = thumbnails.slice(0..5)),
                Collection(name = "Collection 2",
                    thumbnailUriStrings = thumbnails.slice(1..7)),
                Collection(name = "Collection 3",
                    thumbnailUriStrings = thumbnails.slice(3..5)),
                Collection(name = "Collection 4",
                    thumbnailUriStrings = thumbnails.slice(4..4)),
                Collection(name = "Collection 5",
                    thumbnailUriStrings = thumbnails.slice(5..11)),
                Collection(name = "Collection 6",
                    thumbnailUriStrings = thumbnails.slice(6..16)),
                Collection(name = "Collection 7",
                    thumbnailUriStrings = thumbnails.slice(7..11)),
            ),
            showAddCollectionForm = false
        )
    }
}