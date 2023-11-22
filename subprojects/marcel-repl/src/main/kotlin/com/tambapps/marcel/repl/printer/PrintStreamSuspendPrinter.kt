package com.tambapps.marcel.repl.printer

import java.io.PrintStream

class PrintStreamSuspendPrinter(private val printStream: PrintStream): SuspendPrinter {

  override suspend fun suspendPrint(s: CharSequence?) {
    printStream.print(s)
  }

  override suspend fun suspendPrintln(s: CharSequence?) {
    printStream.println(s)
  }

  override suspend fun suspendPrintln() {
    printStream.println()
  }
}