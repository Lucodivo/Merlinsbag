package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
object TipsAndInfoRouteArgs

fun NavController.navigateToTipsAndInfo(navOptions: NavOptions? = null) = navigate(TipsAndInfoRouteArgs, navOptions)

@Composable
fun TipsAndInfoRoute(){
  TipsAndInfoScreen()
}

@Composable
fun TipsAndInfoScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
){
  val layoutDir = LocalLayoutDirection.current
  val spacerModifier = Modifier.padding(bottom = 8.dp)
  val buttonIconModifier = Modifier.padding(end = 8.dp)

  @Composable fun buttonIcon(
      icon: ImageVector,
      @StringRes description: Int,
      modifier: Modifier = Modifier
  ) = Row(modifier = modifier) {
      Icon(icon, REDUNDANT_CONTENT_DESCRIPTION, modifier = buttonIconModifier)
      Text(text = stringResource(description))
    }

  @Composable fun title(@StringRes text: Int, modifier: Modifier = spacerModifier){
    Text(text = stringResource(text), fontSize = MaterialTheme.typography.titleLarge.fontSize, modifier = modifier)
  }

  @Composable fun subtitle(text: String, modifier: Modifier = spacerModifier){
    Text(text = text, fontStyle = FontStyle.Italic, modifier = modifier)
  }

  @Composable fun subtitle(@StringRes text: Int, modifier: Modifier = spacerModifier){
    subtitle(text = stringResource(text), modifier = modifier)
  }

  @Composable fun errorSubtitle(@StringRes text: Int, modifier: Modifier = spacerModifier){
    subtitle(text = "\"${stringResource(text)}\"", modifier = modifier)
  }

  @Composable fun body(@StringRes msg: Int, modifier: Modifier = spacerModifier){
    Text(text = stringResource(msg), modifier = modifier)
  }

  LazyColumn(modifier = Modifier
      .fillMaxSize()
      .padding(start = systemBarPaddingValues.calculateStartPadding(layoutDir) + 16.dp, end = systemBarPaddingValues.calculateEndPadding(layoutDir) + 16.dp)
  ) {
    item{
      title(R.string.articles, modifier = spacerModifier.padding(top = systemBarPaddingValues.calculateTopPadding()))
      body(R.string.article_answer)
      title(R.string.ensembles)
      body(R.string.ensemble_answer)
    }
    item{
      title(R.string.controls)
      body(R.string.long_press_to_interact)
    }
    item{
      title(R.string.button_icons)
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
      buttonIcon(NoopIcons.Download, R.string.export_image_to_device, modifier = spacerModifier)
    }
    item{
      title(R.string.settings)
      subtitle(R.string.image_quality)
      body(R.string.image_quality_answer)
    }
    item {
      title(R.string.underlying_technology)
      body(R.string.underlying_technology_answer)
      title(R.string.what_is_a_quality_image)
      body(R.string.quality_image_answer)
    }
    item {
      title(R.string.messages)
      errorSubtitle(R.string.configuring_try_again_soon)
      body(R.string.ml_model_must_download)
      // TODO: Remove?
      errorSubtitle(R.string.sorry_try_again)
      body(R.string.article_extraction_is_hard_work, modifier = spacerModifier.padding(bottom = systemBarPaddingValues.calculateBottomPadding()))
    }
  }
}

//region COMPOSABLE PREVIEWS
@Preview(name = "__", device = "spec:shape=Normal,width=360,height=2800,unit=dp,dpi=480")
@Composable fun PreviewTipsAndInfoScreen() = NoopTheme(DarkMode.DARK) { TipsAndInfoScreen() }
//endregion