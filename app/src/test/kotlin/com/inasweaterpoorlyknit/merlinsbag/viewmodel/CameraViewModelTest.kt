package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import android.app.Application
import com.inasweaterpoorlyknit.core.testing.MainDispatcherRule
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CameraViewModelTest {
  @get:Rule
  val mockkRule = MockKRule(this)

  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  @MockK lateinit var application: Application

  lateinit var viewModel: CameraViewModel

  @Before
  fun beforeEach() {
    viewModel = CameraViewModel(application = application)
  }

  @Test
  fun `Dialogs are hidden by default`() = runTest {
    assertFalse(viewModel.showPermissionsAlert)
  }
}