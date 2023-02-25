package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Shell
import java.io.PrintStream

interface ShellCommand {
  val name: String
  val shortName: String
  val usage: String

  val helpDescription: String

  fun run(shell: Shell, args: List<String>, out: PrintStream)
  fun printHelp(out: PrintStream)
}