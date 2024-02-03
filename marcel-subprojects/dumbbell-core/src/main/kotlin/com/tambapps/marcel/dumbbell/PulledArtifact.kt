package com.tambapps.marcel.dumbbell

import com.tambapps.maven.dependency.resolver.data.Artifact
import java.io.File
import java.util.*

class PulledArtifact(artifact: Artifact, val jarFile: File?) :
  Artifact(artifact.groupId, artifact.artifactId, artifact.version) {
  override fun toString(): String {
    return "PulledArtifact(artifact=" + toArtifactString() + ", jarFile=" + jarFile + ")"
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o !is PulledArtifact) return false
    if (!super.equals(o)) return false
    return jarFile == o.jarFile
  }

  override fun hashCode(): Int {
    return Objects.hash(super.hashCode(), jarFile)
  }
}
