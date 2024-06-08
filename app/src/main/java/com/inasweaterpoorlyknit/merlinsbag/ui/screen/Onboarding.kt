@file:OptIn(ExperimentalFoundationApi::class)
package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import com.inasweaterpoorlyknit.merlinsbag.R
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
private fun OnboardingSlideOne(){
  Text(text = "Onboarding Slide 1")
}

@Composable
private fun OnboardingSlideTwo(){
  Text(text = "Onboarding Slide 2")
}

@Composable
private fun OnboardingSlideThree(){
  Text(text = "Onboarding Slide 3")
}

class HorizontalPagerIndicatorState(
    val pagerState: PagerState,
): PageIndicatorState {
  override val pageCount get() = pagerState.pageCount
  override val pageOffset get() = pagerState.currentPageOffsetFraction
  override val selectedPage get() = pagerState.currentPage
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
      val slides: Array<@Composable () -> Unit> = arrayOf(
        { OnboardingSlideOne() },
        { OnboardingSlideTwo() },
        { OnboardingSlideThree() },
      )
      val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0.0f,
        pageCount = { slides.size },
      )
      val pagerIndicatorState = remember { HorizontalPagerIndicatorState(pagerState) }
      HorizontalPager(
        state = pagerState
      ) { page ->
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.fillMaxSize()) {
          slides[page]()
        }
      }
      Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
      ){
        TextButton(onClick = onClickSkip) {
          Text(
            text = stringResource(R.string.skip),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
      Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
      ){
        HorizontalPageIndicator(
          pageIndicatorState = pagerIndicatorState,
          selectedColor = MaterialTheme.colorScheme.onBackground,
          spacing = 8.dp,
          indicatorSize = 8.dp
        )
      }
    }
  }
}

//region PREVIEW COMPOSABLES
@Preview
@Composable
fun PreviewOnboardingScreen() = NoopTheme {
  Surface {
    OnboardingInternal(
      onClickSkip = {}
    )
  }
}
//endregion