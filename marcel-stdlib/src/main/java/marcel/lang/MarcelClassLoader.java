package marcel.lang;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Collection;

@NullMarked
abstract public class MarcelClassLoader {

  public Script loadScript(String className, File jarFile) throws ReflectiveOperationException {
    return loadScript(className, jarFile, null);
  }

  /**
   * Add the provided jar to the classpath, load the script class with the provided className and returns an instance of it.
   * The caller of this method should
   * be responsible for removing the jar from the classpath when needed
   * @param className the class name
   * @param jarFile the jar file
   * @param binding the binding
   * @return an instance of the script class loaded
   * @throws ReflectiveOperationException in case a reflective error occurs when loading the script
   */
  public Script loadScript(String className, File jarFile, @Nullable Binding binding) throws ReflectiveOperationException {
    // load the jar into the classpath
    addJar(jarFile);

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

  /**
   * Adds the jar to the classpath
   * @param jarFile the jar to add
   */
  public abstract void addJar(File jarFile);

  public void addJars(Collection<File> jarFiles) {
    for (File file : jarFiles) {
      addJar(file);
    }
  }

  /**
   * Remove the jar from the classpath. Note that this method might delete the file
   * @param jarFile the jar to remove from the classpath
   */
  public abstract boolean removeJar(File jarFile);

  public void removeJars(Collection<File> jarFiles) {
    for (File file : jarFiles) {
      removeJar(file);
    }
  }

  public abstract ClassLoader getClassLoader();

}
