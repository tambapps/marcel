package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Shell
import java.io.PrintStream

class ClearBufferCommand: AbstractShellCommand() {
  override val name = "clear"
  override val shortName = "c"
  override val usage = ":clear"
  override val helpDescription = "clear the current buffer"

  override fun run(shell: Shell, args: List<String>, out: PrintStream) {
    shell.clearBuffer()
  }
}