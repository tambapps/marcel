package com.tambapps.marcel.dumbbell.cl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellEngine
import com.tambapps.marcel.dumbbell.storage.RemoteRepositoryStorage
import com.tambapps.maven.dependency.resolver.DependencyResolver
import com.tambapps.maven.dependency.resolver.data.Artifact
import com.tambapps.maven.dependency.resolver.exception.ArtifactNotFoundException
import com.tambapps.maven.dependency.resolver.repository.LocalMavenRepository
import com.tambapps.maven.dependency.resolver.repository.MavenRepository
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
  private val artifactStr by argument(name = "artifact", help = "The artifact to install into dumbbell repository")
  private val localOnly by option("--local", help = "Lookup for artifact in local repository only").flag()

  override fun run() {
    val (mavenRepository, fetchedArtifacts) = resolve() ?: return

    for (fetchedArtifact in fetchedArtifacts) {
      mavenRepository.retrieveArtifactPom(fetchedArtifact.toArtifactString()).use {
        dumbbell.repository.saveArtifactPom(fetchedArtifact, it)
      }
      // artifacts can be just pom sometimes
      try {
        mavenRepository.retrieveArtifactJar(fetchedArtifact.toArtifactString()).use {
          dumbbell.repository.saveArtifactJar(fetchedArtifact, it)
        }
      } catch (e: ArtifactNotFoundException) {
        // ignore it. This means it was a pom-only artifact
      }
    }
    println("Saved artifact $artifactStr with its transitive dependencies (${fetchedArtifacts.size} artifacts in total)")
  }

  private fun resolve(): Pair<MavenRepository, MutableList<Artifact>>? {
    val artifact = parseArtifact(artifactStr) ?: return null
    if (localOnly) {
      return resolveLocally(artifact)
    }
    try {
      // searching remote
      val remoteRepository = MavenRepository(RemoteRepositoryStorage())
      return remoteRepository to resolve(remoteRepository, artifact)
    } catch (e: ArtifactNotFoundException) {
      // fallbacking on local
      return resolveLocally(artifact)
    }
  }

  private fun resolveLocally(artifact: Artifact): Pair<MavenRepository, MutableList<Artifact>>? {
    try {
      val localRepository = LocalMavenRepository()
      return localRepository to resolve(localRepository, artifact)
    } catch (e: ArtifactNotFoundException) {
      println("Error: " + e.message)
      return null
    }
  }

  private fun resolve(repository: MavenRepository, artifact: Artifact): MutableList<Artifact> {
    val resolver = DependencyResolver(repository)
    resolver.resolve(artifact)
    return resolver.fetchedArtifacts
  }
}

class PullCommand(private val dumbbell: DumbbellEngine): CliktCommand(help = "Pull a artifact along its transitive dependencies and save it to local dumbbells repository") {

  private val arguments by argument(name = "DUMBBELL...", help = "artifact string (projectId:artifactId:version)").multiple()

  override fun run() {
    for (arg in arguments) {
      val artifact = parseArtifact(arg) ?: continue
      try {
        dumbbell.pull(artifact)
        println("Successfully pulled artifact $arg")
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
      println("Successfully removed artifact $arg")
    }
  }
}

class ListCommand(private val dumbbell: DumbbellEngine): CliktCommand(help = "List all fetched artifacts") {

  override fun run() {
    dumbbell.allFetchedArtifacts.forEach {
      println(it.toArtifactString())
    }
  }
}

fun main(args : Array<String>) {
  val dumbbellEngine = Dumbbell.engine
  val dumbbelCl = DumbbellCl().subcommands(PullCommand(dumbbellEngine),
    InstallCommand(dumbbellEngine),
    UninstallCommand(dumbbellEngine),
    ListCommand(dumbbellEngine))
  dumbbelCl.main(args)
}