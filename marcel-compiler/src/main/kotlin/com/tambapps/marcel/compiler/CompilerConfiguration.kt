package com.tambapps.marcel.compiler

data class CompilerConfiguration(val classVersion: Int) {

  companion object {
    @JvmStatic // 52 is for Java 8
    val DEFAULT_CONFIGURATION = CompilerConfiguration(52)
  }
}