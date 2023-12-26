package com.tambapps.marcel.semantic.method

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.literal.IntNode
import com.tambapps.marcel.semantic.AstNodeComposer
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.lang.CharSequence

class JavaMethodTest: AstNodeComposer() {

  override val typeResolver = JavaTypeResolver()
  override val caster = AstNodeCaster(typeResolver)

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

  @Disabled // TODO make vararg method matching
  @Test
  fun testVarArg() {
    val joinMethod = ReflectJavaMethod(java.lang.String::class.java.getMethod("join", CharSequence::class.java, Array<CharSequence>::class.java))


    assertTrue(joinMethod.matches(typeResolver, listOf(string("s1"))))
    assertTrue(joinMethod.matches(typeResolver, listOf(string("s1"), string("s2"))))
    assertTrue(joinMethod.matches(typeResolver, listOf(string("s1"), string("s2"), string("s3"))))
    assertTrue(joinMethod.matches(typeResolver, listOf(string("s1"), array(CharSequence::class.javaType.arrayType, string("s3")))))
  }

  private fun cstNode() = IntNode(null, 0, LexToken.DUMMY)
}