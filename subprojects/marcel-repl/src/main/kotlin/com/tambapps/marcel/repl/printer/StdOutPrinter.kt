package com.tambapps.marcel.repl.printer

import java.io.PrintStream

class StdOutPrinter(private val out: PrintStream): Printer {
  constructor(): this(System.out)

  override fun print(s: String) {
    out.println(s)
  }

  override fun println(s: String) {
    out.println(s)
  }

  override fun println(o: Any) {
    out.println(o)
  }

  override fun println() {
    out.println()
  }
}