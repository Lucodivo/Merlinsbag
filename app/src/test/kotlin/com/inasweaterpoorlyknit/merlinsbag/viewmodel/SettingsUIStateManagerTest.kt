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
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIState.AlertDialogState
import com.inasweaterpoorlyknit.merlinsbag.viewmodel.SettingsUIState.DropdownMenuState
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

class SettingsUIStateManagerTest {
  @get:Rule val mockkRule = MockKRule(this)
  @get:Rule val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var purgeRepository: PurgeRepository
  @MockK lateinit var userPreferencesRepository: UserPreferencesRepository

  val testInitialUserPreferences = UserPreferences(imageQuality = ImageQuality.VERY_HIGH)
  lateinit var settingsUIStateManager: SettingsUIStateManager

  @Before
  fun beforeEach() = runTest {
    every { userPreferencesRepository.userPreferences } returns flowOf(testInitialUserPreferences)
    justRun { purgeRepository.purgeCache() }

    settingsUIStateManager = SettingsUIStateManager(
      purgeRepository = purgeRepository,
      userPreferencesRepository = userPreferencesRepository,
    )
  }

  @Test
  fun `on launch, no alert dialogs or dropdown menus`() = runTest {
    moleculeFlow(mode = RecompositionMode.Immediate){
      settingsUIStateManager.uiState(
        uiEvents = emptyFlow(),
        launchUiEffect = {},
      )
    }.test {
      skipItems(1) // skip initial emission
      val loadedState: SettingsUIState = awaitItem()
      assertEquals(
        loadedState.copy(
          alertDialog = AlertDialogState.None,
          dropdownMenu = DropdownMenuState.None
        ),
        loadedState
      )
    }
  }

  @Test
  fun `Clear cache disables button and triggers effect`() = runTest {
    val uiEvents = Channel<SettingsUIEvent>()
    val uiEffects = ArrayList<SettingsUIEffect>()
    moleculeFlow(mode = RecompositionMode.Immediate){
      settingsUIStateManager.uiState(
        uiEvents = uiEvents.receiveAsFlow(),
        launchUiEffect = { uiEffects.add(it) },
      )
    }.test {
      skipItems(1) // skip initial emission
      assertTrue(awaitItem().clearCacheEnabled)
      uiEvents.send(SettingsUIEvent.ClickClearCache)
      assertFalse(awaitItem().clearCacheEnabled)
      assertEquals(1, uiEffects.size)
      assertEquals(SettingsUIEffect.CachePurged, uiEffects[0])
      uiEffects.clear()
    }
  }

  @Test
  fun `Selecting a higher image quality triggers alert dialog`() = runTest {
    val uiEvents = Channel<SettingsUIEvent>()
    val uiEffects = MutableSharedFlow<SettingsUIEffect>(extraBufferCapacity = 20)
    moleculeFlow(mode = RecompositionMode.Immediate){
      settingsUIStateManager.uiState(
        uiEvents = uiEvents.receiveAsFlow(),
        launchUiEffect = { uiEffects.tryEmit(it) },
      )
    }.test {
      skipItems(1) // skip initial emission
      assertEquals(AlertDialogState.None, awaitItem().alertDialog)
      uiEvents.send(
        SettingsUIEvent.SelectedImageQuality(
          ImageQuality.entries[testInitialUserPreferences.imageQuality.ordinal + 1]
        )
      )
      assertEquals(AlertDialogState.ImageQuality, awaitItem().alertDialog)
    }
  }
}