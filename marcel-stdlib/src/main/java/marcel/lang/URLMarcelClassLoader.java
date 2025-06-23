package marcel.lang;

import lombok.SneakyThrows;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

@NullMarked
public class URLMarcelClassLoader extends MarcelClassLoader {

  private final JavaURLClassLoader classLoader;

  public URLMarcelClassLoader(ClassLoader parentLoader) {
    this.classLoader = new JavaURLClassLoader(parentLoader);
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public URLMarcelClassLoader() {
    this(URLMarcelClassLoader.class.getClassLoader());
  }

  @SneakyThrows
  @Override
  public void addJar(File jarFile) {
    addJar(jarFile.toURI().toURL());
  }

  public void addJar(URL jarUrl) {
    classLoader.addURL(jarUrl);
  }

  @Override
  public boolean removeJar(File jarFile) {
    // actually we can't remove URLs from URLClassLoader, so we'll have to delete the file
    return jarFile.delete();
  }

  @SneakyThrows
  public void removeJar(URL jarUrl) {
    removeJar(Paths.get(jarUrl.toURI()).toFile());
  }

  @SneakyThrows
  @Override
  public Script loadScript(String className, File jarFile, @Nullable Binding binding) throws ReflectiveOperationException {
    if (!jarFile.isFile()) throw new IllegalArgumentException(String.format("File %s is not a regular file", jarFile));
    return super.loadScript(className, jarFile, binding);
  }

  // needed to make method addURL public
  private static class JavaURLClassLoader extends URLClassLoader {

    public JavaURLClassLoader(ClassLoader parent) {
      super(new URL[0], parent);
    }

    @Override
    public void addURL(URL url) {
      super.addURL(url);
    }
  }
}
