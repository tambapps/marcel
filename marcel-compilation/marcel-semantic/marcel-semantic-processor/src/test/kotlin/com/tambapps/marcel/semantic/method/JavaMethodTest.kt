package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.CharSequence
import java.lang.String

class JavaMethodTest {

  private val symbolResolver = MarcelSymbolResolver()

  @Test
  fun test() {
    val m1 = JavaMethodImpl(JavaType.Object, Visibility.PUBLIC, "name", mutableListOf(
      MethodParameter(JavaType.String, "url")
    ), JavaType.void, isDefault = false, isAbstract = false, isStatic = false, isConstructor = true)
    val m2 = JavaMethodImpl(JavaType.Object, Visibility.PUBLIC, "name", mutableListOf(
      MethodParameter(JavaType.String, "url"),
      MethodParameter(JavaType.String, "contentType", StringConstantNode("", cstNode())),
      MethodParameter(JavaType.String, "url", StringConstantNode("", cstNode())),
    ), JavaType.void, isDefault = false, isAbstract = false, isStatic = false, isConstructor = true)

    assertFalse(m1.matches(m2))
    assertFalse(m2.matches(m1))
  }

  @Test
  fun testVarArg() {
    val joinMethod = ReflectJavaMethod(String::class.java.getMethod("join", CharSequence::class.java, Array<CharSequence>::class.java))
    assertTrue(joinMethod.isVarArgs)

    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String)))
    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.String)))
    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.String, JavaType.String)))
    assertTrue(symbolResolver.matches(joinMethod, listOf(JavaType.String, CharSequence::class.javaType.arrayType)))

    assertFalse(symbolResolver.matches(joinMethod, emptyList()))
    assertFalse(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.int)))
    assertFalse(symbolResolver.matches(joinMethod, listOf(JavaType.String, JavaType.String, JavaType.int)))
    assertFalse(symbolResolver.matches(joinMethod, listOf(JavaType.String, Int::class.javaType.arrayType)))
  }

  private fun cstNode() = IntCstNode(null, 0, LexToken.DUMMY)
}