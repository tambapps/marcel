package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Script
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class MarcelCompilerTest: AbstractCompilerTest() {

  @TestFactory
  fun testRunAllScripts(): Collection<DynamicTest?> {
    val path: Path = Paths.get(javaClass.getResource("/tests").toURI())
    val scriptPaths = path.toFile().list { _, name -> name.endsWith(".mcl") }

    return scriptPaths.map { p: String ->
      DynamicTest.dynamicTest(p.removeSuffix(".mcl")) {
        eval("/tests/$p")
      }
    }
  }

  @Test
  fun testScript() {
    val eval = eval("/tests/test1.mcl")!!
    assertNotNull(eval)
    assertEquals(JavaType.Object.realClazz, eval.javaClass)
  }

  @Test
  fun testReturnNull() {
    val eval = eval("/tests/test_return_null.mcl")
    assertNull(eval)
  }

  @Test
  fun testBool() {
    val eval = eval("/tests/test_bool.mcl")
    assertTrue(eval as Boolean)
  }

  @Test
  fun testIf() {
    val eval = eval("/tests/test_if.mcl")
    assertTrue(eval as Boolean)
  }

  @Test
  fun testForLoop() {
    val eval = eval("/tests/test_for_loop.mcl")
    assertEquals(45, eval)
  }

  @Test
  fun testWhileLoop() {

    val eval = eval("/tests/test_while_loop.mcl")
    assertEquals(45, eval)
  }

  @Test
  fun testBreakLoop() {
    val eval = eval("/tests/test_break.mcl")
    assertEquals(10, eval)
  }

  @Test
  fun testContinueLoop() {
    val eval = eval("/tests/test_continue.mcl")
    assertEquals(1 + 2 + 4, eval)
  }

  @Test
  fun testScope() {
    val eval = eval("/tests/test_scope.mcl")
    assertEquals(true, eval)
  }

  @Test
  fun testThis() {
    javaClass.getResource("/json")
    val eval = eval("/tests/test_this.mcl")
    assertTrue(eval is Script)
  }

  @Disabled
  @Test
  fun manualTest() {
    val eval = eval("/tests/test_direct_field_access.mcl")
    println(eval)
  }
}