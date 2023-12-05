package marcel.lang;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.regex.Matcher;

/**
 * The truth specifying if a value is considered to be true or not by Marcel Lang
 */
public interface MarcelTruth {

  /**
   * Returns whether the object should be considered truthy or not
   *
   * @return whether the object should be considered truthy or not
   */
  boolean isTruthy();

  public static boolean isTruthy(Object o) {
    if (o == null) {
      return false;
    }
    Class<?> clazz = o.getClass();
    if (clazz == String.class) {
      return isTruthy((String) o);
    } else if (clazz == Boolean.class) {
      return isTruthy((Boolean) o);
    } else if (clazz == Optional.class) {
      return isTruthy((Optional<?>) o);
    } else if (clazz == OptionalInt.class) {
      return isTruthy((OptionalInt) o);
    } else if (clazz == OptionalLong.class) {
      return isTruthy((OptionalLong) o);
    } else if (clazz == OptionalDouble.class) {
      return isTruthy((OptionalDouble) o);
    } else if (o instanceof Collection) {
      return isTruthy((Collection<?>) o);
    } else if (clazz.isArray()) {
      return Array.getLength(o) > 0;
    } else if (clazz == Matcher.class) {
      return isTruthy((Matcher) o);
    } else if (o instanceof MarcelTruth) {
      return isTruthy((MarcelTruth) o);
    } else {
      return true;
    }
  }

  public static boolean isTruthy(String s) {
    return !s.isEmpty();
  }

  public static boolean isTruthy(Boolean b) {
    return b != null && b;
  }

  public static boolean isTruthy(MarcelTruth object) {
    return object != null && object.isTruthy();
  }

  public static boolean isTruthy(Matcher matcher) {
    return matcher != null && matcher.find();
  }

  public static boolean isTruthy(Collection<?> collection) {
    return collection != null && !collection.isEmpty();
  }

  public static boolean isTruthy(Map<?, ?> map) {
    return map != null && !map.isEmpty();
  }

  public static boolean isTruthy(Optional<?> optional) {
    return optional != null && optional.isPresent();
  }
  public static boolean isTruthy(OptionalInt optional) {
    return optional != null && optional.isPresent();
  }
  public static boolean isTruthy(OptionalDouble optional) {
    return optional != null && optional.isPresent();
  }
  public static boolean isTruthy(OptionalLong optional) {
    return optional != null && optional.isPresent();
  }
  public static boolean isTruthy(int value) {
    return true;
  }
  public static boolean isTruthy(long value) {
    return true;
  }
  public static boolean isTruthy(float value) {
    return true;
  }
  public static boolean isTruthy(double value) {
    return true;
  }
  public static boolean isTruthy(char value) {
    return true;
  }
  public static boolean isTruthy(short value) {
    return true;
  }
  public static boolean isTruthy(byte value) {
    return true;
  }
  public static boolean isTruthy(boolean value) {
    return value;
  }
}
