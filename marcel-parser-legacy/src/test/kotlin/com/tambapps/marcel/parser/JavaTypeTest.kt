package com.tambapps.marcel.parser

import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.JavaType.Companion.Integer
import com.tambapps.marcel.parser.type.JavaType.Companion.of
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JavaTypeTest {

  companion object {
    val Number = of(Number::class.java)
  }

  @Test
  fun testIsAssignableFrom() {
    assertTrue(Number.isAssignableFrom(Integer))
    assertFalse(Integer.isAssignableFrom(of(Number::class.java)))
  }

  @Test
  fun testIsAssignableFromArray() {
    assertTrue(of(Array<Any>::class.java).isAssignableFrom(of(Array<Int>::class.java)))
  }
}