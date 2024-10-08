package com.tambapps.marcel.android.marshell.workout

import com.tambapps.marcel.android.marshell.repl.console.Printer

internal class StringBuilderPrinter: Printer {
  private val builder = StringBuilder()

  override fun print(o: Any?) {
    builder.append(o)
  }

  override fun println(o: Any?) {
    builder.append(o)
    builder.append("\n")
  }

  override fun println() {
    builder.append("\n")
  }

  override fun toString() = builder.toString()
}