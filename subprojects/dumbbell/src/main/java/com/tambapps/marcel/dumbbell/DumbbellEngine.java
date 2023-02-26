package com.tambapps.marcel.dumbbell;

import com.tambapps.maven.dependency.resolver.DependencyResolver;
import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository;
import com.tambapps.maven.dependency.resolver.version.FirstVersionFoundConflictResolver;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DumbbellEngine {

  private final RemoteSavingMavenRepository repository;

  public DumbbellEngine(File rootDirectory) {
    if (!rootDirectory.isDirectory() && !rootDirectory.mkdir()) {
      throw new RuntimeException("Couldn't create dumbbell directory");
    }
    File repositoryRoot = new File(rootDirectory, "repository");
    if (!repositoryRoot.isDirectory() && !repositoryRoot.mkdir()) {
      throw new RuntimeException("Couldn't create dumbbell repository directory");
    }
    this.repository = new RemoteSavingMavenRepository(repositoryRoot);
  }

  public List<Artifact> pull(String endorsedModule) {
    String[] fields = Artifact.extractFields(endorsedModule);
    return pull(fields[0], fields[1], fields[2]);
  }

  public List<Artifact> pull(Artifact artifact) {
    return pull(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  @SneakyThrows
  public List<Artifact> pull(String groupId, String artifactId, String version) {
    DependencyResolver resolver = new DependencyResolver(repository);
    resolver.setExcludedArtifacts(new HashSet<>(Arrays.asList(
        new Artifact("com.squareup.okhttp3", "okhttp", "*"),
        new Artifact("com.squareup.okio", "okio", "*"),
        new Artifact("org.jsoup", "jsoup", "*"),
        new Artifact("org.jetbrains.kotlin", "kotlin-stdlib", "*"),
        new Artifact("org.jetbrains.kotlin", "kotlin-stdlib-common", "*")
    )));
    resolver.resolve(groupId, artifactId, version);
    return resolver.getResults().getArtifacts(new FirstVersionFoundConflictResolver());
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
