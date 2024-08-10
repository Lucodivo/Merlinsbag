@file:OptIn(ExperimentalFoundationApi::class)
package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import com.google.common.primitives.Floats.max
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.OnboardingViewModel

// NOTE: Onboarding is NOT a Screen in the sense that it cannot be navigated too
//   It is UI that can be displayed.
@Composable
fun Onboarding(
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    // Landscape layout for Onboarding will probably never be a priority
    setPortraitOrientationOnly(context)
    onDispose { enableScreenOrientation(context) }
  }
  val pagerState = rememberPagerState(
    initialPage = 0,
    initialPageOffsetFraction = 0.0f,
    pageCount = { slides.size },
  )
  OnboardingInternal(
    pagerState = pagerState,
    onClickSkip = onboardingViewModel::onCompletion,
    onClickGetStarted = onboardingViewModel::onCompletion,
  )
}

@Composable
private fun OnboardingSlide(
    title: String,
    text: String,
    contentDescription: String,
    @DrawableRes imageId: Int = R.drawable.onboarding_photographs,
){
  val slideHorizontalPadding = 32.dp
  val slideTitleSize = 32.sp
  val slideTextSize = 16.sp
  val slideTitleLetterSpacing = 2.sp
  val spacerTitleTextDividerHeight = 16.dp
  val horizontalTextPadding = 16.dp
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.padding(horizontal = slideHorizontalPadding)
  ) {
    Image(
      painter = painterResource(imageId),
      contentDescription = contentDescription,
      contentScale = ContentScale.FillWidth,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(spacerTitleTextDividerHeight))
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Bottom,
    ){
      Text(
        text = title,
        fontSize = slideTitleSize,
        letterSpacing = slideTitleLetterSpacing,
        color = MaterialTheme.colorScheme.primary,
      )
      Spacer(modifier = Modifier.height(spacerTitleTextDividerHeight))
      Text(
        text = text,
        fontSize = slideTextSize,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = horizontalTextPadding)
      )
    }
  }
}

@Composable
private fun OnboardingSlideOne(){
  OnboardingSlide(
    imageId = R.drawable.ic_launcher_color_foreground,
    title = stringResource(R.string.welcome),
    text = stringResource(R.string.merlinsbag_is_an_app_for_cataloging),
    contentDescription = stringResource(R.string.merlinsbag_icon_description)
  )
}

@Composable
private fun OnboardingSlideTwo(){
  OnboardingSlide(
    imageId = R.drawable.onboarding_photographs,
    title = stringResource(R.string.capture),
    text = stringResource(R.string.collect_photographs),
    contentDescription = stringResource(R.string.welcome_page_capture_description)
  )
}

@Composable
private fun OnboardingSlideThree(){
  OnboardingSlide(
    imageId = R.drawable.onboarding_articles,
    title = stringResource(R.string.virtualize),
    text = stringResource(R.string.merlinsbag_will_cut_out),
    contentDescription = stringResource(R.string.welcome_page_virtualize_description)
  )
}

@Composable
private fun OnboardingSlideFour(){
  OnboardingSlide(
    imageId = R.drawable.onboarding_ensembles,
    title = stringResource(R.string.organize),
    text = stringResource(R.string.categorize_your_keepsakes),
    contentDescription = stringResource(R.string.welcome_page_organize_description)
  )
}

val slides: Array<@Composable () -> Unit> = arrayOf(
  { OnboardingSlideOne() },
  { OnboardingSlideTwo() },
  { OnboardingSlideThree() },
  { OnboardingSlideFour() },
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
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
) {
  val pagerIndicatorSpacing = 8.dp
  val pagerIndicatorSize = 8.dp
  val getStartedButtonHorizontalPadding = 30.dp
  val pagerIndicatorState = remember { HorizontalPagerIndicatorState(pagerState) }
  val getStartedAlpha = max(0.0f, (pagerState.currentPage + pagerState.currentPageOffsetFraction) - (pagerState.pageCount - 2))
  val bottomPadding = systemBarPaddingValues.calculateBottomPadding()
  val skipButtonPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = bottomPadding + 10.dp)
  Surface(
    modifier = Modifier.fillMaxSize(),
  ) {
    Column (
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding),
    ) {
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1.0f)
      ) { page ->
        slides[page]()
      }
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
      ){
        HorizontalPageIndicator(
          pageIndicatorState = pagerIndicatorState,
          selectedColor = MaterialTheme.colorScheme.onSurface,
          spacing = pagerIndicatorSpacing,
          indicatorSize = pagerIndicatorSize,
          modifier = Modifier.sizeIn(maxHeight = pagerIndicatorSize)
        )
        ElevatedButton (
          onClick = if(getStartedAlpha > 0.0f) onClickGetStarted else { {} },
          modifier = Modifier
              .alpha(getStartedAlpha)
              .fillMaxWidth()
              .padding(horizontal = getStartedButtonHorizontalPadding),
        ){
          Text(text = stringResource(R.string.get_started))
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
@Preview @Composable fun PreviewOnboarding_Second() = PreviewUtilOnboarding(selectedPage = 1)
@Preview @Composable fun PreviewOnboarding_Third() = PreviewUtilOnboarding(selectedPage = 2)
@Preview @Composable fun PreviewOnboarding_LastSelected() = PreviewUtilOnboarding(selectedPage = slides.lastIndex)
@Preview @Composable fun PreviewOnboarding_halfway() = PreviewUtilOnboarding(selectedPage = slides.lastIndex - 1, offsetFraction = 0.5f)
//endregion