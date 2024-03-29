package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.Printer

class ExitCommand: AbstractShellCommand() {

  override val name = "exit"
  override val shortName = "e"
  override val helpDescription = "Exit the shell"
  override val usage = ":exit"

  override suspend fun run(shell: MarcelShell, args: List<String>, out: Printer) {
    shell.exit()
  }
}