package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R

const val TIPS_AND_INFO_ROUTE = "tips_and_info_route"

fun NavController.navigateToTipsAndInfo(navOptions: NavOptions? = null) = navigate(TIPS_AND_INFO_ROUTE, navOptions)

@Composable
fun TipsAndInfoRoute(navController: NavController){
  TipsAndInfoScreen()
}

@Composable
fun TipsAndInfoScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
){
  val layoutDir = LocalLayoutDirection.current
  @Composable fun spacer() = Spacer(modifier = Modifier.height(8.dp))
  LazyColumn(modifier = Modifier
      .fillMaxSize()
      .padding(start = systemBarPaddingValues.calculateStartPadding(layoutDir) + 16.dp, end = systemBarPaddingValues.calculateEndPadding(layoutDir) + 16.dp)
  ) {
    item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding())) }
    item{
      Text(text = stringResource(R.string.articles), fontSize = MaterialTheme.typography.titleLarge.fontSize)
      spacer()
    }
    item{
      Text(text = stringResource(R.string.article_answer))
      spacer()
    }
    item{
      Text(text = stringResource(R.string.ensembles), fontSize = MaterialTheme.typography.titleLarge.fontSize)
      spacer()
    }
    item {
      Text(text = stringResource(R.string.ensemble_answer))
      spacer()
    }
    item{
      Text(text = stringResource(R.string.add_article_controls), fontSize = MaterialTheme.typography.titleLarge.fontSize)
      spacer()
    }
    item {
      Row {
        Icon(NoopIcons.FocusNarrow, stringResource(R.string.tighten_article_crop_region))
        Text(text = stringResource(R.string.tighten_the_cropping_area_around_the_article))
      }
    }
    item {
      Row {
        Icon(NoopIcons.FocusBroaden, stringResource(R.string.loosen_article_crop_region))
        Text(text = stringResource(R.string.broaden_the_cropping_area_around_the_article))
      }
    }
    item {
      Row {
        Icon(NoopIcons.RotateCCW, stringResource(R.string.rotate_ccw))
        Text(text = stringResource(R.string.rotate_the_article_counter_clockwise_by_90_degrees))
      }
    }
    item {
      Row {
        Icon(NoopIcons.RotateCW, stringResource(R.string.rotate_cw))
        Text(text = stringResource(R.string.rotate_the_article_clockwise_by_90_degrees))
      }
    }
    item {
      Row {
        Icon(NoopIcons.Delete, stringResource(R.string.delete))
        Text(text = stringResource(R.string.discard_the_article))
      }
    }
    item {
      Row {
        Icon(NoopIcons.Check, stringResource(R.string.save))
        Text(text = stringResource(R.string.confirm_catalog_the_article))
      }
      spacer()
    }
    item {
      Text(text = stringResource(R.string.underlying_technology), fontSize = MaterialTheme.typography.titleLarge.fontSize)
      spacer()
    }
    item {
      Text(text = stringResource(R.string.underlying_technology_answer))
      spacer()
    }
    item {
      Text(text = stringResource(R.string.what_is_a_quality_image), fontSize = MaterialTheme.typography.titleLarge.fontSize)
      spacer()
    }
    item {
      Text(text = stringResource(R.string.quality_image_answer))
      spacer()
    }
    item {
      Text(text = stringResource(R.string.messages), fontSize = MaterialTheme.typography.titleLarge.fontSize)
      spacer()
    }
    item {
      Text(text = "\"${stringResource(R.string.configuring_try_again_soon)}\"", fontStyle = FontStyle.Italic)
      Text(text = stringResource(R.string.ml_model_must_download))
      spacer()
    }
    item {
      Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateBottomPadding()))
    }
  }
}

//region COMPOSABLE PREVIEWS
@Preview(name = "__", device = "spec:shape=Normal,width=360,height=2800,unit=dp,dpi=480")
@Composable fun PreviewTipsAndInfoScreen() = NoopTheme(DarkMode.DARK) { TipsAndInfoScreen() }
//endregion