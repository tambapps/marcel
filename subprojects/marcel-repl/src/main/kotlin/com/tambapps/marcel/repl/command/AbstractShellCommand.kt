package com.tambapps.marcel.repl.command

import marcel.lang.printer.Printer

abstract class AbstractShellCommand: ShellCommand {

  override fun printHelp(out: Printer) {
    out.println("$usage\t\t(:$shortName) $helpDescription")
  }
}