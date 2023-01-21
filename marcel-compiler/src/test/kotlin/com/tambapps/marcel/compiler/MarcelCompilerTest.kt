package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Script
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Arrays


class MarcelCompilerTest {

  private val compiler = MarcelCompiler()

  @Test
  fun testScript() {
    val eval = eval("/test1.marcel")!!
    assertNotNull(eval)
    assertEquals(JavaType.Object.realClassOrObject, eval.javaClass)
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


  @Test
  fun testAll() {
    javaClass.getResource("/json")
    val path: Path = Paths.get(javaClass.getResource("/").toURI())
    val scriptPaths = path.toFile().list { dir, name -> name.endsWith(".marcel") }
    for (path in scriptPaths) {
      println("Running $path")
      eval("/$path")
    }

  }
  private fun eval(resourceName: String): Any? {
    val result = javaClass.getResourceAsStream(resourceName).reader().use {
      compiler.compile(it, "Test")
    }

    val jarFile = Files.createTempFile("", "${result.className}.jar").toFile()
    JarWriter().writeScriptJar(result.className, result.bytes, jarFile)

    val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), MarcelCompiler::class.java.classLoader)
    val clazz = classLoader.loadClass(result.className)
    return clazz.getMethod("run", Array<String>::class.java)
      .invoke(clazz.getDeclaredConstructor().newInstance(), arrayOf<String>())
  }
}