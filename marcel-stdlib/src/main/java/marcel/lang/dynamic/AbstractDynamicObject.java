package marcel.lang.dynamic;

import lombok.SneakyThrows;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class AbstractDynamicObject implements DynamicObject {

  private final Map<String, DynamicObject> fieldMap = new HashMap<>();
  private final Map<String, List<DynamicMethod>> methodMap = new HashMap<>();

  @Override
  public String toString() {
    return getValue().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DynamicObject)) return getValue().equals(o);
    DynamicObject that = (DynamicObject) o;
    return Objects.equals(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    return getValue().hashCode();
  }

  protected Object getRealValue(Object o) {
    return o instanceof DynamicObject ? ((DynamicObject) o).getValue() : o;
  }

  protected Method findMethod(Class<?> clazz, String name, Class<?>... args) {
    try {
      return clazz.getMethod(name, args);
    } catch (NoSuchMethodException e) {
      throw new MissingMethodException(clazz, name, args);
    }
  }

  @Override
  public void registerMethod(String name, Lambda0<?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Lambda1<?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Lambda2<?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Lambda3<?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Class<?> arg3, Lambda4<?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2, arg3));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Lambda5<?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2, arg3, arg4));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Lambda6<?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2, arg3, arg4, arg5));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Class<?> arg6, Lambda7<?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2, arg3, arg4, arg5, arg6));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Class<?> arg6, Class<?> arg7, Lambda8<?, ?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Class<?> arg6, Class<?> arg7, Class<?> arg8, Lambda9<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8));
  }

  @Override
  public void registerMethod(String name, Class<?> arg0, Class<?> arg1, Class<?> arg2, Class<?> arg3, Class<?> arg4, Class<?> arg5, Class<?> arg6, Class<?> arg7, Class<?> arg8, Class<?> arg9, Lambda10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9));
  }

  private void registerMethod(String name, DynamicMethod method) {
    List<DynamicMethod> methods = methodMap.computeIfAbsent(name, __ -> new ArrayList<>());
    if (methods.stream().anyMatch(m -> m.getParameters().equals(method.getParameters()))) {
      throw new IllegalArgumentException("Duplicate method '%s' with parameters %s".formatted(name, method.getParameters()));
    }
    methods.add(method);
  }

  @Override
  public void registerField(String name, Object value) {
    if (fieldMap.containsKey(name)) {
      throw new IllegalArgumentException("Field '%s' already exists".formatted(name));
    }
    fieldMap.put(name, DynamicObject.of(value));
  }

  @Override
  public DynamicObject getProperty(String name) {
    if (fieldMap.containsKey(name)) {
      return fieldMap.get(name);
    }
    return DynamicObject.super.getProperty(name);
  }

  @Override
  public DynamicObject setProperty(String name, DynamicObject value) {
    if (fieldMap.containsKey(name)) {
      return fieldMap.put(name, DynamicObject.of(value));
    }
    return DynamicObject.super.setProperty(name, value);
  }

  @SneakyThrows
  final protected DynamicObject invokeMethod(Class<?> clazz, String name, Object... args) {
    Class<?>[] argTypes = new Class[args.length];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
    }
    // find on registered methods
    List<DynamicMethod> methods = methodMap.get(name);
    if (methods != null && !methods.isEmpty()) {
      DynamicMethod method = methods.stream()
              .filter(m -> m.matches(args))
              .findFirst()
                      .orElse(null);
      if (method != null) {
        return method.invoke(args);
      }
    }

    // then find on class methods
    Method method = findMethod(clazz, name, argTypes);
    Object owner = (method.getModifiers() & Modifier.STATIC) != 0 ? null : getValue();
    try {
      return DynamicObject.of(method.invoke(owner, args));
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }
}
