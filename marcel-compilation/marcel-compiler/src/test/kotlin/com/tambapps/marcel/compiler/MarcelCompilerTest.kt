package com.tambapps.marcel.compiler

import com.tambapps.marcel.semantic.symbol.type.JavaType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import marcel.lang.MarcelClassLoader
import marcel.lang.URLMarcelClassLoader
import org.jspecify.annotations.Nullable
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.jvm.javaClass


class MarcelCompilerTest: FunSpec({
  val compiler = MarcelCompiler(CompilerConfiguration())

  fun readResource(resourceName: String) = MarcelCompilerTest::class.java.getResource(resourceName)!!.readText()

  fun eval(resourceName: String, marcelClassLoader: MarcelClassLoader = URLMarcelClassLoader()): Any? {
    val className = resourceName.substring(resourceName.lastIndexOf('/') + 1, resourceName.lastIndexOf('.'))
    val result = compiler.compile(marcelClassLoader = marcelClassLoader, text = readResource(resourceName), fileName = className)
    val jarFile = Files.createTempFile("", "$className.jar").toFile()
    MarcelJarOutputStream(jarFile).use {
      it.writeClasses(result)
    }
    val script = marcelClassLoader.loadScript(className, jarFile)
    return script.run()
  }

  fun compiledTest(toCompileClassName: String, test: (MarcelClassLoader) -> Unit) {
    // compile the class defining method with default parameters
    val compiledClassJarFile = Files.createTempFile("", "$toCompileClassName.jar").toFile()
    compiler.compileToJar(className = toCompileClassName, text = readResource("/compiled_tests/$toCompileClassName.mcl"), compiledClassJarFile)
    val marcelClassLoader = URLMarcelClassLoader()
    marcelClassLoader.addJar(compiledClassJarFile)
    try {
      test.invoke(marcelClassLoader)
    } finally {
      compiledClassJarFile.delete()
    }
  }
  /* all script tests */
  for (testScriptName in
  Paths.get(javaClass.getResource("/tests")!!.toURI())
    .toFile().list { _, name -> name.endsWith(".mcl") }!!) {
    test("script $testScriptName") {
      eval("/tests/$testScriptName")
    }
  }

  test("script (manual)") {
    val manualScriptTest = "function_default_parameters.mcl"
    eval("/tests/$manualScriptTest")
  }

  // TODO test
  //  - annotations async are added for async functions.
  //  - nullable annotations are added
  /* compiled class tests */
  test("compiled class default parameters") {
    compiledTest("DefaultMethodParamsClass") { marcelClassLoader ->
      eval("/compiled_tests/test_function_default_parameters.mcl", marcelClassLoader)
    }
  }

  test("compiled class nullabla members") {
    compiledTest("NullableMembersClass") { marcelClassLoader ->
      val clazz = marcelClassLoader.loadClass("NullableMembersClass")
      clazz.getDeclaredField("aLong").apply {
        annotatedType.annotations.size shouldBeGreaterThan 0
        annotatedType.annotations.first().shouldBeInstanceOf<Nullable>()
      }
      clazz.getDeclaredConstructor(JavaType.Long.realClazz).apply {
        parameters.size shouldBe 1
        parameters.first().annotatedType.annotations.size shouldBeGreaterThan 0
        parameters.first().annotatedType.annotations.first()
          .shouldBeInstanceOf<Nullable>()
      }

      clazz.getDeclaredMethod("computeInteger").apply {
        parameters.size shouldBe 0
        annotatedReturnType.annotations.first().shouldBeInstanceOf<Nullable>()
      }

      clazz.getDeclaredMethod("sum", JavaType.Integer.realClazz, JavaType.Integer.realClazz).apply {
        parameters.size shouldBe 2
        parameters.first().annotatedType.annotations.size shouldBeGreaterThan 0
        parameters.first().annotatedType.annotations.first()
          .shouldBeInstanceOf<Nullable>()
        parameters[1].annotatedType.annotations.any {
          it is Nullable
        } shouldBe false
      }
    }
  }
})