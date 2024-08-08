package com.inasweaterpoorlyknit.core.common

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class EventTest {
  @Test
  fun getContentIfNotHandled() {
    val event = Event("test")

    val firstValue = event.getContentIfNotHandled()
    val secondValue = event.getContentIfNotHandled()

    assertEquals("test", firstValue)
    assertNull(secondValue)
  }
}