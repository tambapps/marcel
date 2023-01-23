package marcel.lang.methods;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

public class MarcelTruth {

  public static boolean truthy(Object o) {
    if (o == null) {
      return false;
    }
    Class<?> clazz = o.getClass();
    if (clazz == Optional.class) {
      return ((Optional<?>) o).isPresent();
    } else if (o instanceof Collection) {
      return !((Collection<?>)o).isEmpty();
    } else if (clazz.isArray()) {
      return Array.getLength(o) > 0;
    } else {
      try {
        Method truthyMethod = clazz.getDeclaredMethod("truthy");
        if (truthyMethod.getReturnType() == boolean.class) {
          return (boolean) truthyMethod.invoke(o);
        } else  if (truthyMethod.getReturnType() == Boolean.class) {
          return (boolean) (Boolean) truthyMethod.invoke(o);
        } else {
          return true;
        }
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        return true;
      }
    }
  }
}
