package com.tambapps.marcel.repl.console


data class HighlightTheme<T>(
  val field: T,
  val variable: T,
  val annotation: T,
  val function: T,
  val string: T,
  val stringTemplate: T,
  val keyword: T,
  val comment: T,
  val number: T,
  val default: T,
)
