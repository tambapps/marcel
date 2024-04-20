package com.tambapps.marcel.marshell.command

import java.io.PrintStream

abstract class AbstractShellCommand: ShellCommand {

  override fun printHelp(out: PrintStream) {
    out.println("$usage\t\t(:$shortName) $helpDescription")
  }
}