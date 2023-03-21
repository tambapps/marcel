package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.SuspendPrinter

interface ShellCommand {
  val name: String
  val shortName: String
  val usage: String

  val helpDescription: String

  suspend fun run(shell: MarcelShell, args: List<String>, out: SuspendPrinter)
  suspend fun printHelp(out: SuspendPrinter)
}