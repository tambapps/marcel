package com.tambapps.marcel.android.marshell.ui.screen.shell

data class Prompt(val type: Type, val text: CharSequence) {
  enum class Type {INPUT, SUCCESS_OUTPUT, ERROR_OUTPUT, STDOUT}
}