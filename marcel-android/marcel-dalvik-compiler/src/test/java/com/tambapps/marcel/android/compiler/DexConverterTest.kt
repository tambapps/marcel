package com.tambapps.marcel.android.compiler

import org.junit.Test

import java.io.File

class DexConverterTest {

  private val converter = DexConverter()

  @Test
  fun testCompileToDex() {

    converter.toDexJar(File("/Users/nfonkoua/Downloads/hyperpoet-marcel-1.4.0.jar"),
      File("/Users/nfonkoua/Downloads/test.jar"))
  }

}