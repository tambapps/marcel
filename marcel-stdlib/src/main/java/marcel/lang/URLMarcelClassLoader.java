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
    // load the jar into the classpath
    classLoader.addURL(jarFile.toURI().toURL());

    // and then load the class
    Class<?> clazz = classLoader.loadClass(className);
    if (!Script.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException("The loaded class is not a script");
    }
    if (binding == null) {
      return (Script) clazz.getDeclaredConstructor().newInstance();
    } else {
      return (Script) clazz.getDeclaredConstructor(Binding.class).newInstance(binding);
    }
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
