package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.theme.AppTheme

// TODO: Collections Screen

const val COLLECTIONS_ROUTE = "collections_route"

fun NavController.navigateToCollections(navOptions: NavOptions? = null) = navigate(COLLECTIONS_ROUTE, navOptions)

@Composable
fun CollectionsRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
){
    val state = viewModel.state
    CollectionsScreen(state.value.collections)
}


@Composable
fun CollectionRow(
    collection: Collection,
    modifier: Modifier = Modifier,
){
    val padding = 10.dp
    Card(
        modifier = modifier
    ){
        OverlappingCollectionLayout(
            modifier = Modifier
                .padding(horizontal = padding),
            overlapPercentage = 0.6f,
        ) {
            for (thumbnailUriString in collection.thumbnailUriStrings) {
                ArticleThumbnailImage(
                    uriString = thumbnailUriString,
                    modifier = Modifier
                        .size(70.dp)
                        .padding(top = padding)
                )
            }
        }
        Row {
            Text(
                text = collection.name,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun CollectionsScreen(collections: List<Collection>){
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
                        .padding(
                            top = topPadding,
                            bottom = bottomPadding,
                        )
                )
            }
        }
    }
}

@Preview
@Composable
fun CollectionsScreenPreview(){
    val debugCollections = listOf(
        Collection(
            name = "Collection 1",
            thumbnailUriStrings = listOf(
                R.raw.test_thumb_1.toString(),
                R.raw.test_thumb_2.toString(),
                R.raw.test_thumb_3.toString(),
                R.raw.test_thumb_4.toString(),
                R.raw.test_thumb_5.toString(),
            )
        ),
        Collection(
            name = "Collection 2",
            thumbnailUriStrings = listOf(
                R.raw.test_thumb_2.toString(),
                R.raw.test_thumb_4.toString(),
                R.raw.test_thumb_5.toString(),
                R.raw.test_thumb_6.toString(),
            )
        ),
        Collection(
            name = "Collection 3",
            thumbnailUriStrings = listOf(
                R.raw.test_thumb_3.toString(),
                R.raw.test_thumb_4.toString(),
                R.raw.test_thumb_5.toString(),
            )
        ),
        Collection(
            name = "Collection 4",
            thumbnailUriStrings = listOf(
                R.raw.test_thumb_4.toString(),
                R.raw.test_thumb_8.toString(),
            )
        ),
        Collection(
            name = "Collection 5",
            thumbnailUriStrings = listOf(
                R.raw.test_thumb_9.toString(),
            )
        ),
        Collection(
            name = "Collection 6",
            thumbnailUriStrings = listOf(
                R.raw.test_thumb_9.toString(),
                R.raw.test_thumb_8.toString(),
                R.raw.test_thumb_7.toString(),
                R.raw.test_thumb_7.toString(),
                R.raw.test_thumb_6.toString(),
                R.raw.test_thumb_5.toString(),
                R.raw.test_thumb_4.toString(),
                R.raw.test_thumb_3.toString(),
                R.raw.test_thumb_2.toString(),
                R.raw.test_thumb_1.toString(),
                R.raw.test_thumb_6.toString(),
                R.raw.test_thumb_5.toString(),
                R.raw.test_thumb_4.toString(),
                R.raw.test_thumb_3.toString(),
                R.raw.test_thumb_2.toString(),
                R.raw.test_thumb_1.toString(),
            )
        ),
    )

    AppTheme {
        CollectionsScreen(debugCollections)
    }
}