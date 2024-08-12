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

  @Test
  fun combineSix() = runTest {
    val flow1 = flowOf(1)
    val flow2 = flowOf(2)
    val flow3 = flowOf(3)
    val flow4 = flowOf(4)
    val flow5 = flowOf(5)
    val flow6 = flowOf(6)
    val combineExpected = listOf(
      flow1.first(),
      flow2.first(),
      flow3.first(),
      flow4.first(),
      flow5.first(),
      flow6.first(),
    )

    val combineActual = combine(flow1, flow2, flow3, flow4, flow5, flow6) { a, b, c, d, e, f ->
      listOf(a, b, c, d, e, f)
    }.first()

    assertEquals(combineExpected, combineActual)
  }
}
