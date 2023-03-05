package com.tambapps.marcel.compiler

data class CompilerConfiguration(
  val classVersion: Int = 52,
  val dumbbellEnabled: Boolean = false
) {

  companion object {
    @JvmStatic // 52 is for Java 8
    val DEFAULT_CONFIGURATION = CompilerConfiguration()
  }
}