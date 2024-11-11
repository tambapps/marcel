package marcel.lang.dynamic;

import lombok.SneakyThrows;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class AbstractDynamicObject implements DynamicObject {

  // lazy initialized
  private Map<String, DynamicObject> fieldMap;
  private Map<String, List<DynamicMethod>> methodMap;

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
  public void registerMethod(String name, MethodParameter param0, Lambda1<?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, Lambda2<?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, Lambda3<?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, MethodParameter param3, Lambda4<?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2, param3));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, MethodParameter param3, MethodParameter param4, Lambda5<?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2, param3, param4));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, MethodParameter param3, MethodParameter param4, MethodParameter param5, Lambda6<?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2, param3, param4, param5));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, MethodParameter param3, MethodParameter param4, MethodParameter param5, MethodParameter param6, Lambda7<?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2, param3, param4, param5, param6));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, MethodParameter param3, MethodParameter param4, MethodParameter param5, MethodParameter param6, MethodParameter param7, Lambda8<?, ?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2, param3, param4, param5, param6, param7));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, MethodParameter param3, MethodParameter param4, MethodParameter param5, MethodParameter param6, MethodParameter param7, MethodParameter param8, Lambda9<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2, param3, param4, param5, param6, param7, param8));
  }

  @Override
  public void registerMethod(String name, MethodParameter param0, MethodParameter param1, MethodParameter param2, MethodParameter param3, MethodParameter param4, MethodParameter param5, MethodParameter param6, MethodParameter param7, MethodParameter param8, MethodParameter param9, Lambda10<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> lambda) {
    registerMethod(name, DynamicMethod.of(lambda, param0, param1, param2, param3, param4, param5, param6, param7, param8, param9));
  }

  private void registerMethod(String name, DynamicMethod method) {
    List<DynamicMethod> methods = getMethodMap().computeIfAbsent(name, __ -> new ArrayList<>());
    if (methods.stream().anyMatch(m -> m.getParameters().equals(method.getParameters()))) {
      throw new IllegalArgumentException("Duplicate method '%s' with parameters %s".formatted(name, method.getParameters()));
    }
    methods.add(method);
  }

  @Override
  public void registerField(String name, Object value) {
    Map<String, DynamicObject> fieldMap = getFieldMap();
    if (fieldMap.containsKey(name)) {
      throw new IllegalArgumentException("Field '%s' already exists".formatted(name));
    }
    fieldMap.put(name, DynamicObject.of(value));
  }

  @Override
  public DynamicObject getProperty(String name) {
    if (fieldMap != null && fieldMap.containsKey(name)) {
      return fieldMap.get(name);
    }
    return DynamicObject.super.getProperty(name);
  }

  @Override
  public DynamicObject setProperty(String name, DynamicObject value) {
    if (fieldMap != null && fieldMap.containsKey(name)) {
      return fieldMap.put(name, DynamicObject.of(value));
    }
    return DynamicObject.super.setProperty(name, value);
  }

  @SneakyThrows
  final protected DynamicObject invokeMethod(Class<?> clazz, String name, Map<String, Object> namedArgs, Object... positionalArgs) {
    if (methodMap != null) {
      // find on registered methods
      List<DynamicMethod> methods = methodMap.get(name);
      if (methods != null && !methods.isEmpty()) {
        DynamicMethod method = methods.stream()
                .filter(m -> m.matches(namedArgs, positionalArgs))
                .findFirst()
                .orElse(null);
        if (method != null) {
          return method.invoke(namedArgs, positionalArgs);
        }
      }
    }

    // then find on class methods
    Class<?>[] argTypes = new Class[positionalArgs.length];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = positionalArgs[i] != null ? positionalArgs[i].getClass() : Object.class;
    }
    Method method = findMethod(clazz, name, argTypes);
    Object owner = (method.getModifiers() & Modifier.STATIC) != 0 ? null : getValue();
    try {
      return DynamicObject.of(method.invoke(owner, positionalArgs));
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  private Map<String, DynamicObject> getFieldMap() {
    if (fieldMap == null) {
      fieldMap = new HashMap<>();
    }
    return fieldMap;
  }

  private Map<String, List<DynamicMethod>> getMethodMap() {
    if (methodMap == null) {
      methodMap = new HashMap<>();
    }
    return methodMap;
  }
}
