@file:OptIn(ExperimentalFoundationApi::class)
package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import com.google.common.primitives.Floats.max
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.ui.getActivity
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.OnboardingViewModel

// NOTE: Onboarding is NOT a Screen in the sense that it cannot be navigated too
//   It is UI that can be displayed.

@Composable
fun Onboarding(
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    // Onboarding landscape layout will probably never be a priority
    setPortraitOrientationOnly(context)
    onDispose {
      enableScreenOrientation(context)
    }
  }
  val pagerState = rememberPagerState(
    initialPage = 0,
    initialPageOffsetFraction = 0.0f,
    pageCount = { slides.size },
  )
  OnboardingInternal(
    pagerState = pagerState,
    onClickSkip = onboardingViewModel::onGetStarted,
    onClickGetStarted = onboardingViewModel::onGetStarted,
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

val slides: Array<@Composable () -> Unit> = arrayOf(
  { OnboardingSlideOne() },
  { OnboardingSlideTwo() },
  { OnboardingSlideThree() },
)

class HorizontalPagerIndicatorState(
    val pagerState: PagerState,
): PageIndicatorState {
  override val pageCount get() = pagerState.pageCount
  override val pageOffset get() = pagerState.currentPageOffsetFraction
  override val selectedPage get() = pagerState.currentPage
}

@Composable
private fun OnboardingInternal(
    pagerState: PagerState,
    onClickSkip: () -> Unit,
    onClickGetStarted: () -> Unit,
) {
  val pagerIndicatorSpacing = 8.dp
  val pagerIndicatorSize = 8.dp
  val pagerIndicatorPadding = 30.dp
  val skipButtonPadding = 10.dp
  val getStartedButtonVerticalPadding = pagerIndicatorPadding + 30.dp
  val getStartedButtonHorizontalPadding = 30.dp
  val pagerIndicatorState = remember { HorizontalPagerIndicatorState(pagerState) }
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxSize(),
    ) {
      HorizontalPager(
        state = pagerState
      ) { page ->
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.fillMaxSize()) {
          slides[page]()
        }
      }
      HorizontalPageIndicator(
        pageIndicatorState = pagerIndicatorState,
        selectedColor = MaterialTheme.colorScheme.onBackground,
        spacing = pagerIndicatorSpacing,
        indicatorSize = pagerIndicatorSize,
        modifier = Modifier.padding(pagerIndicatorPadding)
      )

      // Continue to App button
      val getStartedAlpha = max(0.0f, (pagerState.currentPage + pagerState.currentPageOffsetFraction) - (pagerState.pageCount - 2))
      if(getStartedAlpha > 0.0f){
        Box(
          contentAlignment = Alignment.BottomCenter,
          modifier = Modifier
              .fillMaxSize()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
          ) {
            Button(
              onClick = onClickGetStarted,
              modifier = Modifier
                  .alpha(getStartedAlpha)
                  .fillMaxWidth()
                  .padding(
                    vertical = getStartedButtonVerticalPadding,
                    horizontal = getStartedButtonHorizontalPadding,
                  ),
            ){
              Text(text = stringResource(R.string.get_started))
            }
          }
        }
      }

      // skip button
      if(getStartedAlpha < 1.0f){
        Box(
          contentAlignment = Alignment.BottomEnd,
          modifier = Modifier
              .fillMaxSize()
              .padding(skipButtonPadding)
        ){
          TextButton(
            onClick = onClickSkip,
            modifier = Modifier.alpha(1.0f - getStartedAlpha)
          ) {
            Text(
              text = stringResource(R.string.skip),
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}

private fun setPortraitOrientationOnly(context: Context) {
  val activity = context.getActivity()
  activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

private fun enableScreenOrientation(context: Context) {
  val activity = context.getActivity()
  activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
}

//region PREVIEW COMPOSABLES
@Composable
fun PreviewUtilOnboarding(
    selectedPage: Int = 0,
    offsetFraction: Float = 0.0f,
) = NoopTheme {
  Surface {
    OnboardingInternal(
      pagerState = rememberPagerState(
        initialPage = selectedPage,
        initialPageOffsetFraction = offsetFraction,
        pageCount = { slides.size },
      ),
      onClickSkip = {}, onClickGetStarted = {}
    )
  }
}

@Preview @Composable fun PreviewOnboarding() = PreviewUtilOnboarding()
@Preview @Composable fun PreviewOnboarding_LastSelectedMinus() = PreviewUtilOnboarding(selectedPage = slides.lastIndex, offsetFraction = -0.4f)
@Preview @Composable fun PreviewOnboarding_LastSelected() = PreviewUtilOnboarding(selectedPage = slides.lastIndex)
//endregion