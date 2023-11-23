package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.jar.BasicJarWriterFactory
import marcel.lang.Binding
import marcel.lang.IntRanges
import marcel.lang.URLMarcelClassLoader
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files

class MarcelEvaluatorTest {

  companion object {
    private val TEMP_DIR = Files.createTempDirectory("marcel-repl-tests").toFile()

    @JvmStatic
    @AfterAll
    fun clean(): Unit {
      TEMP_DIR.deleteRecursively()
    }
  }

  private val marcelClassLoader = URLMarcelClassLoader(MarcelEvaluatorTest::class.java.classLoader)
  private val compilerConfiguration = CompilerConfiguration(dumbbellEnabled = false)
  private val binding: Binding = Binding()
  private val typeResolver = ReplJavaTypeResolver(marcelClassLoader, binding)
  private val replCompiler = MarcelReplCompiler(compilerConfiguration, marcelClassLoader, typeResolver)
  private val jarWriterFactory = BasicJarWriterFactory()
  private lateinit var evaluator: MarcelEvaluator

  @BeforeEach
  fun init() {
    evaluator = MarcelEvaluator(binding, replCompiler, marcelClassLoader, jarWriterFactory, TEMP_DIR)
  }

  @Test
  fun testObjectBoundField() {
    assertEquals(IntRanges.of(1, 2), evaluator.eval("a = 1..2"))
    assertEquals(IntRanges.of(1, 2), evaluator.eval("a"))
  }

  @Test
  fun testPrimitiveBoundField() {
    assertEquals(1, evaluator.eval("a = 1"))
    assertEquals(1, evaluator.eval("a"))
  }

}