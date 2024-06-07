package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.OnboardingViewModel

const val ONBOARDING_ROUTE = "onboarding_route"
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) = navigate(ONBOARDING_ROUTE, navOptions)

@Composable
fun OnboardingRoute(
    navController: NavController,
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
  LaunchedEffect(onboardingViewModel.onboardingComplete) {
    onboardingViewModel.onboardingComplete.collect{
      navController.popBackStack()
    }
  }
  OnboardingScreen(
    onClickSkip = {
      onboardingViewModel.onSkip()
    },
  )
}

@Composable
fun OnboardingScreen(
    onClickSkip: () -> Unit
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(text = "Onboarding Test")
      TextButton(onClick = onClickSkip) {
        Text(text = "Skip")
      }
    }
  }
}


@Preview
@Composable
fun PreviewOnboardingScreen() = NoopTheme {
  OnboardingScreen(
    onClickSkip = {}
  )
}