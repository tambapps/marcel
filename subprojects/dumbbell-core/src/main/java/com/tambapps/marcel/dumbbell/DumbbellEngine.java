package com.tambapps.marcel.dumbbell;

import com.tambapps.maven.dependency.resolver.DependencyResolver;
import com.tambapps.maven.dependency.resolver.data.Artifact;
import com.tambapps.maven.dependency.resolver.exceptions.ArtifactNotFoundException;
import com.tambapps.maven.dependency.resolver.repository.RemoteSavingMavenRepository;
import com.tambapps.maven.dependency.resolver.version.FirstVersionFoundConflictResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DumbbellEngine {

  @Getter
  private final RemoteSavingMavenRepository repository;

  public DumbbellEngine(File dumbbellRoot) {
    // the LocalMavenRepository will take care of creating a subdirectory "repository"
    this(new RemoteSavingMavenRepository(dumbbellRoot));
  }

  public List<PulledArtifact> pull(String endorsedModule) throws DumbbellException {
    String[] fields = Artifact.extractFields(endorsedModule);
    return pull(fields[0], fields[1], fields[2]);
  }

  public List<PulledArtifact> pull(Artifact artifact) throws DumbbellException {
    return pull(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  public boolean deleteArtifact(Artifact artifact) throws DumbbellException {
    return repository.deleteArtifact(artifact);
  }

  public List<PulledArtifact> pull(String groupId, String artifactId, String version) throws DumbbellException {
    DependencyResolver resolver = new DependencyResolver(repository);
    resolver.setExcludedArtifacts(new HashSet<>(Arrays.asList(
        new Artifact("org.jetbrains.kotlin", "kotlin-stdlib", "*"),
        new Artifact("org.jetbrains.kotlin", "kotlin-stdlib-common", "*")
    )));
    try {
      resolver.resolve(groupId, artifactId, version);
    } catch (IOException | ArtifactNotFoundException e) {
      throw new DumbbellException(e);
    }
    List<Artifact> artifacts = resolver.getResults().getArtifacts(new FirstVersionFoundConflictResolver());
    return artifacts.stream()
        .map(a -> new PulledArtifact(a, repository.getJarFile(a)))
        .collect(Collectors.toList());
  }
  
  public Map<String, Map<String, List<String>>> enumerateDumbbells() throws DumbbellException {
    try {
      return repository.listArtifacts();
    } catch (IOException | ArtifactNotFoundException e) {
      throw new DumbbellException(e);
    }
  }

  public List<Artifact> getAllFetchedArtifacts() {
    try {
      return repository.getAllArtifacts();
    } catch (IOException | ArtifactNotFoundException e) {
      throw new DumbbellException(e);
    }
  }

  public void deleteAll() {
    repository.deleteAllArtifacts();
  }
}
