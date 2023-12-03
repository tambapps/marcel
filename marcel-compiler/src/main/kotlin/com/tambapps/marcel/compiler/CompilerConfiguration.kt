package com.tambapps.marcel.compiler

data class CompilerConfiguration(
  val classVersion: Int = computeClassVersion(), // 52 is for Java 8
  val dumbbellEnabled: Boolean = false
) {
  companion object {
    @JvmStatic
    val DEFAULT_VERSION = 52
    @JvmStatic
    val VERSION_MAP = mutableMapOf(
      Pair("1.8", 52),
      Pair("8", 52),
      Pair("9", 53),
      Pair("10", 54),
      Pair("11", 55),
      Pair("12", 56),
      Pair("13", 57),
      Pair("14", 58),
      Pair("15", 59),
      Pair("16", 60),
      Pair("17", 61),
      Pair("18", 62),
      Pair("19", 63),
      Pair("20", 64),
    )

    @JvmStatic
    fun computeClassVersion(): Int {
      val version: String? = System.getProperty("java.specification.version")
      return version?.let { VERSION_MAP[it] } ?: DEFAULT_VERSION
    }
  }
}