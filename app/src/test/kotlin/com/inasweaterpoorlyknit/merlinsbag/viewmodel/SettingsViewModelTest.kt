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
  @get:Rule val mockkRule = MockKRule(this)
  @get:Rule val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var purgeRepository: PurgeRepository
  @MockK lateinit var userPreferencesRepository: UserPreferencesRepository

  lateinit var viewModel: SettingsViewModel

  val testInitialUserPreferences = UserPreferences(imageQuality = ImageQuality.VERY_HIGH)

  @Before
  fun beforeEach() = runTest {
    every { userPreferencesRepository.userPreferences } returns flowOf(testInitialUserPreferences)
    viewModel = SettingsViewModel(
      purgeRepository,
      userPreferencesRepository,
    )
  }

  @Test
  fun `Selecting a higher image quality triggers alert dialog`() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

    viewModel.onSelectedImageQuality(
        ImageQuality.entries[testInitialUserPreferences.imageQuality.ordinal + 1]
    )

    assertEquals(SettingsAlertDialogState.ImageQuality, viewModel.uiState.value.alertDialogState)

    collectJob.cancel()
  }
}