package com.tambapps.marcel.compiler

import com.tambapps.marcel.semantic.analysis.SemanticConfiguration
import com.tambapps.marcel.semantic.transform.SemanticPurpose
import marcel.lang.Script

data class CompilerConfiguration(
  val semanticConfiguration: SemanticConfiguration = SemanticConfiguration(),
  val classVersion: Int = computeClassVersion(),
  val dumbbellEnabled: Boolean = false,
) {

  companion object {
    @JvmStatic
    val DEFAULT_VERSION = 61 // Java 17
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
    fun getClassVersion(version: String?) = version?.let { VERSION_MAP[it] } ?: DEFAULT_VERSION

    @JvmStatic
    fun computeClassVersion(): Int {
      val version: String? = System.getProperty("java.specification.version")
      return getClassVersion(version)
    }
  }


  fun withPurpose(purpose: SemanticPurpose) = copy(
    semanticConfiguration.copy(purpose = purpose)
  )
}