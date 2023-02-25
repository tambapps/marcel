package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import java.io.PrintStream

class ExitCommand: AbstractShellCommand() {

  override val name = "exit"
  override val shortName = "e"
  override val helpDescription = "Exit the shell"
  override val usage = ":exit"

  override fun run(shell: MarcelShell, args: List<String>, out: PrintStream) {
    shell.exit()
  }
}