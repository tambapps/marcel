package com.tambapps.marcel.dumbbell

import com.tambapps.marcel.dumbbell.storage.RemoteRepositoryStorage
import com.tambapps.maven.dependency.resolver.DependencyResolver
import com.tambapps.maven.dependency.resolver.data.Artifact
import com.tambapps.maven.dependency.resolver.exception.ArtifactNotFoundException
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import com.tambapps.maven.dependency.resolver.version.FirstVersionFoundConflictResolver
import java.io.File
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

class DumbbellEngine(val repository: RemoteSavingMavenRepository) {
  constructor(dumbbellRoot: File?) : this(RemoteSavingMavenRepository(dumbbellRoot, RemoteRepositoryStorage()))

  private companion object {
    val EXCLUDED_ARTIFACTS = setOf(
      // don't want any of kotlin dependencies as marcl and marshell should have them to be run
      Artifact("org.jetbrains.kotlin", "*", "*"),
      Artifact("com.tambapps.marcel", "marcel-stdlib", "*"),
    )
  }

  @Throws(DumbbellException::class)
  fun pull(artifactString: String): List<PulledArtifact> {
    val fields = Artifact.extractFields(artifactString)
    return pull(fields[0], fields[1], fields[2])
  }

  @Throws(DumbbellException::class)
  fun pull(artifact: Artifact): List<PulledArtifact> {
    return pull(artifact.groupId, artifact.artifactId, artifact.version)
  }

  @Throws(DumbbellException::class)
  fun deleteArtifact(artifact: Artifact?): Boolean {
    return repository.deleteArtifact(artifact)
  }

  @Throws(DumbbellException::class)
  fun pull(groupId: String, artifactId: String, version: String): List<PulledArtifact> {
    val resolver = DependencyResolver(repository)
    resolver.excludedArtifacts = EXCLUDED_ARTIFACTS.toMutableSet()
    try {
      resolver.resolve(groupId, artifactId, version)
    } catch (e: IOException) {
      throw DumbbellException(e)
    } catch (e: ArtifactNotFoundException) {
      throw DumbbellException(e)
    }
    val artifacts = resolver.results.getArtifacts(FirstVersionFoundConflictResolver())
    return artifacts.stream()
      .map { a: Artifact? ->
        PulledArtifact(
          a!!, repository.getJarFile(a)
        )
      }
      .collect(Collectors.toList())
  }

  @Throws(DumbbellException::class)
  fun enumerateDumbbells(): Map<String, Map<String, List<String>>> {
    try {
      return repository.listArtifacts()
    } catch (e: IOException) {
      throw DumbbellException(e)
    } catch (e: ArtifactNotFoundException) {
      throw DumbbellException(e)
    }
  }

  fun isPulled(groupId: String, artifactId: String, version: String): Boolean = repository.existsLocally(groupId, artifactId, version)

  fun isPulled(artifactString: String): Boolean = repository.existsLocally(artifactString)

  val allFetchedArtifacts: List<Artifact>
    get() {
      try {
        return repository.allArtifacts
      } catch (e: IOException) {
        throw DumbbellException(e)
      } catch (e: ArtifactNotFoundException) {
        throw DumbbellException(e)
      }
    }

  fun deleteAll() {
    repository.deleteAllArtifacts()
  }
}
