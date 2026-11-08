package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testAppNameConstant() {
    val appTitle = "TESTIFY"
    assertEquals("TESTIFY", appTitle)
    assertTrue(appTitle.isNotEmpty())
  }
}
