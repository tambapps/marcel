package com.tambapps.marcel.repl.command

import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.Printer

class PullDependencyCommand: AbstractShellCommand() {

  override val name = "pull"
  override val shortName = "p"
  override val usage = ":pull artifactId:groupId:version"
  override val helpDescription = "Pull a dependency"

  override suspend fun run(shell: MarcelShell, args: List<String>, out: Printer) {
    if (args.size != 1) {
      out.println("Need to provide dependency to pull")
      return
    }
    val artifactString = args[0]
    val pulledArtifacts = Dumbbell.pull(artifactString)
    out.println("Pulled dependency $artifactString")
    pulledArtifacts.forEach {
      if (it.jarFile != null) {
        shell.marcelClassLoader.addLibraryJar(it.jarFile)
      }
    }
  }

}