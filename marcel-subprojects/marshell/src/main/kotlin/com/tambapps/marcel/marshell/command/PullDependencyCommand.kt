package com.tambapps.marcel.marshell.command

import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.marshell.Marshell
import java.io.PrintStream

class PullDependencyCommand: AbstractShellCommand() {

  override val name = "pull"
  override val shortName = "p"
  override val usage = ":pull artifactId:groupId:version"
  override val helpDescription = "Pull a dependency"

  override fun run(shell: Marshell, args: List<String>, out: PrintStream) {
    if (args.size != 1) {
      out.println("Need to provide dependency to pull")
      return
    }
    val artifactString = args[0]
    val pulledArtifacts = Dumbbell.pull(artifactString)
    out.println("Pulled dependency $artifactString")
    pulledArtifacts.forEach {
      it.jarFile?.let(shell::addLibraryJar)
    }
  }
}