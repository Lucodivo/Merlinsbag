package com.inasweaterpoorlyknit.inknit.ui.screen

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.inknit.R
import com.inasweaterpoorlyknit.inknit.ui.theme.InKnitTheme

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
fun CollectionRow(collection: Collection){
    val contentResolver = LocalContext.current.contentResolver

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()){
            for (thumbnailUriString in collection.thumbnailUriStrings) {
                val uri = Uri.parse(thumbnailUriString)
                // TODO: Image
            }
            Text(collection.name)
        }
    }
}

@Composable
fun CollectionsScreen(collections: List<Collection>){
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(collections.size){ index ->
                CollectionRow(collections[index])
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
                resourceAsUriString(resId = R.raw.test_thumb_1),
                resourceAsUriString(resId = R.raw.test_thumb_2),
                resourceAsUriString(resId = R.raw.test_thumb_3),
                resourceAsUriString(resId = R.raw.test_thumb_4),
                resourceAsUriString(resId = R.raw.test_thumb_5),
            )
        ),
        Collection(
            name = "Collection 2",
            thumbnailUriStrings = listOf(
                resourceAsUriString(resId = R.raw.test_thumb_2),
                resourceAsUriString(resId = R.raw.test_thumb_4),
                resourceAsUriString(resId = R.raw.test_thumb_5),
                resourceAsUriString(resId = R.raw.test_thumb_6),
            )
        ),
        Collection(
            name = "Collection 3",
            thumbnailUriStrings = listOf(
                resourceAsUriString(resId = R.raw.test_thumb_3),
                resourceAsUriString(resId = R.raw.test_thumb_4),
                resourceAsUriString(resId = R.raw.test_thumb_5),
            )
        ),
        Collection(
            name = "Collection 4",
            thumbnailUriStrings = listOf(
                resourceAsUriString(resId = R.raw.test_thumb_4),
                resourceAsUriString(resId = R.raw.test_thumb_8),
            )
        ),
        Collection(
            name = "Collection 5",
            thumbnailUriStrings = listOf(
                resourceAsUriString(resId = R.raw.test_thumb_9),
            )
        ),
        Collection(
            name = "Collection 6",
            thumbnailUriStrings = listOf(
                resourceAsUriString(resId = R.raw.test_thumb_9),
                resourceAsUriString(resId = R.raw.test_thumb_8),
                resourceAsUriString(resId = R.raw.test_thumb_7),
                resourceAsUriString(resId = R.raw.test_thumb_6),
                resourceAsUriString(resId = R.raw.test_thumb_5),
                resourceAsUriString(resId = R.raw.test_thumb_4),
                resourceAsUriString(resId = R.raw.test_thumb_3),
                resourceAsUriString(resId = R.raw.test_thumb_2),
                resourceAsUriString(resId = R.raw.test_thumb_1),
            )
        ),
    )

    InKnitTheme{
        CollectionsScreen(debugCollections)
    }
}