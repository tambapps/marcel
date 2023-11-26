package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
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
    private val CLASS_SCOPE = ClassScope(TYPE_RESOLVER, Object, null, emptyList())
    private val METHOD = MethodNode("foo", mutableListOf(),  Visibility.PUBLIC, Object, isStatic = false, LexToken.DUMMY, LexToken.DUMMY, Object)
  }


  @Test
  fun testAddLocalVariable() {
    val scope = MethodScope(CLASS_SCOPE, METHOD)

    scope.addLocalVariable(int, "myInt")

    assertTrue(scope.hasLocalVariable("myInt"))
    assertEquals(int, scope.findLocalVariable("myInt")?.type)
    assertEquals("myInt", scope.findLocalVariable("myInt")?.name)
    assertEquals(1, scope.findLocalVariable("myInt")?.index)
    assertThrows<MarcelSemanticException> { scope.addLocalVariable(int, "myInt") }
  }

  @Test
  fun testInnerScope() {
    val scope = MethodScope(CLASS_SCOPE, METHOD)
    val innerScope = MethodInnerScope(scope)

    scope.addLocalVariable(int, "var1")
    assertTrue(scope.hasLocalVariable("var1"))
    assertTrue(innerScope.hasLocalVariable("var1"))
    assertSame(scope.findField("var1"), innerScope.findField("var1"))
    assertEquals(1, scope.findLocalVariable("var1")?.index)


    innerScope.addLocalVariable(int, "myInt")
    assertTrue(innerScope.hasLocalVariable("myInt"))
    assertFalse(scope.hasLocalVariable("myInt"))
    assertEquals(2, innerScope.findLocalVariable("myInt")?.index)

    assertThrows<MarcelSemanticException> { innerScope.addLocalVariable(int, "var1") }
    assertThrows<MarcelSemanticException> { innerScope.addLocalVariable(int, "myInt") }

  }
}