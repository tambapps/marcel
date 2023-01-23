package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Script
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.math.absoluteValue


class MarcelCompilerTest {

  private val compiler = MarcelCompiler()

  @TestFactory
  fun testRunAllScripts(): Collection<DynamicTest?>? {
    val path: Path = Paths.get(javaClass.getResource("/").toURI())
    val scriptPaths = path.toFile().list { dir, name -> name.endsWith(".marcel") }

    return  scriptPaths.map { path: String ->
      DynamicTest.dynamicTest(path.removeSuffix(".marcel")) {
        eval("/$path")
      }
    }
  }

  @Test
  fun testScript() {
    val eval = eval("/test1.marcel")!!
    assertNotNull(eval)
    assertEquals(JavaType.Object.realClazz, eval.javaClass)
  }

  @Test
  fun testReturnNull() {
    val eval = eval("/test_return_null.marcel")
    assertNull(eval)
  }

  @Test
  fun testBool() {
    val eval = eval("/test_bool.marcel")
    assertTrue(eval as Boolean)
  }

  @Test
  fun testIf() {
    val eval = eval("/test_if.marcel")
    assertTrue(eval as Boolean)
  }

  @Test
  fun testForLoop() {
    val eval = eval("/test_for_loop.marcel")
    assertEquals(45, eval)
  }

  @Test
  fun testWhileLoop() {

    val eval = eval("/test_while_loop.marcel")
    assertEquals(45, eval)
  }

  @Test
  fun testBreakLoop() {
    val eval = eval("/test_break.marcel")
    assertEquals(10, eval)
  }

  @Test
  fun testContinueLoop() {
    val eval = eval("/test_continue.marcel")
    assertEquals(1 + 2 + 4, eval)
  }

  @Test
  fun testScope() {
    val eval = eval("/test_scope.marcel")
    assertEquals(true, eval)
  }

  @Test
  fun testThis() {
    javaClass.getResource("/json")
    val path: Path = Paths.get(javaClass.getResource("/").toURI())
    println(Arrays.toString(path.toFile().list()))
    val eval = eval("/test_this.marcel")
    assertTrue(eval is Script)
  }

  @AfterEach
  fun dispose() {
    JavaType.clear()
  }

  private fun eval(resourceName: String): Any? {
    val result = javaClass.getResourceAsStream(resourceName).reader().use {
      // using hashCode to have unique names
      compiler.compile(it, "Test" + resourceName.hashCode().absoluteValue)
    }

    val jarFile = Files.createTempFile("", "${result.className}.jar").toFile()
    JarWriter().writeScriptJar(result.className, result.bytes, jarFile)

    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
    val clazz = classLoader.loadClass(result.className)
    return clazz.getMethod("run", Array<String>::class.java)
      .invoke(clazz.getDeclaredConstructor().newInstance(), arrayOf<String>())
  }
}