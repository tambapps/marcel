package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import marcel.lang.printer.Printer

class HelpCommand: AbstractShellCommand() {
  override val name = "help"
  override val shortName = "h"
  override val usage = ":help or :help [commandName]"
  override val helpDescription = "print this summary or command-specific help"

  override fun run(shell: MarcelShell, args: List<String>, out: Printer) {
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