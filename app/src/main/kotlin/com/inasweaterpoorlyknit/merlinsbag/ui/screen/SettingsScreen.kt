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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.inasweaterpoorlyknit.core.model.ColorPalette
import com.inasweaterpoorlyknit.core.model.DarkMode
import com.inasweaterpoorlyknit.core.model.HighContrast
import com.inasweaterpoorlyknit.core.model.ImageQuality
import com.inasweaterpoorlyknit.core.model.Typography
import com.inasweaterpoorlyknit.core.ui.LargeFontSizePreview
import com.inasweaterpoorlyknit.core.ui.REDUNDANT_CONTENT_DESCRIPTION
import com.inasweaterpoorlyknit.core.ui.SystemUiPreview
import com.inasweaterpoorlyknit.core.ui.component.IconData
import com.inasweaterpoorlyknit.core.ui.component.NoopAlertDialog
import com.inasweaterpoorlyknit.core.ui.component.NoopSimpleAlertDialog
import com.inasweaterpoorlyknit.core.ui.theme.NoopIcons
import com.inasweaterpoorlyknit.core.ui.theme.NoopTheme
import com.inasweaterpoorlyknit.core.ui.theme.scheme.NoopColorSchemes
import com.inasweaterpoorlyknit.merlinsbag.R
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIState.AlertDialogState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIState.DropdownMenuState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIEffect
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIEvent
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsViewModel
import kotlinx.serialization.Serializable
import staggeredHorizontallyAnimatedComposables

private val headerModifier = Modifier.fillMaxWidth()
private val itemHorizontalPadding = 8.dp
private val itemModifier = Modifier.padding(horizontal = itemHorizontalPadding)
private val dividerHorizontalPadding = itemHorizontalPadding * 2
private val dividerModifier = Modifier.padding(horizontal = dividerHorizontalPadding, vertical = 8.dp)
private val dividerThickness = 2.dp
private val sectionSpacerHeight = 16.dp
@Composable private fun settingsFontSize() = MaterialTheme.typography.bodyLarge.fontSize
@Composable private fun settingsTitleFontSize() = MaterialTheme.typography.titleMedium.fontSize

private const val DELETE_ALL_CAPTCHA = "1234"

@Serializable
object SettingsRouteArgs

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRouteArgs, navOptions)

@Composable
fun SettingsRoute(
    navigateToTipsAndInfo: () -> Unit,
    navigateToStatistics: () -> Unit,
    navigateToStartDestination: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  val uriHandler = LocalUriHandler.current

  LaunchedEffect(Unit) {
    lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
      settingsViewModel.uiEffect.collect{ uiEvent ->
        when(uiEvent){
          SettingsUIEffect.AllDataDeleted -> {
            context.toast(msg = context.getString(R.string.all_data_deleted))
            navigateToStartDestination()
          }
          SettingsUIEffect.CachePurged -> {
            context.toast(R.string.cache_cleared)
          }
          SettingsUIEffect.RateAndReviewRequest -> {
            rateAndReviewRequest(
              context = context,
              onCompleted = { context.toast(R.string.thank_you) },
              onUnableToDisplayInAppReview = { settingsViewModel.onUiEvent(SettingsUIEvent.UnableToDisplayInAppReview) },
              onError = { context.toast(R.string.try_again_later) },
            )
          }
          SettingsUIEffect.NavigateToStatistics -> navigateToStatistics()
          SettingsUIEffect.NavigateToTipsAndInfo -> navigateToTipsAndInfo()
          is SettingsUIEffect.NavigateToWeb -> uriHandler.openUri(uiEvent.url)
        }
      }
    }
  }

  val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
  SettingsScreen(
    uiState = uiState,
    onUiEvent = settingsViewModel::onUiEvent
  )
}

@Composable
fun DeveloperRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
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
fun VersionRow() {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 32.dp, vertical = 4.dp)
  ) {
    Text(stringResource(R.string.version))
    Text(versionName())
  }
}

@Composable
fun StatisticsRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.statistics),
  indicator = { Icon(NoopIcons.Statistics, stringResource(R.string.bar_chart)) },
  onClick = onClick,
  modifier = itemModifier,
)

@Composable
fun RateAndReviewRow(onClick: () -> Unit) = SettingsTextIndicatorButton(
  text = stringResource(R.string.review),
  indicator = { Icon(NoopIcons.Reviews, REDUNDANT_CONTENT_DESCRIPTION) },
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
    modifier = itemModifier.semantics { contentDescription = eccohedraContentDescription },
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
fun ImageQualityRow(
    selectedImageQuality: ImageQuality,
    expandedMenu: Boolean,
    onClick: () -> Unit,
    onSelectImageQuality: (ImageQuality) -> Unit,
    onDismiss: () -> Unit,
) {
  // Note: This order matters as we are taking advantage of the ordinal of the DarkMode enum
  val dropdownData = listOf(
    Pair(stringResource(R.string.standard), null),
    Pair(stringResource(R.string.high), null),
    Pair(stringResource(R.string.very_high), null),
    Pair(stringResource(R.string.perfect), null),
  )
  DropdownSettingsRow(
    title = stringResource(R.string.image_quality),
    indicator = {
      Text(
        text = dropdownData[selectedImageQuality.ordinal].first,
        fontSize = settingsFontSize(),
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxHeight()
      )
    },
    expanded = expandedMenu,
    items = dropdownData,
    onClick = onClick,
    onSelect = { index -> onSelectImageQuality(ImageQuality.entries[index]) },
    onDismiss = onDismiss,
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
    uiState: SettingsUIState,
    onUiEvent: (SettingsUIEvent) -> Unit,
) {
  val layoutDir = LocalLayoutDirection.current
  val settingsRows = staggeredHorizontallyAnimatedComposables(
    content = listOf(
      { SettingsTitle(stringResource(R.string.appearance)) },
      {
        ColorPaletteRow(
          selectedColorPalette = uiState.colorPalette,
          expandedMenu = uiState.dropdownMenu == DropdownMenuState.ColorPalette,
          onClick = { onUiEvent(SettingsUIEvent.ClickColorPalette) },
          onSelectColorPalette = { onUiEvent(SettingsUIEvent.SelectColorPalette(it)) },
          onDismiss = { onUiEvent(SettingsUIEvent.DismissColorPalette) },
        )
      },
      {
        TypographyRow(
          selectedTypography = uiState.typography,
          expandedMenu = uiState.dropdownMenu == DropdownMenuState.Typography,
          onClick = { onUiEvent(SettingsUIEvent.ClickTypography) },
          onSelectTypography = { onUiEvent(SettingsUIEvent.SelectTypography(it)) },
          onDismiss = { onUiEvent(SettingsUIEvent.DismissTypography) },
        )
      },
      {
        DarkModeRow(
          selectedDarkMode = uiState.darkMode,
          expandedMenu = uiState.dropdownMenu == DropdownMenuState.DarkMode,
          onClick = { onUiEvent(SettingsUIEvent.ClickDarkMode) },
          onSelectDarkMode = { onUiEvent(SettingsUIEvent.SelectDarkMode(it)) },
          onDismiss = { onUiEvent(SettingsUIEvent.DismissDarkMode) },
        )
      },
      {
        HighContrastRow(
          enabled = uiState.highContrastEnabled,
          selectedHighContrast = uiState.highContrast,
          expandedMenu = uiState.dropdownMenu == DropdownMenuState.HighContrast,
          onClick = { onUiEvent(SettingsUIEvent.ClickHighContrast) },
          onSelectHighContrast = { onUiEvent(SettingsUIEvent.SelectHighContrast(it)) },
          onDismiss = { onUiEvent(SettingsUIEvent.DismissHighContrast) },
        )
      },
      { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) },
      { SettingsTitle(stringResource(R.string.info)) },
      { DemoVideoRow{ onUiEvent(SettingsUIEvent.ClickDemo) } },
      { TipsAndInfoRow{ onUiEvent(SettingsUIEvent.ClickTipsAndInfo) } },
      { WelcomePageRow{ onUiEvent(SettingsUIEvent.ClickWelcome) } },
      { StatisticsRow{ onUiEvent(SettingsUIEvent.ClickStatistics) } },
      { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) },
      { SettingsTitle(stringResource(R.string.data)) },
      {
        ImageQualityRow(
          selectedImageQuality = uiState.imageQuality,
          expandedMenu = uiState.dropdownMenu == DropdownMenuState.ImageQuality,
          onClick = { onUiEvent(SettingsUIEvent.ClickImageQuality) },
          onSelectImageQuality = { onUiEvent(SettingsUIEvent.SelectedImageQuality(it)) },
          onDismiss = { onUiEvent(SettingsUIEvent.DismissImageQualityDropdown) },
        )
      },
      { PrivacyInfoRow{ onUiEvent(SettingsUIEvent.ClickPrivacyInformation) } },
      { ClearCacheRow(uiState.clearCacheEnabled){ onUiEvent(SettingsUIEvent.ClickClearCache)} },
      { DeleteAllDataRow{ onUiEvent(SettingsUIEvent.ClickDeleteAllData) } },
      { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) },
      { SettingsTitle(stringResource(R.string.about)) },
      { DeveloperRow{ onUiEvent(SettingsUIEvent.ClickDeveloper) } },
      { SourceRow{ onUiEvent(SettingsUIEvent.ClickSource) } },
      { VersionRow() },
      { HorizontalDivider(thickness = dividerThickness, modifier = dividerModifier) },
      { SettingsTitle(stringResource(R.string.etc)) },
      { RateAndReviewRow{ onUiEvent(SettingsUIEvent.ClickRateAndReview) } },
      { EccohedraRow{ onUiEvent(SettingsUIEvent.ClickEccohedra) } }
    )
  )
  Row {
    LazyColumn(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top,
      modifier = Modifier
          .fillMaxSize()
          .padding(start = systemBarPaddingValues.calculateStartPadding(layoutDir), end = systemBarPaddingValues.calculateEndPadding(layoutDir)),
    ) {
      item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateTopPadding())) }
      items(settingsRows.size) { index -> settingsRows[index]() }
      item { Spacer(modifier = Modifier.height(systemBarPaddingValues.calculateBottomPadding() + sectionSpacerHeight)) }
    }
  }
  DeleteAllDataAlertDialog(
    visible = uiState.alertDialog == AlertDialogState.DeleteAllData,
    onDismiss = { onUiEvent(SettingsUIEvent.DismissDeleteAllDataAlertDialog) },
    onConfirm = { onUiEvent(SettingsUIEvent.ConfirmDeleteAllDataAlertDialog) },
  )
  ImageQualityAlertDialog(
    visible = uiState.alertDialog == AlertDialogState.ImageQuality,
    onDismiss = { onUiEvent(SettingsUIEvent.DismissImageQualityAlertDialog) },
    onConfirm = { onUiEvent(SettingsUIEvent.ConfirmImageQualityAlertDialog) },
  )
}

@Composable
fun versionName(): String {
  val context = LocalContext.current
  val unknownVersionName = "?"
  try {
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: unknownVersionName
  } catch(e: Exception) {
    e.printStackTrace()
    return unknownVersionName
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
fun ImageQualityAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = NoopSimpleAlertDialog(
  visible = visible,
  title = stringResource(R.string.image_quality_alert_title),
  text = stringResource(R.string.image_quality_alert_text),
  confirmText = stringResource(R.string.confirm),
  cancelText = stringResource(R.string.cancel),
  onDismiss = onDismiss,
  onConfirm = onConfirm,
  headerIcon = { Icon(NoopIcons.Info, stringResource(R.string.information)) }
)

@Composable
fun DeleteAllDataAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  val enteredText = remember { mutableStateOf("") }
  val spacerSize = 8.dp
  val containerColor = MaterialTheme.colorScheme.errorContainer
  val contentColor = MaterialTheme.colorScheme.onErrorContainer
  NoopAlertDialog(
    visible = visible,
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
        Text(stringResource(id = R.string.delete), modifier = Modifier.clickable { onConfirm() })
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
    alertDialogState: AlertDialogState = AlertDialogState.None,
    dropdownMenuState: DropdownMenuState = DropdownMenuState.None,
    highContrastEnabled: Boolean = true,
    clearCacheEnabled: Boolean = true,
    darkMode: DarkMode = DarkMode.DARK,
    colorPalette: ColorPalette = ColorPalette.ROAD_WARRIOR,
    highContrast: HighContrast = HighContrast.OFF,
    imageQuality: ImageQuality = ImageQuality.STANDARD,
    typography: Typography = Typography.DEFAULT,
) = NoopTheme(darkMode = darkMode) {
  Surface {
    SettingsScreen(
      uiState = SettingsUIState(
        alertDialog = alertDialogState,
        dropdownMenu = dropdownMenuState,
        highContrastEnabled = highContrastEnabled,
        clearCacheEnabled = clearCacheEnabled,
        darkMode = darkMode,
        colorPalette = colorPalette,
        highContrast = highContrast,
        imageQuality = imageQuality,
        typography = typography,
      ),
      onUiEvent = {},
    )
  }
}

// TODO: Animations have made previews unusable. Hoist animation values?
@SystemUiPreview @Composable fun PreviewSettingsScreen() = PreviewUtilSettingsScreen()
@LargeFontSizePreview @Composable fun PreviewSettingsScreen_largeFont() = PreviewUtilSettingsScreen()
@Preview @Composable fun PreviewSettingsScreen_AlertDialog() = PreviewUtilSettingsScreen(alertDialogState = AlertDialogState.DeleteAllData)
@Preview @Composable fun PreviewDeleteAllDataAlertDialog() = NoopTheme(darkMode = DarkMode.DARK) { DeleteAllDataAlertDialog(visible = true, onConfirm = {}, onDismiss = {}) }
//endregion
