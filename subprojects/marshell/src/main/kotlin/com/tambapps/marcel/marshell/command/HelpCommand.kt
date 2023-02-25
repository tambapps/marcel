package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Shell
import java.io.PrintStream

class HelpCommand: AbstractShellCommand() {
  override val name = "help"
  override val shortName = "h"
  override val usage = ":help or :help command"
  override val helpDescription = "print this summary or command-specific help"

  override fun run(shell: Shell, args: List<String>, out: PrintStream) {
    if (args.isEmpty()) {
      shell.printHelp()
    } else {
      val name = args[0]
      val command = shell.findCommand(name)
      if (command != null) {
        command.printHelp(out)
      } else {
        out.println("Unknown command $name")
      }
    }
  }
}