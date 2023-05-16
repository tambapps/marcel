package com.tambapps.marcel.dumbbell.cl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.maven.dependency.resolver.DependencyResolver
import com.tambapps.maven.dependency.resolver.data.Artifact
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException
import com.tambapps.maven.dependency.resolver.repository.LocalMavenRepository
import java.io.File
import java.io.IOException

fun parseArtifact(s: String): Artifact? {
  return try {
    Artifact.from(s)
  } catch (e: IllegalArgumentException) {
    print("'$s' is not a valid artifact")
    return null
  }
}
class DumbbellCl : CliktCommand(name = "dumbbell", help = "Dumbbell") {
  override fun run() {
    // just run, subcommands will be executed
  }

}
class InstallCommand(private val dumbbell: DumbbellEngine): CliktCommand(help = "Install a particular dumbbell using the provided jar") {
  private val fromLocal by option("--from-local", help = "Copy a dumbbell (along with its transitive dependencies) from Maven repository to dumbbell").required()

  override fun run() {
    val mavenRepository = LocalMavenRepository()
    val resolver = DependencyResolver(mavenRepository)
    val artifact = parseArtifact(fromLocal) ?: return
    resolver.resolve(artifact)
    val fetchedArtifacts = resolver.fetchedArtifacts

    for (fetchedArtifact in fetchedArtifacts) {
      val pomFile = mavenRepository.getPomFile(fetchedArtifact)
      val jarFile: File? = mavenRepository.getJarFile(fetchedArtifact)
      pomFile.inputStream().use {
        dumbbell.repository.saveArtifactPom(fetchedArtifact, it)
      }
      // artifacts can be just pom sometimes
      jarFile?.inputStream()?.use {
        dumbbell.repository.saveArtifactJar(fetchedArtifact, it)
      }
    }
    println("Saved dumbbell $fromLocal with its transitive dependencies (${fetchedArtifacts.size} artifacts in total)")
  }
}

class PullCommand(private val dumbbell: DumbbellEngine): CliktCommand(help = "Pull a dumbbell (maven dependency resolved transitively) and save it to local dumbbells repository") {

  private val arguments by argument(name = "DUMBBELL...", help = "artifact string (projectId:artifactId:version)").multiple()

  override fun run() {
    for (arg in arguments) {
      val artifact = parseArtifact(arg) ?: continue
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
      val artifact = parseArtifact(arg) ?: continue
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
  val dumbbelCl = DumbbellCl().subcommands(PullCommand(dumbbellEngine),
    InstallCommand(dumbbellEngine),
    UninstallCommand(dumbbellEngine),
    ListCommand(dumbbellEngine))
  dumbbelCl.main(args)
}