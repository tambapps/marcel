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
  fun pull(endorsedModule: String): List<PulledArtifact> {
    return engine.pull(endorsedModule)
  }

  @Throws(DumbbellException::class)
  fun pull(artifact: Artifact): List<PulledArtifact> {
    return engine.pull(artifact)
  }

  @Throws(DumbbellException::class)
  fun pull(groupId: String, artifactId: String, version: String): List<PulledArtifact> {
    return engine.pull(groupId, artifactId, version)
  }

  @Throws(DumbbellException::class)
  fun enumerateDumbbells(): Map<String, Map<String, List<String>>> {
    return engine.enumerateDumbbells()
  }

  @get:Throws(DumbbellException::class)
  val allFetchedArtifacts: List<Artifact>
    get() = engine.allFetchedArtifacts

  fun deleteAll() = engine.deleteAll()
}
