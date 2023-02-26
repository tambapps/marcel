package marcel.lang;

import java.io.File;
import java.net.URLClassLoader;

public class URLMarcelScriptLoader extends MarcelScriptLoader {

  private final ClassLoader parentLoader;

  public URLMarcelScriptLoader(ClassLoader parentLoader) {
    this.parentLoader = parentLoader;
  }

  public URLMarcelScriptLoader() {
    this(URLMarcelScriptLoader.class.getClassLoader());
  }

  @Override
  public Script loadScript(String className, File jarFile, Binding binding) throws ReflectiveOperationException {
    if (!jarFile.isFile()) throw new IllegalArgumentException(String.format("File %s is not a regular file", jarFile));
    // load the jar into the classpath
    URLClassLoader classLoader = new URLClassLoader(getJarUrls(jarFile), parentLoader);

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
}
