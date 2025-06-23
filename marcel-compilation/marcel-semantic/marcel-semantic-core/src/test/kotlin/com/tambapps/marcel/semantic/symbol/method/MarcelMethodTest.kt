package com.tambapps.marcel.semantic.symbol.method

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import marcel.lang.Script
import marcel.util.primitives.collections.IntCollection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.function.IntPredicate

class MarcelMethodTest {

  @Test
  fun test() {
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

  private fun cstNode() = IntCstNode(null, 0, LexToken.DUMMY)
}