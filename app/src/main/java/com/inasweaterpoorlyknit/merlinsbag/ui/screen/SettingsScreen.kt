package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.common.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.component.IconData
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme

const val AUTHOR_WEBSITE_URL = "https://lucodivo.github.io/"
const val SOURCE_CODE_URL = "https://github.com/Lucodivo/Merlinsbag"

const val SETTINGS_ROUTE = "settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SETTINGS_ROUTE, navOptions)

@Composable
fun SettingsRoute(navController: NavController) = SettingsScreen()

@Composable
fun SettingsTextIconButton(
    text: String,
    iconData: IconData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
){
  Surface(
    tonalElevation = 1.dp,
    shadowElevation = 1.dp,
    shape = MaterialTheme.shapes.large,
    modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
  ) {
    Row(
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(8.dp)
    ){
      Text(
        text = text,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        fontWeight = FontWeight.Thin,
      )
      Spacer(modifier = Modifier.width(4.dp))
      Icon(imageVector = iconData.icon, contentDescription = iconData.contentDescription)
    }
  }
}

@Composable
fun SettingsScreen(
) {
  val headerModifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
  val itemModifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
  val uriHandler = LocalUriHandler.current
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
    modifier = Modifier.fillMaxWidth(),
  ){
    Text(
      text = stringResource(R.string.info),
      fontSize = MaterialTheme.typography.titleLarge.fontSize,
      modifier = headerModifier,
    )
    SettingsTextIconButton(
      text = stringResource(R.string.author),
      iconData = IconData(NoopIcons.Web, TODO_ICON_CONTENT_DESCRIPTION),
      onClick = { uriHandler.openUri(AUTHOR_WEBSITE_URL) },
      modifier = itemModifier,
    )
    SettingsTextIconButton(
      text = stringResource(R.string.source),
      iconData = IconData(NoopIcons.Code, TODO_ICON_CONTENT_DESCRIPTION),
      onClick = { uriHandler.openUri(SOURCE_CODE_URL) },
      modifier = itemModifier,
    )
  }
}

//region COMPOSABLE PREVIEWS
@Preview
@Composable
fun PreviewSettingsScreen() = NoopTheme {
  SettingsScreen()
}
//endregion