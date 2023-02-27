package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import java.io.PrintStream

class ImportCommand: AbstractShellCommand() {

  override val name = "import"
  override val shortName = "i"
  override val usage = ":import some.class"
  override val helpDescription = "Import a/some class(es) (wildcards and 'as' imports also work)"

  override fun run(shell: MarcelShell, args: List<String>, out: PrintStream) {
    TODO("Implement import command")
  }
}