package com.tambapps.marcel.android.marshell.repl.console

object NoOpPrinter: Printer {
  override fun print(o: Any?) {}

  override fun println(o: Any?) {}

  override fun println() {}
}