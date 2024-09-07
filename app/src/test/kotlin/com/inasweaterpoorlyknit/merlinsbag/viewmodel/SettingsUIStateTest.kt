@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.inasweaterpoorlyknit.core.data.repository.PurgeRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.ImageQuality
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.justRun
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
  @get:Rule val mockkRule = MockKRule(this)
  @get:Rule val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var purgeRepository: PurgeRepository
  @MockK lateinit var userPreferencesRepository: UserPreferencesRepository

  val testInitialUserPreferences = UserPreferences(imageQuality = ImageQuality.VERY_HIGH)
  val testInitialUiState = with(testInitialUserPreferences){
    SettingsUIState(
      clearCacheEnabled = true,
      highContrastEnabled = true,
      dropdownMenuState = SettingsDropdownMenuState.None,
      alertDialogState = SettingsAlertDialogState.None,
      darkMode = darkMode,
      colorPalette = colorPalette,
      highContrast = highContrast,
      imageQuality = imageQuality,
      typography = typography,
    )
  }

  @Before
  fun beforeEach() = runTest {
    every { userPreferencesRepository.userPreferences } returns flowOf(testInitialUserPreferences)
    justRun { purgeRepository.purgeCache() }
  }

  @Test
  fun `on launch, no alert dialogs or dropdown menus`() = runTest {
    moleculeFlow(mode = RecompositionMode.Immediate){
      settingsUIState(
        initialState = testInitialUiState,
        uiEvents = emptyFlow(), launchUiEffect = {},
        purgeRepository = purgeRepository,
        userPreferencesRepository = userPreferencesRepository,
      )
    }.test {
      val loadedState: SettingsUIState = awaitItem()
      assertEquals(
        loadedState.copy(
          alertDialogState = SettingsAlertDialogState.None,
          dropdownMenuState = SettingsDropdownMenuState.None
        ),
        loadedState
      )
    }
  }

  @Test
  fun `Clear cache disables button and triggers effect`() = runTest {
    val events = Channel<SettingsUIEvent>()
    val uiEffects = ArrayList<SettingsUIEffect>()
    moleculeFlow(mode = RecompositionMode.Immediate){
      settingsUIState(
        initialState = testInitialUiState,
        events.receiveAsFlow(),
        { uiEffects.add(it) },
        purgeRepository = purgeRepository,
        userPreferencesRepository = userPreferencesRepository,
      )
    }.test {
      assertTrue(awaitItem().clearCacheEnabled)
      events.send(SettingsUIEvent.ClickClearCache)
      assertFalse(awaitItem().clearCacheEnabled)
      assertEquals(1, uiEffects.size)
      assertEquals(SettingsUIEffect.CachePurged, uiEffects[0])
      uiEffects.clear()
    }
  }

  @Test
  fun `Selecting a higher image quality triggers alert dialog`() = runTest {
    val events = Channel<SettingsUIEvent>()
    val uiEffects = MutableSharedFlow<SettingsUIEffect>(extraBufferCapacity = 20)
    moleculeFlow(mode = RecompositionMode.Immediate){
      settingsUIState(
        initialState = testInitialUiState,
        events.receiveAsFlow(),
        { uiEffects.tryEmit(it) },
        purgeRepository = purgeRepository,
        userPreferencesRepository = userPreferencesRepository,
      )
    }.test {
      assertEquals(SettingsAlertDialogState.None, awaitItem().alertDialogState)
      events.send(
        SettingsUIEvent.SelectedImageQuality(
          ImageQuality.entries[testInitialUserPreferences.imageQuality.ordinal + 1]
        )
      )
      assertEquals(SettingsAlertDialogState.ImageQuality, awaitItem().alertDialogState)
    }
  }
}
