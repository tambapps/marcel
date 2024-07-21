package marcel.lang;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

public class MarcelDexClassLoader extends MarcelClassLoader {

    private final ClassLoader parentClassLoader = MarcelDexClassLoader.class.getClassLoader();
    private final Set<String> dexPathList = ConcurrentHashMap.newKeySet();
    private DexClassLoader classLoader = newClassLoader();

    @Override
    public void addJars(Collection<File> jarFiles) {
        for (File file : jarFiles) {
            dexPathList.add(file.getAbsolutePath());
        }
        updateClassLoader();
    }

    @Override
    public void addJar(File file) {
        dexPathList.add(file.getAbsolutePath());
        updateClassLoader();
    }

    @Override
    public boolean removeJar(File file) {
        dexPathList.remove(file.getAbsolutePath());
        updateClassLoader();
        return file.delete();
    }

    @Override
    public void removeJars(Collection<File> jarFiles) {
        for (File file : jarFiles) {
            dexPathList.remove(file.getAbsolutePath());
        }
        updateClassLoader();
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    private void updateClassLoader() {
        this.classLoader = null;
        System.gc(); // making sure the previous classLoader instance is disposed
        this.classLoader = newClassLoader();
    }

    private DexClassLoader newClassLoader() {
        return new DexClassLoader(
            String.join(File.pathSeparator, dexPathList), null, null, parentClassLoader);
    }
}
