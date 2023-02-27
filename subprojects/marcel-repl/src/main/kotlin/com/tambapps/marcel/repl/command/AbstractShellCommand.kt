package com.tambapps.marcel.repl.command

import java.io.PrintStream

abstract class AbstractShellCommand: ShellCommand {

  override fun printHelp(out: PrintStream) {
    out.println("$usage\t\t(:$shortName) $helpDescription")
  }
}