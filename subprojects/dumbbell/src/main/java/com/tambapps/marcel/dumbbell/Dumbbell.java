package com.tambapps.marcel.dumbbell;

import com.tambapps.maven.dependency.resolver.data.Artifact;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Dumbbell {

  private static DumbbellEngine engine;

  public static void setEngine(DumbbellEngine engine) {
    Dumbbell.engine = engine;
  }

  public static DumbbellEngine getEngine() {
    if (engine == null) {
      // note that this only works for desktops. Android dumbbell engine should be initialized externally of this class
      return engine = new DumbbellEngine(new File(System.getProperty("user.home"), ".marcel/dumbbell/"));
    }
    return engine;
  }

  public static List<PulledArtifact> pull(String endorsedModule) {
    return getEngine().pull(endorsedModule);
  }

  public static List<PulledArtifact> pull(Artifact artifact) {
    return getEngine().pull(artifact);
  }

  public static List<PulledArtifact> pull(String groupId, String artifactId, String version) {
    return getEngine().pull(groupId, artifactId, version);
  }

  public static Map<String, Map<String, List<String>>> enumerateDumbbells() {
    return getEngine().enumerateDumbbells();
  }

  public static List<Artifact> getAllFetchedArtifacts() {
    return getEngine().getAllFetchedArtifacts();
  }
}
