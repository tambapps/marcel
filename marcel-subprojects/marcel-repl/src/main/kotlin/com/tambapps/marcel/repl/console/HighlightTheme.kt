package com.tambapps.marcel.repl.console


data class HighlightTheme<T>(
  val variable: T,
  val function: T,
  val string: T,
  val stringTemplate: T,
  val keyword: T,
  val comment: T,
  val number: T,
  val default: T,
)
