package marcel.io.clargs;

import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

class ReflectionUtils {
    private static final Map<Class<?>, Class<?>> primitiveToWrapper = Map.of(
        boolean.class, Boolean.class,
        byte.class, Byte.class,
        char.class, Character.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class
    );

    public static Class<?> getObjectClass(Class<?> aClass) {
        return primitiveToWrapper.getOrDefault(aClass, aClass);
    }

    @SneakyThrows
    public static Object getFieldValue(Object instance, Field field) {
        checkFieldAccess(field);
        return field.get(instance);
    }

    @SneakyThrows
    public static void setFieldValue(Field field, Object instance, Object value) {
        checkFieldAccess(field);
        field.set(instance, value);
    }

    @SneakyThrows
    public static <T> T newInstance(Class<?> clazz) {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        if (!Modifier.isPublic(constructor.getModifiers())) {
            constructor.setAccessible(true);
        }
        return (T) constructor.newInstance();
    }

    private static void checkFieldAccess(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }
}
