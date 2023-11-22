package com.tambapps.marcel.repl.command

import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.SuspendPrinter

class PullDependencyCommand: AbstractShellCommand() {

  override val name = "pull"
  override val shortName = "p"
  override val usage = ":pull artifactId:groupId:version"
  override val helpDescription = "Pull a dependency"

  override suspend fun run(shell: MarcelShell, args: List<String>, out: SuspendPrinter) {
    if (args.size != 1) {
      out.suspendPrintln("Need to provide dependency to pull")
      return
    }
    val artifactString = args[0]
    val pulledArtifacts = Dumbbell.pull(artifactString)
    out.suspendPrintln("Pulled dependency $artifactString")
    pulledArtifacts.forEach {
      if (it.jarFile != null) {
        shell.marcelClassLoader.addLibraryJar(it.jarFile)
      }
    }
  }

}