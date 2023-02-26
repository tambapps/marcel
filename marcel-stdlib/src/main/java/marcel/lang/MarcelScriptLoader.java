package marcel.lang;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

abstract public class MarcelScriptLoader {

  private final Set<File> libraryJars = new HashSet<>();

  public Script loadScript(String className, File jarFile) throws ReflectiveOperationException {
    return loadScript(className, jarFile, null);
  }

  public abstract Script loadScript(String className, File jarFile, Binding binding)
  throws ReflectiveOperationException;

  public boolean addLibraryJar(File jarFile) {
    return libraryJars.add(jarFile);
  }

  public boolean removeLibraryJar(File jarFile) {
    return libraryJars.remove(jarFile);
  }

  @SneakyThrows
  protected URL[] getJarUrls(File mainJarFile) {
    URL[] urls = new URL[libraryJars.size() + 1];
    urls[0] = mainJarFile.toURI().toURL();
    int i = 1;
    for (File jarFile : libraryJars) {
      urls[i++] = jarFile.toURI().toURL();
    }
    return urls;
  }
}
