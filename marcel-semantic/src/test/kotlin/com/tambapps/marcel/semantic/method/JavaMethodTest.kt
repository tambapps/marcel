package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class JavaMethodTest {

  private val typeResolver = JavaTypeResolver()
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

    assertFalse(m1.matches(typeResolver, m2.name, m2.parameters, strict = true))
    assertFalse(m2.matches(typeResolver, m1.name, m1.parameters, strict = true))
  }

  private fun cstNode() = IntCstNode(null, 0, LexToken.DUMMY)
}