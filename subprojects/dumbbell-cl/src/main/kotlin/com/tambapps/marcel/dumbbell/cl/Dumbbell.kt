package com.tambapps.marcel.dumbbell.cl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.maven.dependency.resolver.data.Artifact
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException
import java.io.IOException

class DumbbellCl : CliktCommand(help = "Dumbbell") {
  override fun run() {
    // just run, subcommands will be executed
  }

}

class InstallCommand(private val dumbbell: DumbbellEngine): CliktCommand(help = "Install a dumbbell (maven dependency resolved transitively)") {

  private val arguments by argument(name = "DUMBBELL...", help = "artifact string (projectId:artifactId:version)").multiple()

  override fun run() {
    for (arg in arguments) {
      val artifact = try {
        Artifact.from(arg)
      } catch (e: IllegalArgumentException) {
        print("'$arg' is not a valid artifact")
        continue
      }
      try {
        dumbbell.pull(artifact)
        println("Successfully pulled dumbbell $arg")
      } catch (e: ArtifactNotFoundException) {
        println("Artifact $arg was not found")
      } catch (e: IOException) {
        println("An unexpected error occurred: " + e.message)
      }
    }
  }
}

class UninstallCommand(private val dumbbell: DumbbellEngine): CliktCommand(help = "Uninstall a particular maven artifact") {

  private val arguments by argument(name = "DUMBBELL...", help = "artifact string (projectId:artifactId:version)").multiple()

  override fun run() {
    for (arg in arguments) {
      val artifact = try {
        Artifact.from(arg)
      } catch (e: IllegalArgumentException) {
        print("'$arg' is not a valid artifact")
        continue
      }
      dumbbell.deleteArtifact(artifact)
      println("Successfully removed dumbbell $arg")
    }
  }
}

class ListCommand(private val dumbbell: DumbbellEngine): CliktCommand(help = "List all fetched dumbbells") {

  override fun run() {
    dumbbell.allFetchedArtifacts.forEach {
      println(it)
    }
  }
}

fun main(args : Array<String>) {
  val dumbbellEngine = Dumbbell.getEngine()
  val dumbbelCl = DumbbellCl().subcommands(InstallCommand(dumbbellEngine),
    UninstallCommand(dumbbellEngine),
    ListCommand(dumbbellEngine))
  dumbbelCl.main(args)
}