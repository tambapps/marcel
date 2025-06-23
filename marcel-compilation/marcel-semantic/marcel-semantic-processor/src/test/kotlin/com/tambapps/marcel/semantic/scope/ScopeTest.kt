package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.processor.scope.MethodInnerScope
import com.tambapps.marcel.semantic.processor.scope.MethodScope
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.Object
import com.tambapps.marcel.semantic.symbol.type.JavaType.Companion.int
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.Nullness
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ScopeTest {

  companion object {
    private val TYPE_RESOLVER = MarcelSymbolResolver()
    private val CLASS_SCOPE = ClassScope(TYPE_RESOLVER, Object, null, ImportResolver.DEFAULT_IMPORTS)
    private val METHOD = MethodNode("foo", Nullness.UNKNOWN, mutableListOf(),  Visibility.PUBLIC, Object, isStatic = false, LexToken.DUMMY, LexToken.DUMMY, Object)
  }


  @Test
  fun testAddLocalVariable() {
    val scope = MethodScope(CLASS_SCOPE, METHOD)

    scope.addLocalVariable(int, "myInt", Nullness.UNKNOWN)

    assertTrue(scope.hasLocalVariable("myInt"))
    assertEquals(int, scope.findLocalVariable("myInt")?.type)
    assertEquals("myInt", scope.findLocalVariable("myInt")?.name)
    assertEquals(1, scope.findLocalVariable("myInt")?.index)
    assertThrows<MarcelSemanticException> { scope.addLocalVariable(int, "myInt", Nullness.UNKNOWN) }
  }

  @Test
  fun testInnerScope() {
    val scope = MethodScope(CLASS_SCOPE, METHOD)
    val innerScope = MethodInnerScope(scope)

    scope.addLocalVariable(int, "var1", Nullness.UNKNOWN)
    assertTrue(scope.hasLocalVariable("var1"))
    assertTrue(innerScope.hasLocalVariable("var1"))
    assertSame(scope.findField("var1"), innerScope.findField("var1"))
    assertEquals(1, scope.findLocalVariable("var1")?.index)


    innerScope.addLocalVariable(int, "myInt", Nullness.UNKNOWN)
    assertTrue(innerScope.hasLocalVariable("myInt"))
    assertFalse(scope.hasLocalVariable("myInt"))
    assertEquals(2, innerScope.findLocalVariable("myInt")?.index)

    assertThrows<MarcelSemanticException> { innerScope.addLocalVariable(int, "var1", Nullness.UNKNOWN) }
    assertThrows<MarcelSemanticException> { innerScope.addLocalVariable(int, "myInt", Nullness.UNKNOWN) }

  }
}