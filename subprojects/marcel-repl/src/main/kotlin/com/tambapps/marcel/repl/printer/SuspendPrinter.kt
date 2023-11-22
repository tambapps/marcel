package com.tambapps.marcel.repl.printer

interface SuspendPrinter {
  suspend fun suspendPrint(s: CharSequence?)
  suspend fun suspendPrint(o: Any?) {
    suspendPrint(if (o is CharSequence) o else o.toString())
  }

  suspend fun suspendPrintln(s: CharSequence?)
  suspend fun suspendPrintln(o: Any?) {
    suspendPrintln(if (o is CharSequence) o else o.toString())
  }

  suspend fun suspendPrintln()
}