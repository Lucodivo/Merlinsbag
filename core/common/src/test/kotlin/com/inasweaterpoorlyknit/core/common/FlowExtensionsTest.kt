package com.inasweaterpoorlyknit.core.common

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FlowExtensionsTest {
  @Test
  fun listMapTest() = runTest {
    val flowValues = listOf(1, 2, 3)
    val expectedResults = flowValues.map { it.toString() }
    val flow = flowOf(flowValues)

    val actualResults = flow.listMap { it.toString() }.first()

    assertEquals(expectedResults, actualResults)
  }
}
