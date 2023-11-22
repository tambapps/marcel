package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.SuspendPrinter

class ClearBufferCommand: AbstractShellCommand() {
  override val name = "clear"
  override val shortName = "c"
  override val usage = ":clear"
  override val helpDescription = "clear the current buffer"

  override suspend fun run(shell: MarcelShell, args: List<String>, out: SuspendPrinter) {
    shell.clearBuffer()
  }
}