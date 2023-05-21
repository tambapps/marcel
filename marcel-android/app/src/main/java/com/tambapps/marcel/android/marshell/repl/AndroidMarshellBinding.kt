package com.tambapps.marcel.android.marshell.repl

import marcel.lang.Binding
import marcel.lang.printer.Printer

// TODO delete me
class AndroidMarshellBinding(printer: Printer? = null): Binding() {

  companion object {
    const val OUT_VAR_NAME = "out"
  }
  init {
    if (printer != null) {
      super.setVariable(OUT_VAR_NAME, printer)
    }
  }

  fun setOut(printer: Printer?) {
    super.setVariable(OUT_VAR_NAME, printer)
  }

  override fun setVariable(name: String?, newValue: Any?) {
    if (name == OUT_VAR_NAME) {
      throw IllegalAccessException("Cannot modify out")
    }
    super.setVariable(name, newValue)
  }

  override fun removeVariable(name: String?): Boolean {
    if (name == OUT_VAR_NAME) {
      throw IllegalAccessException("Cannot modify out")
    }
    return super.removeVariable(name)
  }
}