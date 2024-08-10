@file:OptIn(ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.repository.PurgeRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.ImageQuality
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK
  lateinit var purgeRepository: PurgeRepository

  @MockK
  lateinit var userPreferencesRepository: UserPreferencesRepository

  lateinit var viewModel: SettingsViewModel

  val testInitialUserPreferences = UserPreferences(
    imageQuality = ImageQuality.VERY_HIGH
  )

  @Before
  fun setup() {
    every { userPreferencesRepository.userPreferences } returns flowOf(testInitialUserPreferences)
    viewModel = SettingsViewModel(
      purgeRepository,
      userPreferencesRepository,
    )
  }

  @Test
  fun `Dialogs are hidden by default`() = runTest {
    assertEquals(SettingsViewModel.AlertDialogState.None, viewModel.alertDialogState)
  }

  @Test
  fun `Show delete all data alert dialog`() = runTest {
    viewModel.onClickDeleteAllData()
    assertEquals(SettingsViewModel.AlertDialogState.DeleteAllData, viewModel.alertDialogState)
  }

  @Test
  fun `Selecting a higher image quality triggers alert dialog`() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.preferencesState.collect() }

    viewModel.onSelectedImageQuality(
        ImageQuality.entries[testInitialUserPreferences.imageQuality.ordinal + 1]
    )

    assertEquals(SettingsViewModel.AlertDialogState.ImageQuality, viewModel.alertDialogState)

    collectJob.cancel()
  }

  @Test
  fun `Selecting a lower image quality does not triggers alert dialog`() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.preferencesState.collect() }

    val lowerImageQuality = ImageQuality.entries[testInitialUserPreferences.imageQuality.ordinal - 1]
    coJustRun { userPreferencesRepository.setImageQuality(lowerImageQuality) }

    viewModel.onSelectedImageQuality(lowerImageQuality)

    assertEquals(SettingsViewModel.AlertDialogState.None, viewModel.alertDialogState)
    coVerify(exactly = 1) { userPreferencesRepository.setImageQuality(lowerImageQuality) }

    collectJob.cancel()
  }

  @Test
  fun `Selecting a equal image quality does not triggers alert dialog`() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.preferencesState.collect() }

    viewModel.onSelectedImageQuality(testInitialUserPreferences.imageQuality)

    assertEquals(SettingsViewModel.AlertDialogState.None, viewModel.alertDialogState)

    collectJob.cancel()
  }

  @Test
  fun `Clearing cache disables button and notifies user`() = runTest {
    coJustRun { purgeRepository.purgeCache() }

    viewModel.onClickClearCache()

    verify(exactly = 1) { purgeRepository.purgeCache() }
    assertFalse(viewModel.clearCacheEnabled)
    assertNotNull(viewModel.cachePurged.getContentIfNotHandled())
  }
}