package com.tambapps.marcel.semantic.symbol.variable

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.method.MarcelMethodImpl
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.field.BoundField
import com.tambapps.marcel.semantic.symbol.variable.field.CompositeField
import com.tambapps.marcel.semantic.symbol.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassField
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassFieldImpl
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.symbol.variable.field.MethodField
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class VariableTest {

  @Test
  fun testLocalVariable() {
    val variable = LocalVariable(
      type = JavaType.int,
      name = "testVar",
      nbSlots = 1,
      index = 1,
      isFinal = true,
      nullness = Nullness.NOT_NULL
    )

    assertEquals(JavaType.int, variable.type)
    assertEquals("testVar", variable.name)
    assertEquals(1, variable.nbSlots)
    assertEquals(1, variable.index)
    assertTrue(variable.isFinal)
    assertEquals(Nullness.NOT_NULL, variable.nullness)
    assertTrue(variable.isGettable)
    assertFalse(variable.isSettable)
    assertTrue(variable.isVisibleFrom(JavaType.Object))

    // Test withIndex
    val indexedVar = variable.withIndex(2)
    assertEquals(2, indexedVar.index)
    assertEquals(variable.name, indexedVar.name)
    assertEquals(variable.type, indexedVar.type)
  }

  @Test
  fun testLocalVariableEquality() {
    val var1 = LocalVariable(
      type = JavaType.int,
      name = "testVar",
      nbSlots = 1,
      index = 1,
      isFinal = true,
      nullness = Nullness.NOT_NULL
    )

    val var2 = LocalVariable(
      type = JavaType.int,
      name = "testVar",
      nbSlots = 1,
      index = 1,
      isFinal = true,
      nullness = Nullness.NOT_NULL
    )

    val var3 = LocalVariable(
      type = JavaType.int,
      name = "testVar",
      nbSlots = 1,
      index = 2, // Different index
      isFinal = true,
      nullness = Nullness.NOT_NULL
    )

    assertEquals(var1, var2)
    assertNotEquals(var1, var3)
    assertEquals(var1.hashCode(), var2.hashCode())
    assertNotEquals(var1.hashCode(), var3.hashCode())
  }

  @Test
  fun testNonFinalLocalVariable() {
    val variable = LocalVariable(
      type = JavaType.int,
      name = "testVar",
      nbSlots = 1,
      index = 1,
      isFinal = false,
      nullness = Nullness.NOT_NULL
    )

    assertFalse(variable.isFinal)
    assertTrue(variable.isGettable)
    assertTrue(variable.isSettable)
  }

  @Test
  fun testArrayLengthField() {
    val field = MarcelArrayLengthField(JavaType.int.arrayType)

    assertEquals("length", field.name)
    assertEquals(JavaType.int, field.type)
    assertTrue(field.isFinal)
    assertEquals(Nullness.NOT_NULL, field.nullness)
    assertTrue(field.isGettable)
    assertFalse(field.isSettable)
  }

  @Test
  fun testCompositeField() {
    val field1 = MethodField.fromGetter(
      MarcelMethodImpl(
        ownerClass = JavaType.Object,
        visibility = Visibility.PUBLIC,
        name = "getFoo",
        nullness = Nullness.NOT_NULL,
        parameters = emptyList(),
        returnType = JavaType.int,
      )
    )
    val field2 = JavaClassFieldImpl(
      visibility = Visibility.PUBLIC,
      type = JavaType.int,
      name = "foo",
      owner = JavaType.Object,
      isStatic = false,
      isFinal = false,
      nullness = Nullness.NOT_NULL,
      isSettable = false
    )

    val compositeField = CompositeField(listOf(field1, field2))

    assertEquals(field1.name, compositeField.name)
    assertEquals(field1.type, compositeField.type)
    assertFalse(compositeField.isFinal)
    assertEquals(field1.nullness, compositeField.nullness)
    assertTrue(compositeField.isGettable)
    assertFalse(compositeField.isSettable)
  }

  @Test
  fun testVariableVisitorPattern() {
    // Create a test visitor that counts the type of variables visited
    val visitor = object : VariableVisitor<String> {
      override fun visit(variable: LocalVariable) = "LocalVariable"
      override fun visit(variable: BoundField) = "BoundField"
      override fun visit(variable: DynamicMethodField) = "DynamicMethodField"
      override fun visit(variable: JavaClassField) = "JavaClassField"
      override fun visit(variable: MarcelArrayLengthField) = "MarcelArrayLengthField"
      override fun visit(variable: CompositeField) = "CompositeField"
      override fun visit(variable: MethodField) = "MethodField"
    }

    val localVar = LocalVariable(
      type = JavaType.int,
      name = "testVar",
      nbSlots = 1,
      index = 1,
      isFinal = true,
      nullness = Nullness.NOT_NULL
    )

    val arrayLengthField = MarcelArrayLengthField(JavaType.int.arrayType)

    // Test visitor pattern implementation
    assertEquals("LocalVariable", localVar.accept(visitor))
    assertEquals("MarcelArrayLengthField", arrayLengthField.accept(visitor))
  }
}
