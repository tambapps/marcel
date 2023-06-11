package marcel.lang.dynamic;

import lombok.SneakyThrows;
import marcel.lang.DynamicObject;
import marcel.lang.NoSuchPropertyException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// TODO document that extensions are not handled by dynamic objects
public abstract class AbstractDynamicObject implements DynamicObject {

  @Override
  public String toString() {
    return "dynamic " + getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AbstractDynamicObject)) return false;
    AbstractDynamicObject that = (AbstractDynamicObject) o;
    return Objects.equals(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    return getValue().hashCode();
  }

  protected Object getRealValue(Object o) {
    return o instanceof DynamicObject ? ((DynamicObject) o).getValue() : o;
  }

  @SneakyThrows
  @Override
  public DynamicObject getProperty(String name) {
    String getterName = "get" +
        Character.toUpperCase(name.charAt(0)) +
        name.substring(1);
    try {
      Method method = findMethod(getValue().getClass(), getterName, new Object[0]);
      return DynamicObject.of(method.invoke(getValue()));
    } catch (MissingMethodException e) {
      // try searching in fields
      try {
        Field field = getValue().getClass().getDeclaredField(name);
        return DynamicObject.of(field.get(getValue()));
      } catch (NoSuchFieldException e2) {
        throw new NoSuchPropertyException(getValue().getClass(), name);
      }
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @SneakyThrows
  @Override
  public DynamicObject setProperty(String name, Object value) {
    String setterName = "set" +
        Character.toUpperCase(name.charAt(0)) +
        name.substring(1);
    try {
      Method method = findMethod(getValue().getClass(), setterName, new Object[] {value});
      return DynamicObject.of(method.invoke(getValue(), value));
    } catch (MissingMethodException e) {
      // try searching in fields
      try {
        Field field = getValue().getClass().getDeclaredField(name);
        field.set(getValue(), value);
        return null;
      } catch (NoSuchFieldException e2) {
        throw new NoSuchPropertyException(getValue().getClass(), name);
      }
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @SneakyThrows
  @Override
  public DynamicObject invokeMethod(String name, Object... args) {
    Class<?> type = getValue().getClass();
    Method method = findMethod(type, name, args);
    try {
      return DynamicObject.of(method.invoke(getValue(), args));
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  private Method findMethod(Class<?> type, String name, Object[] args) {
    Method foundMethod;
    if (Arrays.stream(args).allMatch(Objects::nonNull)) {
      try {
        foundMethod = type.getMethod(name, argsTypes(args));
      } catch (NoSuchMethodException e) {
        throw new MissingMethodException(type, name, args);
      }
    } else {
      List<Method> methods = Arrays.stream(type.getDeclaredMethods())
          .filter(m -> m.getName().equals(name) &&  m.getParameterCount() == args.length)
          .collect(Collectors.toList());
      if (methods.isEmpty()) {
        throw new MissingMethodException(type, name, args);
      } else if (methods.size() == 1) {
        foundMethod = methods.get(0);
      } else {
        foundMethod = methods.stream()
            .filter(m -> parametersMatch(m, args))
            .findFirst()
            .orElseThrow(() -> new MissingMethodException(type, name, args));
      }
    }
    if (!foundMethod.isAccessible()) foundMethod.setAccessible(true);
    return foundMethod;
  }

  private static Class<?>[] argsTypes(Object[] args) {
    Class<?>[] classes = new Class<?>[args.length];
    for (int i = 0; i < args.length; i++) {
      classes[i] = args[i].getClass();
    }
    return classes;
  }

  private static boolean parametersMatch(Method method, Object[] args) {
    // assuming we've already checked method parameter size with args size
    Class<?>[] parameterTypes = method.getParameterTypes();
    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];
      if (arg != null && !parameterTypes[i].isInstance(arg)) return false;
    }
    return true;
  }
}
