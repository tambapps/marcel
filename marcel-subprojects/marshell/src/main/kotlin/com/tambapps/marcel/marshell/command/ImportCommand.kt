package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.marshell.Marshell
import java.io.PrintStream

class ImportCommand: AbstractShellCommand() {

  override val name = "import"
  override val shortName = "i"
  override val usage = ":import some.class"
  override val helpDescription = "Import a/some class(es)"

  override fun run(shell: Marshell, args: List<String>, out: PrintStream) {
    if (args.isEmpty()) {
      shell.listImports()
      return
    }
    val importArgs = args.joinToString(separator = " ")

    try {
      shell.evaluator.addImport(importArgs)
      shell.listImports()
    } catch (e: Exception) {
      out.println("Couldn't add import: ${e.message}")
    }
  }

}