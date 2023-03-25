package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.command.AbstractShellCommand
import com.tambapps.marcel.repl.printer.SuspendPrinter

class ExitCommand: AbstractShellCommand() {

  override val name = "exit"
  override val shortName = "e"
  override val helpDescription = "Exit the shell"
  override val usage = ":exit"

  override suspend fun run(shell: MarcelShell, args: List<String>, out: SuspendPrinter) {
    shell.exit()
  }
}