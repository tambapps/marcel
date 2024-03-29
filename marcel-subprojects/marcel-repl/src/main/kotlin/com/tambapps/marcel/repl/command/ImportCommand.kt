package com.tambapps.marcel.repl.command

import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.Printer

class ImportCommand: AbstractShellCommand() {

  override val name = "import"
  override val shortName = "i"
  override val usage = ":import some.class"
  override val helpDescription = "Import a/some class(es)"

  override suspend fun run(shell: MarcelShell, args: List<String>, out: Printer) {
    if (args.isEmpty()) {
      shell.listImports()
      return
    }
    val importArgs = args.joinToString(separator = " ")

    try {
      shell.addImport(importArgs)
      shell.listImports()
    } catch (e: Exception) {
      out.println("Couldn't add import: ${e.message}")
    }
  }

}