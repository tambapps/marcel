package marcel.lang;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class URLMarcelClassLoader extends MarcelClassLoader {

  private final MarcelURLClassLoader classLoader;

  public URLMarcelClassLoader(ClassLoader parentLoader) {
    this.classLoader = new MarcelURLClassLoader(parentLoader);
  }

  @Override
  public MarcelURLClassLoader getClassLoader() {
    return classLoader;
  }

  public URLMarcelClassLoader() {
    this(URLMarcelClassLoader.class.getClassLoader());
  }

  @SneakyThrows
  @Override
  public void addLibraryJar(File jarFile) {
    classLoader.addURL(jarFile.toURI().toURL());
  }

  @Override
  public void removeLibraryJar(File jarFile) {
    // actually we can't remove URLs from URLClassLoader, so we'll have to delete the file
    jarFile.delete();
  }

  @SneakyThrows
  @Override
  public Script loadScript(String className, File jarFile, Binding binding) throws ReflectiveOperationException {
    if (!jarFile.isFile()) throw new IllegalArgumentException(String.format("File %s is not a regular file", jarFile));
    return super.loadScript(className, jarFile, binding);
  }

  // needed to make method addURL public
  private static class MarcelURLClassLoader extends URLClassLoader {

    public MarcelURLClassLoader(ClassLoader parent) {
      super(new URL[0], parent);
    }

    @Override
    public void addURL(URL url) {
      super.addURL(url);
    }
  }
}
