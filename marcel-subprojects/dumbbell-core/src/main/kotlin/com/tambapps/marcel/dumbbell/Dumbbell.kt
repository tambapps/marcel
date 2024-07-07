package com.tambapps.marcel.dumbbell

import com.tambapps.maven.dependency.resolver.data.Artifact
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository
import java.io.File

object Dumbbell {
  private var _engine: DumbbellEngine? = null
  val engine: DumbbellEngine get() {
    if (_engine == null) {
      // note that this only works for desktops. Android dumbbell engine should be initialized externally of this class
      return DumbbellEngine(File(System.getProperty("user.home"), ".marcel/dumbbell/")).also { _engine = it }
    }
    return _engine!!
  }

  fun setEngine(engine: DumbbellEngine?) {
    Dumbbell._engine = engine
  }

  fun setEngineUsingRepository(repository: RemoteSavingMavenRepository) {
    _engine = DumbbellEngine(repository)
  }

  @Throws(DumbbellException::class)
  fun pull(artifactString: String) = engine.pull(artifactString)

  @Throws(DumbbellException::class)
  fun pull(artifact: Artifact) = engine.pull(artifact)

  @Throws(DumbbellException::class)
  fun pull(groupId: String, artifactId: String, version: String) = engine.pull(groupId, artifactId, version)

  fun isPulled(groupId: String, artifactId: String, version: String) = engine.isPulled(groupId, artifactId, version)

  fun isPulled(artifactString: String) = engine.isPulled(artifactString)

  @Throws(DumbbellException::class)
  fun enumerateDumbbells() = engine.enumerateDumbbells()

  @get:Throws(DumbbellException::class)
  val allFetchedArtifacts: List<Artifact>
    get() = engine.allFetchedArtifacts

  fun deleteAll() = engine.deleteAll()
}
