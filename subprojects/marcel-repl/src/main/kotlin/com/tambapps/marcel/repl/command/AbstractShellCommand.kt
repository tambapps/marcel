package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.printer.SuspendPrinter

abstract class AbstractShellCommand: ShellCommand {

  override suspend fun printHelp(out: SuspendPrinter) {
    out.suspendPrintln("$usage\t\t(:$shortName) $helpDescription")
  }
}