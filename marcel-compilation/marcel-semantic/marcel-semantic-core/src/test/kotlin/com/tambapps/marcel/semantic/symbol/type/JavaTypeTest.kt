package com.tambapps.marcel.semantic.symbol.type

import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.DynamicObject
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.Integer
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.List
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.Object
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.Set
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.String
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.commonType
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.double
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.float
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.int
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.intArray
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.intList
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.intSet
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.long
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.objectArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Optional

class JavaTypeTest {

  @Test
  fun testIsSelfOrSuper() {
    assertTrue(Object.isSelfOrSuper(Object))
    assertTrue(DynamicObject.isSelfOrSuper(Object))
    assertFalse(Object.isSelfOrSuper(DynamicObject))
  }

  @Test
  fun testIsAssignable() {
    assertTrue(Object.isAssignableFrom(DynamicObject))
    assertFalse(DynamicObject.isAssignableFrom(Object))
    assertFalse(int.isAssignableFrom(long))
  }

  @Test
  fun testImplements() {
    assertTrue(intList.implements(List))
    assertTrue(intList.implements(Collection::class.javaType))
    assertFalse(intList.implements(Set::class.javaType))
  }

  @Test
  fun testCommonTypes() {
    assertEquals(float, commonType(int, float))
    assertEquals(float, commonType(float, float))
    assertEquals(double, commonType(int, double))
    assertEquals(double, commonType(long, double))
    assertEquals(List, commonType(List, intList))
    assertEquals(Object, commonType(Object, Object))

    assertEquals(Object, commonType(RuntimeException::class.javaType, Optional::class.javaType))
    assertEquals(Object, commonType(Optional::class.javaType, RuntimeException::class.javaType))
  }

  @Test
  fun testArrayType() {
    assertEquals(IntArray::class.javaType, int.arrayType)
    assertEquals(Array<Int>::class.javaType, Integer.arrayType)
  }

  @Test
  fun testIsExtendedOrImplementedBy() {
    assertTrue(Object.isExtendedOrImplementedBy(DynamicObject))
    assertFalse(DynamicObject.isExtendedOrImplementedBy(Object))
    assertTrue(List.isExtendedOrImplementedBy(intList))
    assertFalse(intList.isExtendedOrImplementedBy(List))
  }

  @Test
  fun testWithGenericTypes() {
    val listWithStringType = List.withGenericTypes(String)
    assertEquals(1, listWithStringType.genericTypes.size)
    assertEquals(String, listWithStringType.genericTypes[0])
    
    val rawList = listWithStringType.raw()
    assertTrue(rawList.genericTypes.isEmpty())
    assertEquals(List.className, rawList.className)
  }

  @Test
  fun testSimpleName() {
    assertEquals("Object", Object.simpleName)
    assertEquals("String", String.simpleName)
    assertEquals("Integer", Integer.simpleName)
    assertEquals("int[]", int.arrayType.simpleName)
  }

  @Test
  fun testIsVisibleFrom() {
    assertTrue(Object.isVisibleFrom(String))
    assertTrue(String.isVisibleFrom(Object))
  }

  @Test
  fun testArray() {
    val intArray1D = int.array(1)
    assertEquals(int.arrayType, intArray1D)
    
    val intArray2D = int.array(2)
    assertEquals(int.arrayType.arrayType, intArray2D)
    
    val intArray3D = int.array(3)
    assertEquals(int.arrayType.arrayType.arrayType, intArray3D)
  }

  @Test
  fun testPrimitiveTypeProperties() {
    assertTrue(int.primitive)
    assertFalse(Integer.primitive)
    assertTrue(Integer.isPrimitiveObjectType)
    assertTrue(int.isPrimitiveOrObjectPrimitive)
    assertTrue(Integer.isPrimitiveOrObjectPrimitive)
    assertFalse(String.isPrimitiveOrObjectPrimitive)
  }

  @Test
  fun testListConvertable() {
    assertTrue(JavaType.isListConvertable(intList, intArray))
    assertTrue(JavaType.isListConvertable(List, objectArray))
    assertFalse(JavaType.isListConvertable(String, intArray))
  }

  @Test
  fun testSetConvertable() {
    assertTrue(JavaType.isSetConvertable(intSet, intArray))
    assertTrue(JavaType.isSetConvertable(Set, objectArray))
    assertFalse(JavaType.isSetConvertable(String, intArray))
  }

  @Test
  fun testHasGenericTypes() {
    assertFalse(Object.hasGenericTypes)
    assertFalse(String.hasGenericTypes)
    
    val listWithGeneric = List.withGenericTypes(String)
    assertTrue(listWithGeneric.hasGenericTypes)
  }

  @Test
  fun testCastTypeOperations() {
    assertEquals(int, int.asPrimitiveType)
    assertEquals(intArray, intArray.asArrayType)
  }
}