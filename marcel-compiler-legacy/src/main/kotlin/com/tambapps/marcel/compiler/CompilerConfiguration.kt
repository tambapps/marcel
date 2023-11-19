package com.tambapps.marcel.compiler

data class CompilerConfiguration(
  val classVersion: Int = 52, // 52 is for Java 8
  val dumbbellEnabled: Boolean = false
) {

}