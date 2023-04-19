package com.tambapps.marcel.android.marshell.ui.shell

import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.printer.Printer
import java.util.function.Supplier

class SuppliedShellPrinter(private val printerSupplier: Supplier<TextViewPrinter?>): SuspendPrinter, Printer {
  override suspend fun suspendPrint(s: CharSequence?) {
    printerSupplier.get()?.suspendPrint(s)
  }

  override suspend fun suspendPrintln() {
    printerSupplier.get()?.println()
  }

  override suspend fun suspendPrintln(s: CharSequence?) {
    printerSupplier.get()?.suspendPrintln(s)
  }

  override fun print(p0: CharSequence?) {
    printerSupplier.get()?.print(p0)
  }

  override fun println(p0: CharSequence?) {
    printerSupplier.get()?.println(p0)
  }

  override fun println() {
    printerSupplier.get()?.println()
  }
}