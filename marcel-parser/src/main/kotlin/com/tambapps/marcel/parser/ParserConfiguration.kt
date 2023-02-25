package com.tambapps.marcel.parser

data class ParserConfiguration(
  /**
   * A flag whether to tell whether to make classes defined in scripts inner classes from the script =&gt false, or
   * dependant (with no outer class) =&lt true
   */
  val independentScriptInnerClasses: Boolean
)