package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavOptions

// TODO: Collections Screen

const val COLLECTIONS_ROUTE = "collections_route"

fun NavController.navigateToCollections(navOptions: NavOptions? = null) = navigate(COLLECTIONS_ROUTE, navOptions)

@Composable
fun CollectionsRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
){
    CollectionsScreen()
}

@Composable
fun CollectionsScreen(){

}

