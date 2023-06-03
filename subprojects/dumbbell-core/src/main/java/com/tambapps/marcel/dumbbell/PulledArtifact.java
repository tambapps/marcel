package com.tambapps.marcel.dumbbell;

import com.tambapps.maven.dependency.resolver.data.Artifact;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Value
public class PulledArtifact extends Artifact {

  File jarFile;

  public PulledArtifact(Artifact artifact, File jarFile) {
    super(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
    this.jarFile = jarFile;
  }

  @Override
  public String toString() {
    return "PulledArtifact(artifact=" + toArtifactString() + ", jarFile=" + jarFile + ")";
  }
}
