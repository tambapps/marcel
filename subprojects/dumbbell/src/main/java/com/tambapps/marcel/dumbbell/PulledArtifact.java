package com.tambapps.marcel.dumbbell;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import lombok.Value;

import java.io.File;

@Value
public class PulledArtifact extends Artifact {

  File jarFile;

  public PulledArtifact(Artifact artifact, File jarFile) {
    super(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    this.jarFile = jarFile;
  }
}
