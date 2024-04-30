package com.inasweaterpoorlyknit.inknit.ui.screen

import androidx.navigation.NavController
import androidx.navigation.NavOptions

// TODO: Outfits Screen

const val OUTFITS_ROUTE = "outfits_route"

fun NavController.navigateToOutfits(navOptions: NavOptions? = null) = navigate(OUTFITS_ROUTE, navOptions)
