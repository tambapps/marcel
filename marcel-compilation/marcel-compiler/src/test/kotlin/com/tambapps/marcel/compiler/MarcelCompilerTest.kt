package com.tambapps.marcel.compiler

import io.kotest.core.spec.style.FunSpec
import marcel.lang.MarcelClassLoader
import marcel.lang.URLMarcelClassLoader
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

  /* all script tests */
  for (testScriptName in
  Paths.get(javaClass.getResource("/tests")!!.toURI())
    .toFile().list { _, name -> name.endsWith(".mcl") }!!) {
    test("script $testScriptName") {
      eval("/tests/$testScriptName")
    }
  }

  test("script (manual)") {
    val manualScriptTest = "stringify.mcl"
    eval("/tests/$manualScriptTest")
  }

  // TODO test
  //  - annotations async are added for async functions.
  //  - nullable annotations are added
  /* compiled class tests */
  test("compiled class default parameters") {
    // compile the class defining method with default parameters
    val compiledClassJarFile = Files.createTempFile("", "DefaultMethodParamsClass.jar").toFile()
    compiler.compileToJar(className = "DefaultMethodParamsClass", text = readResource("/compiled_tests/DefaultMethodParamsClass.mcl"), compiledClassJarFile)
    val marcelClassLoader = URLMarcelClassLoader()
    marcelClassLoader.addJar(compiledClassJarFile)
    try {
      eval("/compiled_tests/test_function_default_parameters.mcl", marcelClassLoader)
    } finally {
      compiledClassJarFile.delete()
    }
  }
})