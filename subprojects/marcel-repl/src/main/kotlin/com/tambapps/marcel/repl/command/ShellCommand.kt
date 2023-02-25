package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import java.io.PrintStream

interface ShellCommand {
  val name: String
  val shortName: String
  val usage: String

  val helpDescription: String

  fun run(shell: MarcelShell, args: List<String>, out: PrintStream)
  fun printHelp(out: PrintStream)
}