package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.R
import kotlinx.serialization.Serializable

@Serializable
object TipsAndInfoRoute

fun NavController.navigateToTipsAndInfo(navOptions: NavOptions? = null) = navigate(TipsAndInfoRoute, navOptions)

@Composable
fun TipsAndInfoRoute(){
  TipsAndInfoScreen()
}

@Composable
fun TipsAndInfoScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
){
  val layoutDir = LocalLayoutDirection.current
  val buttonIconModifier = Modifier.padding(end = 8.dp)
  @Composable fun spacer() = Spacer(modifier = Modifier.height(8.dp))
  @Composable fun buttonIcon(
      icon: ImageVector,
      @StringRes description: Int,
  ) = Row {
      Icon(icon, REDUNDANT_CONTENT_DESCRIPTION, modifier = buttonIconModifier)
      Text(text = stringResource(description))
    }
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
      Text(text = stringResource(R.string.button_icons), fontSize = MaterialTheme.typography.titleLarge.fontSize)
      spacer()
      buttonIcon(NoopIcons.Items, R.string.articles)
      buttonIcon(NoopIcons.ensembles(), R.string.ensembles)
      buttonIcon(NoopIcons.Settings, R.string.settings)
      buttonIcon(NoopIcons.Edit, R.string.edit)
      buttonIcon(NoopIcons.Add, R.string.add)
      buttonIcon(NoopIcons.AddPhotoAlbum, R.string.add_photo_from_device)
      buttonIcon(NoopIcons.AddPhotoCamera, R.string.add_photo_from_camera)
      buttonIcon(NoopIcons.FocusNarrow, R.string.tighten_cutout_region)
      buttonIcon(NoopIcons.FocusBroaden, R.string.loosen_cutout_region)
      buttonIcon(NoopIcons.RotateCCW, R.string.rotate_counterclockwise_90_degrees)
      buttonIcon(NoopIcons.RotateCW, R.string.rotate_clockwise_90_degrees)
      buttonIcon(NoopIcons.Check, R.string.confirm)
      buttonIcon(NoopIcons.Delete, R.string.discard)
      buttonIcon(NoopIcons.DeleteForever, R.string.delete)
      buttonIcon(NoopIcons.Cancel, R.string.cancel_selections)
      buttonIcon(NoopIcons.Remove, R.string.minimize_buttons)
      buttonIcon(NoopIcons.Attachment, R.string.attach_article_ensemble_or_image)
      buttonIcon(NoopIcons.attachmentRemove(), R.string.remove_attached_article_or_ensemble)
      buttonIcon(NoopIcons.Download, R.string.export_image_to_device)
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
      Text(text = "\"${stringResource(R.string.sorry_try_again)}\"", fontStyle = FontStyle.Italic)
      Text(text = stringResource(R.string.article_extraction_is_hard_work))
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