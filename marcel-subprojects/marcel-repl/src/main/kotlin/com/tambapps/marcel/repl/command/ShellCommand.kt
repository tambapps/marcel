package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.Printer

interface ShellCommand {
  val name: String
  val shortName: String
  val usage: String

  val helpDescription: String

  suspend fun run(shell: MarcelShell, args: List<String>, out: Printer)
  suspend fun printHelp(out: Printer)
}