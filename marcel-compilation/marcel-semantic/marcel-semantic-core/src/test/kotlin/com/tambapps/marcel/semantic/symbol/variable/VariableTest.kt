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


  @Test
  fun testFieldEquals() {
    // Fields consider name and owner for equals/hashCode
    val field1 = JavaClassFieldImpl(
      visibility = Visibility.PUBLIC,
      type = JavaType.int,
      name = "myField",
      owner = JavaType.String,
      isStatic = true,
      isFinal = true,
      nullness = Nullness.NOT_NULL,
      isSettable = false
    )

    val field2 = JavaClassFieldImpl(
      visibility = Visibility.PRIVATE, // Different visibility
      type = JavaType.int,
      name = "myField", // Same name
      owner = JavaType.String, // Same owner
      isStatic = false, // Different static flag
      isFinal = false, // Different final flag
      nullness = Nullness.NULLABLE, // Different nullness
      isSettable = true // Different settable flag
    )

    val field3 = JavaClassFieldImpl(
      visibility = Visibility.PUBLIC,
      type = JavaType.int,
      name = "differentFieldName", // Different name
      owner = JavaType.String,
      isStatic = true,
      isFinal = true,
      nullness = Nullness.NOT_NULL,
      isSettable = false
    )

    val field4 = JavaClassFieldImpl(
      visibility = Visibility.PUBLIC,
      type = JavaType.int,
      name = "myField",
      owner = JavaType.int, // Different owner
      isStatic = true,
      isFinal = true,
      nullness = Nullness.NOT_NULL,
      isSettable = false
    )

    // Same type, name and owner means equal, regardless of other properties
    assertEquals(field1, field2)
    assertEquals(field1.hashCode(), field2.hashCode())

    // Different name means not equal
    assertNotEquals(field1, field3)
    assertNotEquals(field1.hashCode(), field3.hashCode())

    // Different owner means not equal
    assertNotEquals(field1, field4)
    assertNotEquals(field1.hashCode(), field4.hashCode())

    // Different field implementations with same name and owner should be equal
    val methodField = MethodField.fromGetter(
      MarcelMethodImpl(
        ownerClass = JavaType.String, // Same owner as field1
        visibility = Visibility.PUBLIC,
        name = "getMyField", // will be transformed to field name "myField"
        nullness = Nullness.NOT_NULL,
        parameters = emptyList(),
        returnType = JavaType.int,
      )
    )

    assertEquals(field1, methodField)

    // Different types of variables are never equal
    assertNotEquals(field1, "not a field")
  }
}
