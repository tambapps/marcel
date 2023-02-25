package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Shell
import java.io.PrintStream

class ExitCommand: AbstractShellCommand() {

  override val name = "exit"
  override val shortName = "e"
  override val helpDescription = "Exit the shell"
  override val usage = ":exit"

  override fun run(shell: Shell, args: List<String>, out: PrintStream) {
    shell.exit()
  }
}