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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
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
  var expandedDarkModeMenu by remember { mutableStateOf(false) }
  var clearCacheEnabled by remember { mutableStateOf(true) }
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
    expandDarkModeDropdownMenu = expandedDarkModeMenu,
    clearCacheEnabled = clearCacheEnabled,
    darkMode = userPreferences.darkMode,
    onClickAuthor = { uriHandler.openUri(AUTHOR_WEBSITE_URL) },
    onClickSource = { uriHandler.openUri(SOURCE_CODE_URL) },
    onClickClearCache = {
      clearCacheEnabled = false
      settingsViewModel.clearCache()
    },
    onClickDeleteAllData = { showDeleteAllDataAlertDialog.value = true },
    onClickDismissDeleteAllDataAlertDialog = { showDeleteAllDataAlertDialog.value = false },
    onClickConfirmDeleteAllDataAlertDialog = {
      showDeleteAllDataAlertDialog.value = false
      settingsViewModel.deleteAllData()
    },
    onSelectDarkMode = { darkMode ->
      settingsViewModel.setDarkMode(darkMode)
      expandedDarkModeMenu = false
    },
    onClickDarkMode = { expandedDarkModeMenu = !expandedDarkModeMenu },
    onDismissDarkMode = { expandedDarkModeMenu = false },
  )
}

val headerModifier = Modifier.padding(vertical = 4.dp, horizontal = 32.dp).fillMaxWidth()
val itemHorizontalPadding = 8.dp
val itemModifier = Modifier.padding(vertical = 4.dp, horizontal = itemHorizontalPadding)
val dividerHorizontalPadding = 16.dp
val dividerModifier = Modifier.padding(horizontal = dividerHorizontalPadding, vertical = 16.dp)

@Composable
fun AuthorRow(onClick: () -> Unit) = SettingsTextIconButton(
  text = stringResource(R.string.author),
  iconData = IconData(NoopIcons.Web, TODO_ICON_CONTENT_DESCRIPTION),
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun SourceRow(onClick: () -> Unit) = SettingsTextIconButton(
  text = stringResource(R.string.source),
  iconData = IconData(NoopIcons.Code, TODO_ICON_CONTENT_DESCRIPTION),
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun DarkModeRow(
    selectedDarkMode: DarkMode,
    expandedMenu: Boolean,
    onClick: () -> Unit,
    onSelectDarkMode: (DarkMode) -> Unit,
    onDismiss: () -> Unit)
{
  Column(
    horizontalAlignment = Alignment.End,
    modifier = itemModifier.fillMaxWidth(),
  ){
    val systemIconData = IconData(NoopIcons.SystemMode(), TODO_ICON_CONTENT_DESCRIPTION)
    val lightIconData = IconData(NoopIcons.LightMode, TODO_ICON_CONTENT_DESCRIPTION)
    val darkIconData = IconData(NoopIcons.DarkMode, TODO_ICON_CONTENT_DESCRIPTION)
    val iconData = when(selectedDarkMode){
      DarkMode.SYSTEM -> systemIconData
      DarkMode.LIGHT -> lightIconData
      DarkMode.DARK -> darkIconData
    }
    SettingsTextIconButton(
      text = stringResource(R.string.dark_mode),
      iconData = iconData,
      onClick = onClick,
    )
    Box {
      DropdownMenu(
        expanded = expandedMenu,
        onDismissRequest = onDismiss,
      ){
        DropdownMenuItem(
          text = { Text(text = stringResource(R.string.system)) },
          trailingIcon = { Icon(imageVector = systemIconData.icon, contentDescription = systemIconData.contentDescription) },
          onClick = { onSelectDarkMode(DarkMode.SYSTEM) },
        )
        DropdownMenuItem(
          text = { Text(text = stringResource(R.string.light)) },
          trailingIcon = { Icon(imageVector = lightIconData.icon, contentDescription = lightIconData.contentDescription) },
          onClick = { onSelectDarkMode(DarkMode.LIGHT) },
        )
        DropdownMenuItem(
          text = { Text(text = stringResource(R.string.dark)) },
          trailingIcon = { Icon(imageVector = darkIconData.icon, contentDescription = darkIconData.contentDescription) },
          onClick = { onSelectDarkMode(DarkMode.DARK) },
        )
      }
    }
  }
}

@Composable
fun ClearCacheRow(
    enabled: Boolean,
    onClick: () -> Unit
) = SettingsTextIconButton(
    enabled = enabled,
    text = stringResource(R.string.clear_cache),
    iconData = IconData(NoopIcons.Clean, TODO_ICON_CONTENT_DESCRIPTION),
    onClick = onClick,
    modifier = itemModifier,
  )

@Composable
fun DeleteAllDataRow(onClick: () -> Unit) {
  SettingsTextIconButton(
    text = stringResource(R.string.delete_all_data),
    iconData = IconData(NoopIcons.DeleteForever, TODO_ICON_CONTENT_DESCRIPTION),
    onClick = onClick,
    modifier = itemModifier,
    containerColor = MaterialTheme.colorScheme.error,
    contentColor = MaterialTheme.colorScheme.onError,
  )
}

@Composable
fun SettingsScreen(
    showDeleteAllDataAlertDialog: Boolean,
    expandDarkModeDropdownMenu: Boolean,
    clearCacheEnabled: Boolean,
    darkMode: DarkMode,
    onClickAuthor: () -> Unit,
    onClickSource: () -> Unit,
    onClickClearCache: () -> Unit,
    onClickDeleteAllData: () -> Unit,
    onClickDismissDeleteAllDataAlertDialog: () -> Unit,
    onClickConfirmDeleteAllDataAlertDialog: () -> Unit,
    onClickDarkMode: () -> Unit,
    onDismissDarkMode: () -> Unit,
    onSelectDarkMode: (DarkMode) -> Unit,
) {
  val dividerThickness = 2.dp
  LazyColumn(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
    modifier = Modifier.fillMaxSize(),
  ){
    item { Spacer(modifier = Modifier.height(16.dp)) }
    item { SettingsTitle(stringResource(R.string.info)) }
    item { AuthorRow(onClickAuthor) }
    item { SourceRow(onClickSource) }
    item { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) }
    item { SettingsTitle(stringResource(R.string.theme)) }
    item {
      DarkModeRow(
        selectedDarkMode = darkMode,
        expandedMenu = expandDarkModeDropdownMenu,
        onClick = onClickDarkMode,
        onSelectDarkMode = onSelectDarkMode,
        onDismiss = onDismissDarkMode,
      )
    }
    item { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) }
    item { SettingsTitle(stringResource(R.string.data)) }
    item { ClearCacheRow(clearCacheEnabled, onClickClearCache) }
    item { DeleteAllDataRow(onClickDeleteAllData) }
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
fun SettingsTitle(text: String) {
  Text(
    text = text,
    fontSize = MaterialTheme.typography.titleLarge.fontSize,
    textAlign = TextAlign.Center,
    modifier = headerModifier,
  )
}

@Composable
fun SettingsTextIconButton(
    text: String,
    iconData: IconData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
){
  val buttonDefaultColors = ButtonDefaults.elevatedButtonColors()
  val buttonColors = buttonDefaultColors.copy(
    containerColor = containerColor ?: buttonDefaultColors.containerColor,
    contentColor = contentColor ?: buttonDefaultColors.contentColor
  )
  ElevatedButton(
    onClick = onClick,
    enabled = enabled,
    shape = MaterialTheme.shapes.large,
    colors = buttonColors,
    modifier = modifier.fillMaxWidth()
  ){
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
          .padding(vertical = 8.dp)
          .fillMaxWidth(),
    ){
      Text(
        text = text,
        fontSize = MaterialTheme.typography.titleLarge.fontSize,
      )
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = iconData.icon,
        contentDescription = iconData.contentDescription,
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
    expandDarkModeDropdownMenu: Boolean = false,
    clearCacheEnabled: Boolean = true,
    darkMode: DarkMode = DarkMode.SYSTEM,
) = NoopTheme(darkTheme = darkMode) {
  Surface {
    SettingsScreen(
      showDeleteAllDataAlertDialog = showDeleteAllDataAlertDialog,
      expandDarkModeDropdownMenu = expandDarkModeDropdownMenu,
      clearCacheEnabled = clearCacheEnabled,
      darkMode = darkMode,
      onClickAuthor = {}, onClickSource = {}, onClickClearCache = {}, onClickDeleteAllData = {},
      onClickConfirmDeleteAllDataAlertDialog = {},
      onClickDismissDeleteAllDataAlertDialog = {},
      onClickDarkMode = {}, onSelectDarkMode = {}, onDismissDarkMode = {},
    )
  }
}

@Preview @Composable fun PreviewSettingsScreen() = PreviewUtilSettingsScreen(darkMode = DarkMode.DARK)
@Preview @Composable fun PreviewSettingsScreen_AlertDialog() = PreviewUtilSettingsScreen(showDeleteAllDataAlertDialog = true, darkMode = DarkMode.LIGHT
)

@Preview @Composable fun PreviewDeleteAllDataAlertDialog() = NoopTheme(darkTheme = DarkMode.DARK) {
  DeleteAllDataAlertDialog(onClickPositive = {}, onClickNegative = {}, onClickOutside = {})
}
//endregion