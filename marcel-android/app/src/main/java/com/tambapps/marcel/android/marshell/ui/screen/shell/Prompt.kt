package com.tambapps.marcel.android.marshell.ui.screen.shell

data class Prompt constructor(val type: Type, val value: Any?) {
  val text: CharSequence = if (value is CharSequence) value else java.lang.String.valueOf(value)
  enum class Type {INPUT, SUCCESS_OUTPUT, ERROR_OUTPUT, STDOUT}
}