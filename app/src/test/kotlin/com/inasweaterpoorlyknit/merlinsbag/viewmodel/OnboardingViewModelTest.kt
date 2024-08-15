package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import com.inasweaterpoorlyknit.core.data.repository.UserPreferencesRepository
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var userPreferencesRepository: UserPreferencesRepository

  lateinit var viewModel: OnboardingViewModel

  @Before
  fun beforeEach() = runTest {
    coJustRun { userPreferencesRepository.setHasCompletedOnboarding(true) }
    viewModel = OnboardingViewModel(userPreferencesRepository)
  }

  @Test
  fun `On completion sets onboarding user preferences`() = runTest {
    viewModel.onCompletion()

    coVerify { userPreferencesRepository.setHasCompletedOnboarding(true) }
  }
}