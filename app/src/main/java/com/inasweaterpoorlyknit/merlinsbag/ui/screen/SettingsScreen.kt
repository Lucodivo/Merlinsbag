package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.HighContrast
import com.inasweaterpoorlyknit.core.model.Typography
import com.inasweaterpoorlyknit.core.ui.LargeFontSizePreview
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.SystemUiPreview
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopAlertDialog
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.core.ui.theme.scheme.NoopColorSchemes
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.navigation.navigateToAppStartDestination
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsViewModel

private val headerModifier = Modifier.fillMaxWidth()
private val itemHorizontalPadding = 8.dp
private val itemModifier = Modifier.padding(horizontal = itemHorizontalPadding)
private val dividerHorizontalPadding = itemHorizontalPadding * 2
private val dividerModifier = Modifier.padding(horizontal = dividerHorizontalPadding, vertical = 8.dp)
private val dividerThickness = 2.dp
private val sectionSpacerHeight = 16.dp
@Composable private fun settingsFontSize() = MaterialTheme.typography.bodyLarge.fontSize
@Composable private fun settingsTitleFontSize() = MaterialTheme.typography.titleMedium.fontSize

private const val AUTHOR_WEBSITE_URL = "https://lucodivo.github.io/"
private const val SOURCE_CODE_URL = "https://github.com/Lucodivo/Merlinsbag"
private const val ECCOHEDRA_URL = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.learnopengl_androidport"
private const val MERLINSBAG_URL = "https://play.google.com/store/apps/details?id=com.inasweaterpoorlyknit.merlinsbag"
private const val PRIVACY_INFO_URL = "https://lucodivo.github.io/merlinsbag_android_privacy_policy.html"
private const val DEMO_VIDEO_URL = "https://www.youtube.com/watch?v=uUQYMU2N4kA"
private const val DELETE_ALL_CAPTCHA = "1234"

const val SETTINGS_ROUTE = "settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SETTINGS_ROUTE, navOptions)

@Composable
fun SettingsRoute(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val uriHandler = LocalUriHandler.current
  var showDeleteAllDataAlertDialog by remember { mutableStateOf(false) }
  var expandedDarkModeMenu by remember { mutableStateOf(false) }
  var expandedColorPaletteMenu by remember { mutableStateOf(false) }
  var expandedHighContrastMenu by remember { mutableStateOf(false) }
  var expandedTypographyMenu by remember { mutableStateOf(false) }
  var clearCacheEnabled by remember { mutableStateOf(true) }
  val userPreferences by settingsViewModel.userPreferences.collectAsState()

  LaunchedEffect(settingsViewModel.cacheClearedTrigger) { settingsViewModel.cacheClearedTrigger.collect { context.toast(R.string.cache_cleared) } }

  LaunchedEffect(settingsViewModel.allDataDeletedTrigger) {
    settingsViewModel.allDataDeletedTrigger.collect {
      context.toast(msg = context.getString(R.string.all_data_deleted))
      navController.navigateToAppStartDestination()
    }
  }

  SettingsScreen(
    showDeleteAllDataAlertDialog = showDeleteAllDataAlertDialog,
    expandDarkModeDropdownMenu = expandedDarkModeMenu,
    expandColorPaletteDropdownMenu = expandedColorPaletteMenu,
    expandHighContrastDropdownMenu = expandedHighContrastMenu,
    expandTypographyDropdownMenu = expandedTypographyMenu,
    clearCacheEnabled = clearCacheEnabled,
    darkMode = userPreferences.darkMode,
    colorPalette = userPreferences.colorPalette,
    highContrast = userPreferences.highContrast,
    typography = userPreferences.typography,
    onClickAuthor = { uriHandler.openUri(AUTHOR_WEBSITE_URL) },
    onClickSource = { uriHandler.openUri(SOURCE_CODE_URL) },
    onClickEccohedra = { uriHandler.openUri(ECCOHEDRA_URL) },
    onClickDemoVideo = { uriHandler.openUri(DEMO_VIDEO_URL) },
    onClickReview = { uriHandler.openUri(MERLINSBAG_URL) },
    onClickWelcomePage = { settingsViewModel.showWelcomePage() },
    onClickTipsAndInfoPage = { navController.navigateToTipsAndInfo() },
    onClickPrivacyInfo = { uriHandler.openUri(PRIVACY_INFO_URL) },
    onClickClearCache = {
      clearCacheEnabled = false
      settingsViewModel.clearCache()
    },
    onClickDeleteAllData = { showDeleteAllDataAlertDialog = true },
    onDismissDeleteAllDataAlertDialog = { showDeleteAllDataAlertDialog = false },
    onClickConfirmDeleteAllDataAlertDialog = {
      showDeleteAllDataAlertDialog = false
      settingsViewModel.deleteAllData()
    },
    onSelectDarkMode = { darkMode ->
      settingsViewModel.setDarkMode(darkMode)
      expandedDarkModeMenu = false
    },
    onClickDarkMode = { expandedDarkModeMenu = !expandedDarkModeMenu },
    onDismissDarkMode = { expandedDarkModeMenu = false },
    onSelectColorPalette = {
      settingsViewModel.setColorPalette(it)
      expandedColorPaletteMenu = false
    },
    onClickColorPalette = { expandedColorPaletteMenu = !expandedColorPaletteMenu },
    onDismissColorPalette = { expandedColorPaletteMenu = false },
    onSelectHighContrast = {
      settingsViewModel.setHighContrast(it)
      expandedHighContrastMenu = false
    },
    onDismissHighContrast = { expandedHighContrastMenu = false },
    onClickHighContrast = { expandedHighContrastMenu = !expandedHighContrastMenu },
    onSelectTypography = {
      settingsViewModel.setTypography(it)
      expandedTypographyMenu = false
    },
    onDismissTypography = { expandedTypographyMenu = false },
    onClickTypography = { expandedTypographyMenu = !expandedTypographyMenu },
    onClickStatistics = navController::navigateToStatistics
  )
}

@Composable
fun AuthorRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.developer),
  indicator = { Icon(NoopIcons.Web, stringResource(R.string.web_page)) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun SourceRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.source),
  indicator = { Icon(NoopIcons.Code, stringResource(R.string.source_code)) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun StatisticsRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.statistics),
  indicator = { Icon(NoopIcons.Statistics, stringResource(R.string.bar_chart))},
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun ReviewRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.review),
  indicator = { Icon(NoopIcons.Reviews, REDUNDANT_CONTENT_DESCRIPTION)},
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun DemoVideoRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.demo_video),
  indicator = { Icon(NoopIcons.Video, stringResource(R.string.video)) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun EccohedraRow(onClick: () -> Unit) {
  val eccohedraContentDescription = stringResource(R.string.eccohedra_description)
  SettingsTextIndicatorButton(
    text = stringResource(R.string.eccohedra),
    indicator = { Icon(NoopIcons.eccohedra(), REDUNDANT_CONTENT_DESCRIPTION) },
    onClick = onClick,
    modifier = itemModifier.semantics { contentDescription = eccohedraContentDescription},
  )
}

@Composable
fun PrivacyInfoRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.privacy_information),
  indicator = { Icon(NoopIcons.Privacy, REDUNDANT_CONTENT_DESCRIPTION) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun DropdownSettingsRow(
    title: String,
    indicator: @Composable (() -> Unit),
    expanded: Boolean,
    onClick: () -> Unit,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    items: List<Pair<String, IconData?>>,
    enabled: Boolean = true,
) {
  Column(
    horizontalAlignment = Alignment.End,
    modifier = itemModifier.fillMaxWidth(),
  ) {
    SettingsTextIndicatorButton(
      enabled = enabled,
      text = title,
      indicator = indicator,
      onClick = onClick,
    )
    Box {
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
      ) {
        items.forEachIndexed { index, item ->
          val iconData = item.second
          DropdownMenuItem(
            text = { Text(text = item.first) },
            trailingIcon = iconData?.run { { Icon(icon, contentDescription) } },
            onClick = { onSelect(index) },
          )
        }
      }
    }
  }
}

@Composable
fun DarkModeRow(
    selectedDarkMode: DarkMode,
    expandedMenu: Boolean,
    onClick: () -> Unit,
    onSelectDarkMode: (DarkMode) -> Unit,
    onDismiss: () -> Unit,
) {
  // Note: This order matters as we are taking advantage of the ordinal of the DarkMode enum
  val dropdownData = listOf(
    Pair(stringResource(R.string.system), IconData(NoopIcons.systemMode(), REDUNDANT_CONTENT_DESCRIPTION)),
    Pair(stringResource(R.string.light), IconData(NoopIcons.LightMode, REDUNDANT_CONTENT_DESCRIPTION)),
    Pair(stringResource(R.string.dark), IconData(NoopIcons.DarkMode, REDUNDANT_CONTENT_DESCRIPTION))
  )
  val selectedDarkModeIcon = dropdownData[selectedDarkMode.ordinal].second
  DropdownSettingsRow(
    title = stringResource(R.string.dark_mode),
    indicator = { Icon(selectedDarkModeIcon.icon, selectedDarkModeIcon.contentDescription, modifier = Modifier.fillMaxHeight()) },
    expanded = expandedMenu,
    items = dropdownData,
    onClick = onClick,
    onSelect = { index -> onSelectDarkMode(DarkMode.entries[index]) },
    onDismiss = onDismiss,
  )
}

@Composable
fun TypographyRow(
    selectedTypography: Typography,
    expandedMenu: Boolean,
    onClick: () -> Unit,
    onSelectTypography: (Typography) -> Unit,
    onDismiss: () -> Unit,
) {
  // Note: This order matters as we are taking advantage of the ordinal of the DarkMode enum
  val dropdownData = listOf(
    Pair(stringResource(R.string.default_), null),
    Pair(stringResource(R.string.montserrat), null),
    Pair(stringResource(R.string.jetbrains_mono), null),
    Pair(stringResource(R.string.cinzel), null),
    Pair(stringResource(R.string.concert_one), null),
    Pair(stringResource(R.string.macondo), null),
    Pair(stringResource(R.string.tiny5), null),
  )
  val selectedTypographyText = dropdownData[selectedTypography.ordinal]
  DropdownSettingsRow(
    title = stringResource(R.string.font),
    indicator = {
      Text(
        text = selectedTypographyText.first,
        fontSize = settingsFontSize(),
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxHeight()
      )
      },
    expanded = expandedMenu,
    items = dropdownData,
    onClick = onClick,
    onSelect = { index -> onSelectTypography(Typography.entries[index]) },
    onDismiss = onDismiss,
  )
}

@Composable
fun ColorPaletteRow(
    selectedColorPalette: ColorPalette,
    expandedMenu: Boolean,
    onClick: () -> Unit,
    onSelectColorPalette: (ColorPalette) -> Unit,
    onDismiss: () -> Unit,
) {
  // Note: This order matters as we are taking advantage of the ordinal of the DarkMode enum
  val dropdownData = NoopColorSchemes.colorPaletteSchemes.map { scheme ->
    Pair(stringResource(scheme.nameStrRes), null)
  }
  DropdownSettingsRow(
    title = stringResource(R.string.colors),
    indicator = {
      Text(
        text = dropdownData[selectedColorPalette.ordinal].first,
        fontSize = settingsFontSize(),
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxHeight()
      )
    },
    expanded = expandedMenu,
    items = dropdownData,
    onClick = onClick,
    onSelect = { index -> onSelectColorPalette(ColorPalette.entries[index]) },
    onDismiss = onDismiss,
  )
}

@Composable
fun HighContrastRow(
    enabled: Boolean,
    selectedHighContrast: HighContrast,
    expandedMenu: Boolean,
    onClick: () -> Unit,
    onSelectHighContrast: (HighContrast) -> Unit,
    onDismiss: () -> Unit,
) {
  // Note: This order matters as we are taking advantage of the ordinal of the DarkMode enum
  val dropdownData = listOf(
    Pair(stringResource(R.string.off), null),
    Pair(stringResource(R.string.medium), null),
    Pair(stringResource(R.string.high), null),
  )
  DropdownSettingsRow(
    title = stringResource(R.string.high_contrast),
    indicator = {
      Text(
        text = dropdownData[selectedHighContrast.ordinal].first,
        fontSize = settingsFontSize(),
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxHeight()
      )
    },
    expanded = expandedMenu,
    items = dropdownData,
    onClick = onClick,
    onSelect = { index -> onSelectHighContrast(HighContrast.entries[index]) },
    onDismiss = onDismiss,
    enabled = enabled,
  )
}

@Composable
fun ClearCacheRow(
    enabled: Boolean,
    onClick: () -> Unit,
) = SettingsTextIndicatorButton(
  enabled = enabled,
  text = stringResource(R.string.clear_cache),
  indicator = { Icon(NoopIcons.Clean, REDUNDANT_CONTENT_DESCRIPTION) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun DeleteAllDataRow(onClick: () -> Unit) {
  SettingsTextIndicatorButton(
    text = stringResource(R.string.delete_all_data),
    indicator = { Icon(NoopIcons.DeleteForever, REDUNDANT_CONTENT_DESCRIPTION) },
    onClick = onClick,
    modifier = itemModifier,
  )
}

@Composable
fun SettingsScreen(
    systemBarPaddingValues: PaddingValues = WindowInsets.systemBars.asPaddingValues(),
    showDeleteAllDataAlertDialog: Boolean,
    expandDarkModeDropdownMenu: Boolean,
    expandColorPaletteDropdownMenu: Boolean,
    expandHighContrastDropdownMenu: Boolean,
    expandTypographyDropdownMenu: Boolean,
    clearCacheEnabled: Boolean,
    darkMode: DarkMode,
    colorPalette: ColorPalette,
    typography: Typography,
    highContrast: HighContrast,
    onClickAuthor: () -> Unit,
    onClickSource: () -> Unit,
    onClickEccohedra: () -> Unit,
    onClickWelcomePage: () -> Unit,
    onClickStatistics: () -> Unit,
    onClickTipsAndInfoPage: () -> Unit,
    onClickReview: () -> Unit,
    onClickPrivacyInfo: () -> Unit,
    onClickClearCache: () -> Unit,
    onClickDeleteAllData: () -> Unit,
    onClickDemoVideo: () -> Unit,
    onClickConfirmDeleteAllDataAlertDialog: () -> Unit,
    onClickDarkMode: () -> Unit,
    onDismissDarkMode: () -> Unit,
    onSelectDarkMode: (DarkMode) -> Unit,
    onClickColorPalette: () -> Unit,
    onSelectColorPalette: (ColorPalette) -> Unit,
    onDismissColorPalette: () -> Unit,
    onClickHighContrast: () -> Unit,
    onSelectHighContrast: (HighContrast) -> Unit,
    onDismissHighContrast: () -> Unit,
    onClickTypography: () -> Unit,
    onSelectTypography: (Typography) -> Unit,
    onDismissTypography: () -> Unit,
    onDismissDeleteAllDataAlertDialog: () -> Unit,
) {
  val layoutDir = LocalLayoutDirection.current
  Row {
    LazyColumn(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top,
      modifier = Modifier.fillMaxSize().padding(start = systemBarPaddingValues.calculateStartPadding(layoutDir), end = systemBarPaddingValues.calculateEndPadding(layoutDir)),
    ) {
      item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding())) }
      item { SettingsTitle(stringResource(R.string.appearance)) }
      item {
        ColorPaletteRow(
          selectedColorPalette = colorPalette,
          expandedMenu = expandColorPaletteDropdownMenu,
          onClick = onClickColorPalette,
          onSelectColorPalette = onSelectColorPalette,
          onDismiss = onDismissColorPalette,
        )
      }
      item {
        TypographyRow(
          selectedTypography = typography,
          expandedMenu = expandTypographyDropdownMenu,
          onClick = onClickTypography,
          onSelectTypography = onSelectTypography,
          onDismiss = onDismissTypography,
        )
      }
      item {
        DarkModeRow(
          selectedDarkMode = darkMode,
          expandedMenu = expandDarkModeDropdownMenu,
          onClick = onClickDarkMode,
          onSelectDarkMode = onSelectDarkMode,
          onDismiss = onDismissDarkMode,
        )
      }
      item {
        val systemDynamic = colorPalette == ColorPalette.SYSTEM_DYNAMIC
        HighContrastRow(
          enabled = !systemDynamic, // System dynamic color schemes do not currently support high contrast
          selectedHighContrast = if(systemDynamic) HighContrast.OFF else highContrast,
          expandedMenu = expandHighContrastDropdownMenu,
          onClick = onClickHighContrast,
          onSelectHighContrast = onSelectHighContrast,
          onDismiss = onDismissHighContrast,
        )
      }
      item { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) }
      item { SettingsTitle(stringResource(R.string.info)) }
      item { DemoVideoRow(onClickDemoVideo) }
      item { TipsAndInfoRow(onClickTipsAndInfoPage) }
      item { WelcomePageRow(onClickWelcomePage)}
      item { StatisticsRow(onClickStatistics) }
      item { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) }
      item { SettingsTitle(stringResource(R.string.data)) }
      item { PrivacyInfoRow(onClickPrivacyInfo) }
      item { ClearCacheRow(clearCacheEnabled, onClickClearCache) }
      item { DeleteAllDataRow(onClickDeleteAllData) }
      item { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) }
      item { SettingsTitle(stringResource(R.string.about)) }
      item { AuthorRow(onClickAuthor) }
      item { SourceRow(onClickSource) }
      item { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) }
      item { SettingsTitle(stringResource(R.string.etc)) }
      item { ReviewRow(onClickReview) }
      item { EccohedraRow(onClickEccohedra) }
      item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateBottomPadding() + sectionSpacerHeight)) }
    }
  }
  if(showDeleteAllDataAlertDialog) {
    DeleteAllDataAlertDialog(
      onDismiss = onDismissDeleteAllDataAlertDialog,
      onClickPositive = onClickConfirmDeleteAllDataAlertDialog,
    )
  }
}

@Composable
fun SettingsTitle(text: String) {
  Text(
    text = text,
    fontSize = settingsTitleFontSize(),
    textAlign = TextAlign.Center,
    modifier = headerModifier,
  )
}

@Composable
fun SettingsTextIndicatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indicator: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
  ElevatedButton(
    onClick = onClick,
    enabled = enabled,
    shape = MaterialTheme.shapes.medium,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = text,
        fontSize = settingsFontSize(),
      )
      Spacer(modifier.width(itemHorizontalPadding))
      indicator?.invoke()
    }
  }
}

@Composable
fun WelcomePageRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.welcome_page),
  indicator = { Icon(NoopIcons.WavingHand, stringResource(R.string.waving_hand)) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun TipsAndInfoRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.tips_and_info),
  indicator = { Icon(NoopIcons.Info, stringResource(R.string.information)) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun DeleteAllDataAlertDialog(
    onDismiss: () -> Unit,
    onClickPositive: () -> Unit,
) {
  val enteredText = remember { mutableStateOf("") }
  val spacerSize = 8.dp
  val containerColor = MaterialTheme.colorScheme.errorContainer
  val contentColor = MaterialTheme.colorScheme.onErrorContainer
  NoopAlertDialog(
    title = { Text(stringResource(id = R.string.delete_all_data)) },
    headerIcon = { Icon(imageVector = NoopIcons.DeleteForever, contentDescription = REDUNDANT_CONTENT_DESCRIPTION) },
    text = {
      Column {
        Text(text = stringResource(id = R.string.deleted_all_data_unrecoverable))
        Spacer(modifier = Modifier.height(spacerSize))
        OutlinedTextField(
          value = enteredText.value,
          label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = NoopIcons.Key, contentDescription = stringResource(R.string.confirmation_key), tint = contentColor)
              Spacer(modifier = Modifier.width(spacerSize))
              Text(text = DELETE_ALL_CAPTCHA, color = contentColor)
            }
          },
          placeholder = { Text(DELETE_ALL_CAPTCHA) },
          onValueChange = { enteredText.value = it },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors().copy(
            focusedTextColor = contentColor, disabledTextColor = contentColor, unfocusedTextColor = contentColor, errorTextColor = contentColor,
            errorSupportingTextColor = contentColor, disabledSupportingTextColor = contentColor, unfocusedSupportingTextColor = contentColor, focusedSupportingTextColor = contentColor,
            disabledPlaceholderColor = contentColor, errorPlaceholderColor = contentColor, unfocusedPlaceholderColor = contentColor, focusedPlaceholderColor = contentColor,
            cursorColor = contentColor, errorCursorColor = contentColor,
          ),
        )
      }
    },
    confirmButton = {
      Spacer(modifier = Modifier.width(20.dp))
      if(enteredText.value == DELETE_ALL_CAPTCHA) {
        Text(stringResource(id = R.string.delete), modifier = Modifier.clickable { onClickPositive() })
      } else {
        Icon(imageVector = NoopIcons.Lock, contentDescription = stringResource(R.string.confirmation_locked))
      }
    },
    cancelButton = { Text(text = stringResource(id = R.string.cancel), modifier = Modifier.clickable { onDismiss() }) },
    onDismiss = onDismiss,
    contentColor = contentColor,
    containerColor = containerColor,
  )
}

//region COMPOSABLE PREVIEWS
@Composable
fun PreviewUtilSettingsScreen(
    showDeleteAllDataAlertDialog: Boolean = false,
    expandDarkModeDropdownMenu: Boolean = false,
    expandColorPaletteDropdownMenu: Boolean = false,
    expandHighContrastDropdownMenu: Boolean = false,
    expandTypographyDropdownMenu: Boolean = false,
    clearCacheEnabled: Boolean = true,
    darkMode: DarkMode = DarkMode.SYSTEM,
    colorPalette: ColorPalette = ColorPalette.ROAD_WARRIOR,
    highContrast: HighContrast = HighContrast.OFF,
    typography: Typography = Typography.DEFAULT,
) = NoopTheme(darkMode = darkMode) {
  Surface {
    SettingsScreen(
      showDeleteAllDataAlertDialog = showDeleteAllDataAlertDialog,
      expandDarkModeDropdownMenu = expandDarkModeDropdownMenu,
      expandColorPaletteDropdownMenu = expandColorPaletteDropdownMenu,
      expandHighContrastDropdownMenu = expandHighContrastDropdownMenu,
      expandTypographyDropdownMenu = expandTypographyDropdownMenu,
      clearCacheEnabled = clearCacheEnabled,
      darkMode = darkMode,
      colorPalette = colorPalette,
      typography = typography,
      highContrast = highContrast, onClickAuthor = {}, onClickSource = {}, onClickEccohedra = {},
      onClickWelcomePage = {}, onClickTipsAndInfoPage = {}, onClickPrivacyInfo = {},
      onClickClearCache = {}, onClickDeleteAllData = {}, onClickStatistics = {},
      onClickDemoVideo = {}, onClickConfirmDeleteAllDataAlertDialog = {}, onClickDarkMode = {},
      onDismissDarkMode = {}, onSelectDarkMode = {}, onClickColorPalette = {}, onSelectColorPalette = {},
      onDismissColorPalette = {}, onClickHighContrast = {}, onSelectHighContrast = {}, onClickReview = {},
      onDismissHighContrast = {}, onClickTypography = {}, onSelectTypography = {}, onDismissTypography = {}, onDismissDeleteAllDataAlertDialog = {},
    )
  }
}

@SystemUiPreview @Composable fun PreviewSettingsScreen() = PreviewUtilSettingsScreen(darkMode = DarkMode.DARK)
@LargeFontSizePreview @Composable fun PreviewSettingsScreen_largeFont() = PreviewUtilSettingsScreen(darkMode = DarkMode.DARK, colorPalette = ColorPalette.SYSTEM_DYNAMIC)
@Preview @Composable fun PreviewSettingsScreen_AlertDialog() = PreviewUtilSettingsScreen(showDeleteAllDataAlertDialog = true, darkMode = DarkMode.LIGHT)
@Preview @Composable fun PreviewDeleteAllDataAlertDialog() = NoopTheme(darkMode = DarkMode.DARK) { DeleteAllDataAlertDialog(onClickPositive = {}, onDismiss = {}) }
//endregion