package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.OnboardingViewModel

// NOTE: Onboarding is NOT a Screen in the sense that it cannot be navigated too
//   It is UI that can be displayed.

@Composable
fun Onboarding(
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
  OnboardingInternal(
    onClickSkip = {
      onboardingViewModel.onSkip()
    },
  )
}

@Composable
private fun OnboardingInternal(
    onClickSkip: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxSize(),
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
}

//region PREVIEW COMPOSABLES
@Preview
@Composable
fun PreviewOnboardingScreen() = NoopTheme {
  OnboardingInternal(
    onClickSkip = {}
  )
}
//endregion