package marcel.lang.dynamic;

import lombok.SneakyThrows;
import marcel.lang.DynamicObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

  // TODO do get/set property and document that extensions are not handled by dynamic objects
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
    if (Arrays.stream(args).allMatch(Objects::nonNull)) {
      try {
        return type.getMethod(name, argsTypes(args));
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
        return methods.get(0);
      } else {
        return methods.stream()
            .filter(m -> parametersMatch(m, args))
            .findFirst()
            .orElseThrow(() -> new MissingMethodException(type, name, args));
      }
    }
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
