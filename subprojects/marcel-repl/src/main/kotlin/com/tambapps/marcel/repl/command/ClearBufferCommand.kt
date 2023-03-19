package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import marcel.lang.printer.Printer

class ClearBufferCommand: AbstractShellCommand() {
  override val name = "clear"
  override val shortName = "c"
  override val usage = ":clear"
  override val helpDescription = "clear the current buffer"

  override fun run(shell: MarcelShell, args: List<String>, out: Printer) {
    shell.clearBuffer()
  }
}