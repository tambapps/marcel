package com.tambapps.marcel.compiler

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Path
import java.nio.file.Paths

// maybe someday I'll include them in this project
@Disabled
class AdventOfCodeTest: AbstractCompilerTest() {

  @TestFactory
  fun testRunAllScripts(): Collection<DynamicTest?>? {
    val path: Path = Paths.get(javaClass.getResource("/aoc_tests").toURI())
    val scriptPaths = path.toFile().list { dir, name -> name.endsWith(".marcel") }

    return scriptPaths.map { path: String ->
      DynamicTest.dynamicTest(path.removeSuffix(".marcel")) {

        eval("/$path")
      }
    }
  }

}