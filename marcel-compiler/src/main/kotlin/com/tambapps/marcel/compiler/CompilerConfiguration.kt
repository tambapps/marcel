package com.tambapps.marcel.compiler

import marcel.lang.Script

data class CompilerConfiguration(
  val classVersion: Int = computeClassVersion(),
  val dumbbellEnabled: Boolean = false,
  val scriptClass: Class<*> = Script::class.java
) {

  constructor(classVersion: Int, dumbbellEnabled: Boolean): this(classVersion, dumbbellEnabled, Script::class.java)

  companion object {
    @JvmStatic
    val DEFAULT_VERSION = 52 // 52 is for Java 8
    // https://stackoverflow.com/questions/9170832/list-of-java-class-file-format-major-version-numbers
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
      Pair("21", 65),
      Pair("22", 66),
    )

    @JvmStatic
    fun computeClassVersion(): Int {
      val version: String? = System.getProperty("java.specification.version")
      return version?.let { VERSION_MAP[it] } ?: DEFAULT_VERSION
    }
  }
}