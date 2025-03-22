package marcel.io.clargs;

import java.util.Map;

class PrimitiveToWrapperUtil {
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

    public static Class<?> getWrapperClassOrSelf(Class<?> aClass) {
        return primitiveToWrapper.getOrDefault(aClass, aClass);
    }
}
