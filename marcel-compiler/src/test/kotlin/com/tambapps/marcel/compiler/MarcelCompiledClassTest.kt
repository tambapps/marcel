package com.tambapps.marcel.compiler

import marcel.lang.URLMarcelClassLoader
import org.junit.jupiter.api.Test

class MarcelCompiledClassTest: AbstractCompilerTest() {

  // test that default parameters are loaded correctly from compiled classes
  @Test
  fun testDefaultParameters() {
    val compiledClassJarFile = writeJar("DefaultMethodParamsClass", getResourceText("/compiled_tests/DefaultMethodParamsClass.mcl"))
    val marcelClassLoader = URLMarcelClassLoader()

    marcelClassLoader.addLibraryJar(compiledClassJarFile)

    val compiler = MarcelCompiler()

    val className = "test_function_default_parameters"
    val result = compiler.compile(scriptLoader = marcelClassLoader, text = getResourceText("/compiled_tests/test_function_default_parameters.mcl"), className = className)

    val jarFile = writeJar(result, className)

    try {
      val script = marcelClassLoader.loadScript(className, jarFile)
      script.run()
    } finally {
      jarFile.delete()
      compiledClassJarFile.delete()
    }
  }
}