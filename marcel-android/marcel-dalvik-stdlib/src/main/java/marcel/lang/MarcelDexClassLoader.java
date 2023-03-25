package marcel.lang;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

public class MarcelDexClassLoader extends MarcelClassLoader {

    private static final Class DEX_PATH_ELEMENT_CLASS;

    static {
        try {
            DEX_PATH_ELEMENT_CLASS = Class.forName("dalvik.system.DexPathList$Element");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot load dalvik class. Is dalvik library in classpath?");
        }
    }

    public final BaseDexClassLoader classLoader;

    public MarcelDexClassLoader() {
        ClassLoader classLoader = MarcelDexClassLoader.class.getClassLoader();
        if (!(classLoader instanceof BaseDexClassLoader)) {
            throw new UnsupportedOperationException("Class loader not supported");
        }
        this.classLoader = (BaseDexClassLoader) classLoader;
    }

    @Override
    public void addLibraryJar(File file) {
        // can also be a directory, or a dex class
        Object existing = getDexClassLoaderElements();
        if (containsDexPath(existing, file.getAbsolutePath())) {
            return;
        }
        BaseDexClassLoader newClassLoader = new DexClassLoader(file.getAbsolutePath(), null, null, classLoader);
        Object incoming = getDexClassLoaderElements(newClassLoader);
        Object joined = joinArrays(incoming, existing);
        setDexClassLoaderElements(joined);
    }

    @Override
    public void removeLibraryJar(File file) {
        if (isBaseApk(file.getAbsolutePath())) {
            throw new IllegalArgumentException("Cannot remove base apk from classpath");
        }
        Object dexElements = getDexClassLoaderElements();
        List<Object> remainingDexElements = new ArrayList<>();
        String dexFilePath = file.getAbsolutePath();
        for (Object dexElement : collect(dexElements)) {
            if (!dexFilePath.equals(getDexElementPath(dexElement))) {
                remainingDexElements.add(dexElement);
            }
        }
        if (Array.getLength(dexElements) == remainingDexElements.size()) {
            return;
        }
        Object remainingDexElementsArray = Array.newInstance(DEX_PATH_ELEMENT_CLASS, remainingDexElements.size());
        for (int i = 0; i < remainingDexElements.size(); i++) {
            Array.set(remainingDexElementsArray, i, remainingDexElements.get(i));
        }
        setDexClassLoaderElements(remainingDexElementsArray);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public List<String> getClassPath() {
        Collection dexElements = collect(getDexClassLoaderElements());
        List<String> result = new ArrayList<>(dexElements.size());
        for (Object dexElement : dexElements) {
            String path = getDexElementPath(dexElement);
            if (!isBaseApk(path)) {
                result.add(path);
            }
        }
        return Collections.unmodifiableList(result);
    }



    private void setDexClassLoaderElements(Object elements) {
        try {
            Class<BaseDexClassLoader> dexClassLoaderClass = BaseDexClassLoader.class;
            Field pathListField = dexClassLoaderClass.getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(classLoader);
            Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            dexElementsField.set(pathList, elements);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getDexClassLoaderElements() {
        return getDexClassLoaderElements(classLoader);
    }

    private static Object getDexClassLoaderElements(BaseDexClassLoader classLoader) {
       try {
           Class<BaseDexClassLoader> dexClassLoaderClass = BaseDexClassLoader.class;
           Field pathListField = dexClassLoaderClass.getDeclaredField("pathList");
           pathListField.setAccessible(true);
           Object pathList = pathListField.get(classLoader);
           Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
           dexElementsField.setAccessible(true);
           Object dexElements = dexElementsField.get(pathList);
           return dexElements;
       } catch (ReflectiveOperationException e) {
           throw new RuntimeException(e);
       }
    }

    private static Object joinArrays(Object o1, Object o2) {
        Class<?> o1Type = o1.getClass().getComponentType();
        Class<?> o2Type = o2.getClass().getComponentType();

        if (o1Type != o2Type)
            throw new IllegalArgumentException();

        int o1Size = Array.getLength(o1);
        int o2Size = Array.getLength(o2);
        Object array = Array.newInstance(o1Type, o1Size + o2Size);

        int offset = 0, i;
        for (i = 0; i < o1Size; i++, offset++)
            Array.set(array, offset, Array.get(o1, i));
        for (i = 0; i < o2Size; i++, offset++)
            Array.set(array, offset, Array.get(o2, i));

        return array;
    }

    private boolean containsDexPath(Object existing, String fileName) {
        try {
            Collection dexElements = collect(existing);
            // class is DexPathList$Element
            // we are interested in field String dexFile which is the path of the file
            Field dexFileField = DEX_PATH_ELEMENT_CLASS.getDeclaredField("dexFile");
            dexFileField.setAccessible(true);

            for (Object o : dexElements) {
                // the toString method of a DexFile seems to return its path
                String dexFilePath = getDexElementPath(o);
                if (fileName.equals(dexFilePath)) {
                    return true;
                }
            }
            return false;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDexElementPath(Object o) {
      try {
          Field dexFileField = DEX_PATH_ELEMENT_CLASS.getDeclaredField("path");
          if (!dexFileField.isAccessible()) {
              dexFileField.setAccessible(true);
          }
          return String.valueOf(dexFileField.get(o));
      } catch (ReflectiveOperationException e) {
          throw new RuntimeException(e);
      }
    }

    private static boolean isBaseApk(String path) {
        return path.contains("com.tambapps.android.grooidshell") && path.endsWith("/base.apk");
    }

    private static Collection<?> collect(Object array) {
        int size = Array.getLength(array);
        List<Object> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }
}
