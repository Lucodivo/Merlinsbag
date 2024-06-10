package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.navigation.navigateToAppStartDestination
import com.inasweaterpoorlyknit.merlinsbag.ui.TODO_ICON_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.merlinsbag.ui.component.IconData
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.merlinsbag.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.merlinsbag.ui.toast
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsViewModel

const val AUTHOR_WEBSITE_URL = "https://lucodivo.github.io/"
const val SOURCE_CODE_URL = "https://github.com/Lucodivo/Merlinsbag"
const val DELETE_ALL_CAPTCHA = "1234"

const val SETTINGS_ROUTE = "settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SETTINGS_ROUTE, navOptions)

@Composable
fun SettingsRoute(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current
  val showDeleteAllDataAlertDialog = remember{ mutableStateOf(false) }
  val userPreferences by settingsViewModel.userPreferences.collectAsState()

  LaunchedEffect(settingsViewModel.cacheClearedTrigger) {
    settingsViewModel.cacheClearedTrigger.collect {
      snackbarHostState.showSnackbar(message = context.getString(R.string.cache_cleared), withDismissAction = true)
    }
  }

  LaunchedEffect(settingsViewModel.allDataDeletedTrigger) {
    settingsViewModel.allDataDeletedTrigger.collect {
      context.toast(msg = context.getString(R.string.all_data_deleted))
      navController.navigateToAppStartDestination()
    }
  }

  SettingsScreen(
    showDeleteAllDataAlertDialog = showDeleteAllDataAlertDialog.value,
    darkMode = userPreferences.darkMode,
    onClickAuthor = { uriHandler.openUri(AUTHOR_WEBSITE_URL) },
    onClickSource = { uriHandler.openUri(SOURCE_CODE_URL) },
    onClickClearCache = {
      settingsViewModel.clearCache()
    },
    onClickDeleteAllData = { showDeleteAllDataAlertDialog.value = true },
    onClickDismissDeleteAllDataAlertDialog = { showDeleteAllDataAlertDialog.value = false },
    onClickConfirmDeleteAllDataAlertDialog = {
      showDeleteAllDataAlertDialog.value = false
      settingsViewModel.deleteAllData()
    },
    onClickDarkMode = { darkMode ->
      settingsViewModel.setDarkMode(darkMode)
    }
  )
}

@Composable
fun SettingsScreen(
    showDeleteAllDataAlertDialog: Boolean,
    darkMode: DarkMode,
    onClickAuthor: () -> Unit,
    onClickSource: () -> Unit,
    onClickClearCache: () -> Unit,
    onClickDeleteAllData: () -> Unit,
    onClickDismissDeleteAllDataAlertDialog: () -> Unit,
    onClickConfirmDeleteAllDataAlertDialog: () -> Unit,
    onClickDarkMode: (DarkMode) -> Unit,
) {
  val headerModifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
  val itemHorizontalPadding = 8.dp
  val itemModifier = Modifier.padding(vertical = 4.dp, horizontal = itemHorizontalPadding)
  LazyColumn(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
    modifier = Modifier.fillMaxSize(),
  ){
    item {
      Text(
        text = stringResource(R.string.info),
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        modifier = headerModifier,
      )
    }
    item {
      SettingsTextIconButton(
        text = stringResource(R.string.author),
        iconData = IconData(NoopIcons.Web, TODO_ICON_CONTENT_DESCRIPTION),
        onClick = onClickAuthor,
        modifier = itemModifier,
      )
    }
    item {
      SettingsTextIconButton(
        text = stringResource(R.string.source),
        iconData = IconData(NoopIcons.Code, TODO_ICON_CONTENT_DESCRIPTION),
        onClick = onClickSource,
        modifier = itemModifier,
      )
    }
    item {
      Text(
        text = stringResource(R.string.theme),
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        modifier = headerModifier,
      )
    }
    item {
      var expandedDarkModeMenu by remember { mutableStateOf(false) }
      Row(
        horizontalArrangement = Arrangement.End,
      ){
        val systemIconData = IconData(NoopIcons.SystemMode(), TODO_ICON_CONTENT_DESCRIPTION)
        val darkIconData = IconData(NoopIcons.LightMode, TODO_ICON_CONTENT_DESCRIPTION)
        val lightIconData = IconData(NoopIcons.DarkMode, TODO_ICON_CONTENT_DESCRIPTION)
        val iconData = when(darkMode){
          DarkMode.SYSTEM -> systemIconData
          DarkMode.LIGHT -> darkIconData
          DarkMode.DARK -> lightIconData
        }
        SettingsTextIconButton(
          text = stringResource(R.string.dark_mode),
          iconData = iconData,
          onClick = { expandedDarkModeMenu = !expandedDarkModeMenu },
          modifier = itemModifier,
        )
        DropdownMenu(
          expanded = expandedDarkModeMenu,
          onDismissRequest = { expandedDarkModeMenu = false },
          offset = DpOffset(itemHorizontalPadding, 0.dp),
        ){
          DropdownMenuItem(
            text = { Text(text = stringResource(R.string.system)) },
            trailingIcon = { Icon(imageVector = systemIconData.icon, contentDescription = systemIconData.contentDescription) },
            onClick = {
              onClickDarkMode(DarkMode.SYSTEM)
              expandedDarkModeMenu = false
            },
            modifier = itemModifier,
          )
          DropdownMenuItem(
            text = { Text(text = stringResource(R.string.light)) },
            trailingIcon = { Icon(imageVector = lightIconData.icon, contentDescription = lightIconData.contentDescription) },
            onClick = {
              onClickDarkMode(DarkMode.LIGHT)
              expandedDarkModeMenu = false
            },
            modifier = itemModifier,
          )
          DropdownMenuItem(
            text = { Text(text = stringResource(R.string.dark)) },
            trailingIcon = { Icon(imageVector = darkIconData.icon, contentDescription = darkIconData.contentDescription) },
            onClick = {
              onClickDarkMode(DarkMode.DARK)
              expandedDarkModeMenu = false
            },
            modifier = itemModifier,
          )
        }
      }
    }
    item {
      Text(
        text = stringResource(R.string.data),
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        modifier = headerModifier,
      )
    }
    item {
      SettingsTextIconButton(
        text = stringResource(R.string.clear_cache),
        iconData = IconData(NoopIcons.Clean, TODO_ICON_CONTENT_DESCRIPTION),
        onClick = onClickClearCache,
        modifier = itemModifier,
      )
    }
    item {
      SettingsTextIconButton(
        text = stringResource(R.string.delete_all_data),
        iconData = IconData(NoopIcons.DeleteForever, TODO_ICON_CONTENT_DESCRIPTION),
        onClick = onClickDeleteAllData,
        modifier = itemModifier,
        backgroundColor = MaterialTheme.colorScheme.error,
        textColor = MaterialTheme.colorScheme.onError,
      )
    }
  }
  if(showDeleteAllDataAlertDialog) {
    DeleteAllDataAlertDialog(
      onClickOutside = onClickDismissDeleteAllDataAlertDialog,
      onClickNegative = onClickDismissDeleteAllDataAlertDialog,
      onClickPositive = onClickConfirmDeleteAllDataAlertDialog,
    )
  }
}

@Composable
fun SettingsTextIconButton(
    text: String,
    iconData: IconData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
){
  Surface(
    tonalElevation = 1.dp,
    shadowElevation = 1.dp,
    shape = MaterialTheme.shapes.large,
    color = backgroundColor,
    modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp)
    ){
      Text(
        text = text,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
        color = textColor,
      )
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = iconData.icon,
        contentDescription = iconData.contentDescription,
        tint = textColor,
      )
    }
  }
}

@Composable
fun DeleteAllDataAlertDialog(
    onClickOutside: () -> Unit,
    onClickNegative: () -> Unit,
    onClickPositive: () -> Unit,
) {
  val enteredText = remember { mutableStateOf("") }
  val label: @Composable () -> Unit = {
    Row{
      Icon(imageVector = NoopIcons.Key, contentDescription = TODO_ICON_CONTENT_DESCRIPTION)
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = DELETE_ALL_CAPTCHA)
    }
  }
  AlertDialog(
    title = { Text(text = stringResource(id = R.string.delete_all_data)) },
    icon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = TODO_ICON_CONTENT_DESCRIPTION) },
    text = {
      Column{
        Text(text = stringResource(id = R.string.deleted_all_data_unrecoverable))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = enteredText.value,
          label = label,
          placeholder = {Text("1234")},
          onValueChange = { enteredText.value = it },
        )
      }
    },
    onDismissRequest = onClickOutside,
    confirmButton = {
      if(enteredText.value == DELETE_ALL_CAPTCHA) {
        TextButton(onClick = onClickPositive) {
          Text(stringResource(id = R.string.delete_all_data_positive))
        }
      } else {
        Box(contentAlignment = Alignment.Center) {
          TextButton(onClick = {}){} // Note: Simply to match height of delete button
          Icon(
            imageVector = NoopIcons.Lock,
            contentDescription = TODO_ICON_CONTENT_DESCRIPTION,
            tint = Color.Gray,
          )
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onClickNegative) {
        Text(stringResource(id = R.string.delete_all_data_negative))
      }
    }
  )
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilSettingsScreen(
    showDeleteAllDataAlertDialog: Boolean = false,
    darkMode: DarkMode = DarkMode.SYSTEM,
) = NoopTheme {
    SettingsScreen(
      showDeleteAllDataAlertDialog = false,
      darkMode = darkMode,
      onClickAuthor = {}, onClickSource = {}, onClickClearCache = {}, onClickDeleteAllData = {},
      onClickConfirmDeleteAllDataAlertDialog = {},
      onClickDismissDeleteAllDataAlertDialog = {},
      onClickDarkMode = {},
    )
  }

@Preview @Composable fun PreviewSettingsScreen() = PreviewUtilSettingsScreen()


@Preview @Composable fun PreviewSettingsScreen_AlertDialog() = PreviewUtilSettingsScreen(showDeleteAllDataAlertDialog = true, darkMode = DarkMode.LIGHT)

@Preview
@Composable
fun PreviewDeleteAllDataAlertDialog() = NoopTheme {
  DeleteAllDataAlertDialog(onClickPositive = {}, onClickNegative = {}, onClickOutside = {})
}
//endregion