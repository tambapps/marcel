package com.tambapps.marcel.repl.printer

import java.io.PrintStream

class PrintStreamSuspendPrinter(private val printStream: PrintStream): SuspendPrinter {

  override suspend fun suspendPrint(s: CharSequence?) {
    kotlin.io.print(s)
  }

  override suspend fun suspendPrintln(s: CharSequence?) {
    kotlin.io.println(s)
  }

  override suspend fun suspendPrintln() {
    kotlin.io.println()
  }
}