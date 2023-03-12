package marcel.lang;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

abstract public class MarcelClassLoader {

  private final Set<File> libraryJars = new HashSet<>();

  public Script loadScript(String className, File jarFile) throws ReflectiveOperationException {
    return loadScript(className, jarFile, null);
  }

  public Script loadScript(String className, File jarFile, Binding binding) throws ReflectiveOperationException {
    // load the jar into the classpath
    addLibraryJar(jarFile);

    // and then load the class
    Class<?> clazz = getClassLoader().loadClass(className);
    if (!Script.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException("The loaded class is not a script");
    }
    if (binding == null) {
      return (Script) clazz.getDeclaredConstructor().newInstance();
    } else {
      return (Script) clazz.getDeclaredConstructor(Binding.class).newInstance(binding);
    }
  }

  public Class<?> loadClass(String name) throws ClassNotFoundException {
    return getClassLoader().loadClass(name);
  }

  abstract public void addLibraryJar(File jarFile);

  abstract public void removeLibraryJar(File jarFile);

  abstract public ClassLoader getClassLoader();

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
