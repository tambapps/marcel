package com.tambapps.marcel.parser

data class ParserConfiguration(
  /**
   * A flag whether to tell whether to make classes defined in scripts inner classes from the script =&gt false, or
   * dependant (with no outer class) =&lt true
   */
  val independentScriptInnerClasses: Boolean = false,

  /**
   * Interfaces to associate with the script class when parsing them
   */
  val scriptInterfaces: List<Class<*>> = emptyList(),

  /**
   * The super class to use for the script class
   */
  val scriptClass: Class<*>? = null
)