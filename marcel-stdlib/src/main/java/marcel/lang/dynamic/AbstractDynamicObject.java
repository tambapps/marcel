package marcel.lang.dynamic;

import lombok.SneakyThrows;
import marcel.lang.DynamicObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public abstract class AbstractDynamicObject implements DynamicObject {

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

  @SneakyThrows
  final protected DynamicObject invokeMethod(Class<?> clazz, String name, Object... args) {
    Class<?>[] argTypes = new Class[args.length];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
    }
    Method method = findMethod(clazz, name, argTypes);
    Object owner = (method.getModifiers() & Modifier.STATIC) != 0 ? null : getValue();
    try {
      return DynamicObject.of(method.invoke(owner, args));
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }
}
