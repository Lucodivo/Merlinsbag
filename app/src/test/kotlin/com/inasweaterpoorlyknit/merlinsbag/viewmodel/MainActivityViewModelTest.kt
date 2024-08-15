package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.model.UserPreferences
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var userPreferencesRepository: UserPreferencesRepository

  lateinit var viewModel: MainActivityViewModel

  companion object {
    val testUserPreferences = UserPreferences()
  }

  @Before
  fun beforeEach() = runTest {
    every { userPreferencesRepository.userPreferences } returns flowOf(testUserPreferences)
    viewModel = MainActivityViewModel(
      userPreferencesRepository = userPreferencesRepository,
    )
  }

  @Test
  fun `Initialized as Loading`() = runTest {
    assertEquals(MainActivityViewModel.LoadState.Loading, viewModel.uiState)
  }

  @Test
  fun `Success after user preferences loaded`() = runTest {
    viewModel.userPreferences.first()

    assertEquals(MainActivityViewModel.LoadState.Success, viewModel.uiState)
  }
}
