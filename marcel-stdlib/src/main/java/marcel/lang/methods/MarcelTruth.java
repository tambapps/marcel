package marcel.lang.methods;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.regex.Matcher;

public class MarcelTruth {

  public static boolean truthy(Object o) {
    if (o == null) {
      return false;
    }
    Class<?> clazz = o.getClass();
    if (clazz == Optional.class) {
      return truthy((Optional<?>) o);
    } else if (clazz == OptionalInt.class) {
      return truthy((OptionalInt) o);
    } else if (clazz == OptionalLong.class) {
      return truthy((OptionalLong) o);
    } else if (clazz == OptionalDouble.class) {
      return truthy((OptionalDouble) o);
    } else if (o instanceof Collection) {
      return truthy((Collection<?>) o);
    } else if (clazz.isArray()) {
      return Array.getLength(o) > 0;
    } else if (clazz == Matcher.class) {
      return truthy((Matcher) o);
    } else {
      return true;
    }
  }

  public static boolean truthy(Matcher matcher) {
    return matcher.find();
  }

  public static boolean truthy(Collection<?> collection) {
    return !collection.isEmpty();
  }

  public static boolean truthy(Map<?, ?> map) {
    return !map.isEmpty();
  }

  public static boolean truthy(Optional<?> optional) {
    return optional.isPresent();
  }
  public static boolean truthy(OptionalInt optional) {
    return optional.isPresent();
  }
  public static boolean truthy(OptionalDouble optional) {
    return optional.isPresent();
  }
  public static boolean truthy(OptionalLong optional) {
    return optional.isPresent();
  }
  public static boolean truthy(int value) {
    return true;
  }
  public static boolean truthy(long value) {
    return true;
  }
  public static boolean truthy(float value) {
    return true;
  }
  public static boolean truthy(double value) {
    return true;
  }
  public static boolean truthy(char value) {
    return true;
  }
  public static boolean truthy(short value) {
    return true;
  }
  public static boolean truthy(byte value) {
    return true;
  }
  public static boolean truthy(boolean value) {
    return true;
  }
}
