package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType.Companion.DynamicObject
import com.tambapps.marcel.semantic.type.JavaType.Companion.Integer
import com.tambapps.marcel.semantic.type.JavaType.Companion.Object
import com.tambapps.marcel.semantic.type.JavaType.Companion.commonType
import com.tambapps.marcel.semantic.type.JavaType.Companion.double
import com.tambapps.marcel.semantic.type.JavaType.Companion.float
import com.tambapps.marcel.semantic.type.JavaType.Companion.int
import com.tambapps.marcel.semantic.type.JavaType.Companion.intList
import com.tambapps.marcel.semantic.type.JavaType.Companion.long
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JavaTypeTest {

  @Test
  fun testIsAssignable() {
    assertTrue(Object.isAssignableFrom(DynamicObject))
    assertFalse(DynamicObject.isAssignableFrom(Object))
    assertFalse(int.isAssignableFrom(long))
  }

  @Test
  fun testImplements() {
    assertTrue(intList.implements(List::class.javaType))
    assertTrue(intList.implements(Collection::class.javaType))
    assertFalse(intList.implements(Set::class.javaType))
  }

  @Test
  fun testCommonTypes() {
    assertEquals(float, commonType(int, float))
    assertEquals(float, commonType(float, float))
    assertEquals(double, commonType(int, double))
    assertEquals(Object, commonType(long, double))
    assertEquals(List::class.javaType, commonType(List::class.javaType, intList))
    assertEquals(Object, commonType(Object, Object))
  }

  @Test
  fun testArrayType() {
    assertEquals(IntArray::class.javaType, int.arrayType)
    assertEquals(Array<Int>::class.javaType, Integer.arrayType)
  }
}