package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.printer.Printer

abstract class AbstractShellCommand: ShellCommand {

  override suspend fun printHelp(out: Printer) {
    out.println("$usage\t\t(:$shortName) $helpDescription")
  }
}