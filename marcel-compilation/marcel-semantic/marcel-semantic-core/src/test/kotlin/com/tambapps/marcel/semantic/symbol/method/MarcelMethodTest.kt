package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import marcel.lang.Script
import marcel.util.primitives.collections.IntCollection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.function.IntPredicate

class MarcelMethodTest {

  @Test
  fun testMatch() {
    val m1 = MarcelMethodImpl(JavaType.Object, Visibility.PUBLIC, "name", Nullness.UNKNOWN, mutableListOf(
      MethodParameter(JavaType.String, Nullness.UNKNOWN, "url")
    ), JavaType.void, isDefault = false, isAbstract = false, isStatic = false, isConstructor = true)
    val m2 = MarcelMethodImpl(JavaType.Object, Visibility.PUBLIC, "name", Nullness.UNKNOWN, mutableListOf(
      MethodParameter(JavaType.String, Nullness.UNKNOWN, "url"),
      MethodParameter(JavaType.String, Nullness.UNKNOWN, "contentType", StringConstantNode("", cstNode())),
      MethodParameter(JavaType.String, Nullness.UNKNOWN, "url", StringConstantNode("", cstNode())),
    ), JavaType.void, isDefault = false, isAbstract = false, isStatic = false, isConstructor = true)

    assertFalse(m1.matches(m2))
    assertFalse(m2.matches(m1))
  }

  @Test
  fun testMethodReturnTypeNullness() {
    val method = ReflectJavaMethod(IntCollection::class.java.getDeclaredMethod("find", IntPredicate::class.java))
    assertEquals(Nullness.NULLABLE, method.nullness)
    assertEquals(Nullness.NOT_NULL, method.parameters.first().nullness)

  }

  @Test
  fun testMethodArgumentNullness() {
    val method = ReflectJavaMethod(Script::class.java.getDeclaredMethod("println", JavaType.Object.realClazz))
    assertEquals(Nullness.NULLABLE, method.parameters.first().nullness)
  }

  @Test
  fun testMethodEquals() {
    // Create methods with the same essential properties
    val method1 = MarcelMethodImpl(
      ownerClass = JavaType.String,
      visibility = Visibility.PUBLIC,
      name = "testMethod",
      nullness = Nullness.NOT_NULL,
      parameters = listOf(
        MethodParameter(JavaType.int, Nullness.NOT_NULL, "param1"),
        MethodParameter(JavaType.boolean, Nullness.NULLABLE, "param2")
      ),
      returnType = JavaType.void
    )

    val method2 = MarcelMethodImpl(
      ownerClass = JavaType.String,
      visibility = Visibility.PRIVATE, // Different visibility
      name = "testMethod",
      nullness = Nullness.NULLABLE, // Different nullness
      parameters = listOf(
        MethodParameter(JavaType.int, Nullness.NOT_NULL, "param1"),
        MethodParameter(JavaType.boolean, Nullness.NULLABLE, "param2")
      ),
      returnType = JavaType.void,
      isDefault = true, // Different default
      isAbstract = true, // Different abstract
      isStatic = true    // Different static
    )

    // These methods should be equal despite different visibility, nullness, and modifiers
    // because AbstractMethod equals only checks name, ownerClass, parameters, and returnType
    assertEquals(method1, method2)
    assertEquals(method1.hashCode(), method2.hashCode())

    // Create a method with a different name
    val method3 = MarcelMethodImpl(
      ownerClass = JavaType.String,
      visibility = Visibility.PUBLIC,
      name = "differentName", // Different name
      nullness = Nullness.NOT_NULL,
      parameters = listOf(
        MethodParameter(JavaType.int, Nullness.NOT_NULL, "param1"),
        MethodParameter(JavaType.boolean, Nullness.NULLABLE, "param2")
      ),
      returnType = JavaType.void
    )

    // Different name means not equal
    assertNotEquals(method1, method3)
    assertNotEquals(method1.hashCode(), method3.hashCode())

    // Create a method with a different owner class
    val method4 = MarcelMethodImpl(
      ownerClass = JavaType.Object, // Different owner
      visibility = Visibility.PUBLIC,
      name = "testMethod",
      nullness = Nullness.NOT_NULL,
      parameters = listOf(
        MethodParameter(JavaType.int, Nullness.NOT_NULL, "param1"),
        MethodParameter(JavaType.boolean, Nullness.NULLABLE, "param2")
      ),
      returnType = JavaType.void
    )

    // Different owner means not equal
    assertNotEquals(method1, method4)
    assertNotEquals(method1.hashCode(), method4.hashCode())

    // Create a method with different parameters
    val method5 = MarcelMethodImpl(
      ownerClass = JavaType.String,
      visibility = Visibility.PUBLIC,
      name = "testMethod",
      nullness = Nullness.NOT_NULL,
      parameters = listOf(
        MethodParameter(JavaType.int, Nullness.NOT_NULL, "param1"),
        MethodParameter(JavaType.String, Nullness.NULLABLE, "differentParam") // Different parameter type and name
      ),
      returnType = JavaType.void
    )

    // Different parameters means not equal
    assertNotEquals(method1, method5)
    assertNotEquals(method1.hashCode(), method5.hashCode())

    // Create a method with a different return type
    val method6 = MarcelMethodImpl(
      ownerClass = JavaType.String,
      visibility = Visibility.PUBLIC,
      name = "testMethod",
      nullness = Nullness.NOT_NULL,
      parameters = listOf(
        MethodParameter(JavaType.int, Nullness.NOT_NULL, "param1"),
        MethodParameter(JavaType.boolean, Nullness.NULLABLE, "param2")
      ),
      returnType = JavaType.int // Different return type
    )

    // Different return type means not equal
    assertNotEquals(method1, method6)
    assertNotEquals(method1.hashCode(), method6.hashCode())

    // Test with a completely different object type
    assertNotEquals(method1, "not a method")
  }

  private fun cstNode() = IntCstNode(null, 0, LexToken.DUMMY)

}