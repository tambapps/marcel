package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Shell
import java.io.PrintStream

class ListCommand: AbstractShellCommand() {

  override val name = "list"
  override val shortName = "l"
  override val usage = ":list or :list (Variables, Functions, Classes)"
  override val helpDescription = "list defined members"


  override fun run(shell: Shell, args: List<String>, out: PrintStream) {
    TODO("Not yet implemented")
  }
}