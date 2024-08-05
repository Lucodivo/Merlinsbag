@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.repository.PurgeRepository
import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.ImageQuality
import com.inasweaterpoorlyknit.core.model.UserPreferences
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit [TestRule] that sets the Main dispatcher to [testDispatcher]
 * for the duration of the test.
 */
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
  override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
  override fun finished(description: Description) = Dispatchers.resetMain()
}

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

  val testInitialUserPreferences = UserPreferences().copy(imageQuality = ImageQuality.HIGH)

  @Before
  fun setup() {
    every { userPreferencesRepository.userPreferences } returns flowOf(testInitialUserPreferences)
    coEvery { userPreferencesRepository.setImageQuality(imageQuality = any()) } just runs
    viewModel = SettingsViewModel(
      purgeRepository,
      userPreferencesRepository,
    )
  }

  @Test
  fun dialogsAreHiddenByDefault() = runTest {
    assertEquals(false, viewModel.showImageQualityAlertDialog)
    assertEquals(false, viewModel.showDeleteAllDataAlertDialog)
  }

  @Test
  fun deleteAllTriggersDialog() = runTest {
    viewModel.onClickDeleteAllData()
    assertEquals(true, viewModel.showDeleteAllDataAlertDialog)
  }

  @Test
  fun selectHigherImageQualityTriggersDialog() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.userPreferences.collect() }

    viewModel.onSelectedImageQuality(ImageQuality.PERFECT)
    assertEquals(true, viewModel.showImageQualityAlertDialog)

    collectJob.cancel()
  }

  @Test
  fun selectLowerOrEqualImageQualityDoesNotTriggersDialog() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.userPreferences.collect() }

    viewModel.onSelectedImageQuality(ImageQuality.HIGH)
    assertEquals(false, viewModel.showImageQualityAlertDialog)
    viewModel.onSelectedImageQuality(ImageQuality.STANDARD)
    assertEquals(false, viewModel.showImageQualityAlertDialog)

    collectJob.cancel()
  }
}