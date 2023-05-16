package com.tambapps.marcel.dumbbell;

import com.tambapps.maven.dependency.resolver.DependencyResolver;
import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository;
import com.tambapps.maven.dependency.resolver.version.FirstVersionFoundConflictResolver;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DumbbellEngine {

  @Getter
  private final RemoteSavingMavenRepository repository;

  public DumbbellEngine(File dumbbellRoot) {
    // the LocalMavenRepository will take care of creating a subdirectory "repository"
    this.repository = new RemoteSavingMavenRepository(dumbbellRoot);
  }

  public List<PulledArtifact> pull(String endorsedModule) {
    String[] fields = Artifact.extractFields(endorsedModule);
    return pull(fields[0], fields[1], fields[2]);
  }

  public List<PulledArtifact> pull(Artifact artifact) {
    return pull(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  public boolean deleteArtifact(Artifact artifact) {
    return repository.deleteArtifact(artifact);
  }

  @SneakyThrows
  public List<PulledArtifact> pull(String groupId, String artifactId, String version) {
    DependencyResolver resolver = new DependencyResolver(repository);
    resolver.setExcludedArtifacts(new HashSet<>(Arrays.asList(
        new Artifact("com.squareup.okhttp3", "okhttp", "*"),
        new Artifact("com.squareup.okio", "okio", "*"),
        new Artifact("org.jsoup", "jsoup", "*"),
        new Artifact("org.jetbrains.kotlin", "kotlin-stdlib", "*"),
        new Artifact("org.jetbrains.kotlin", "kotlin-stdlib-common", "*")
    )));
    resolver.resolve(groupId, artifactId, version);
    List<Artifact> artifacts = resolver.getResults().getArtifacts(new FirstVersionFoundConflictResolver());
    return artifacts.stream()
        .map(a -> new PulledArtifact(a, repository.getJarFile(a)))
        .collect(Collectors.toList());
  }
  
  public Map<String, Map<String, List<String>>> enumerateDumbbells() {
    try {
      return repository.listArtifacts();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Artifact> getAllFetchedArtifacts() {
    try {
      return repository.getAllArtifacts();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
