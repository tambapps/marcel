package com.tambapps.marcel.repl.printer

import java.io.PrintStream

class PrintStreamPrinter(private val printStream: PrintStream): Printer {

  override suspend fun print(s: CharSequence?) = printStream.print(s)

  override suspend fun println(s: CharSequence?) = printStream.println(s)

  override suspend fun println() = printStream.println()
}