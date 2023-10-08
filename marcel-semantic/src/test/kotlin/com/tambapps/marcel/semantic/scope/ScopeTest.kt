package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaType.Companion.Object
import com.tambapps.marcel.semantic.type.JavaType.Companion.int
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ScopeTest {

  companion object {
    private val TYPE_RESOLVER = JavaTypeResolver()
    private val CLASS_SCOPE = ClassScope(JavaType.Object, TYPE_RESOLVER, emptyList())
    private val METHOD = MethodNode("foo", Visibility.PUBLIC, Object, isStatic = false, isConstructor = false, LexToken.dummy(), LexToken.dummy(), Object)
  }


  @Test
  fun testAddLocalVariable() {
    val scope = MethodScope(CLASS_SCOPE, METHOD, false)

    scope.addLocalVariable(int, "myInt")

    assertTrue(scope.hasVariable("myInt"))
    assertEquals(int, scope.findLocalVariable("myInt")?.type)
    assertEquals("myInt", scope.findLocalVariable("myInt")?.name)
    assertEquals(1, scope.findLocalVariable("myInt")?.index)
    assertThrows<MarcelSemanticException> { scope.addLocalVariable(int, "myInt") }
  }

  @Test
  fun testInnerScope() {
    val scope = MethodScope(CLASS_SCOPE, METHOD, false)
    val innerScope = MethodInnerScope(scope)

    scope.addLocalVariable(int, "var1")
    assertTrue(scope.hasVariable("var1"))
    assertTrue(innerScope.hasVariable("var1"))
    assertSame(scope.findVariable("var1"), innerScope.findVariable("var1"))
    assertEquals(1, scope.findLocalVariable("var1")?.index)


    innerScope.addLocalVariable(int, "myInt")
    assertTrue(innerScope.hasVariable("myInt"))
    assertFalse(scope.hasVariable("myInt"))
    assertEquals(2, innerScope.findLocalVariable("myInt")?.index)

    assertThrows<MarcelSemanticException> { innerScope.addLocalVariable(int, "var1") }
    assertThrows<MarcelSemanticException> { innerScope.addLocalVariable(int, "myInt") }

  }
}