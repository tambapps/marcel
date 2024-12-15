package com.tambapps.marcel.compiler

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.nio.file.Path
import java.nio.file.Paths

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

  @Disabled
  @Test
  fun manualTest() {
    val eval = eval("/tests/enums.mcl")
    println(eval)
  }
}