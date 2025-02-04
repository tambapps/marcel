package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

import java.util.Map;

@AllArgsConstructor
public class DynamicNumber extends AbstractDynamicObject {

  private final Number value;

  @Override
  public Number getValue() {
    return value;
  }

  @Override
  public int asInt() {
    return value.intValue();
  }

  @Override
  public long asLong() {
    return value.longValue();
  }

  @Override
  public float asFloat() {
    return value.floatValue();
  }

  @Override
  public double asDouble() {
    return value.doubleValue();
  }

  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (!(o instanceof Number)) {
      super.plus(object);
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() + n.doubleValue());
    } else if (value instanceof Float || n instanceof Float) {
      return DynamicObject.of(value.floatValue() + n.floatValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() + n.longValue());
    } else {
      return DynamicObject.of(value.intValue() + n.intValue());
    }
  }

  @Override
  public DynamicObject minus(Object object) {
    Object o = getRealValue(object);
    if (!(o instanceof Number)) {
      super.minus(object);
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() - n.doubleValue());
    } else if (value instanceof Float || n instanceof Float) {
      return DynamicObject.of(value.floatValue() - n.floatValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() - n.longValue());
    } else {
      return DynamicObject.of(value.intValue() - n.intValue());
    }
  }

  @Override
  public DynamicObject multiply(Object object) {
    Object o = getRealValue(object);
    if (!(o instanceof Number)) {
      throw new MissingMethodException(getValue().getClass(), "multiply", new Object[]{object});
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() * n.doubleValue());
    } else if (value instanceof Float || n instanceof Float) {
      return DynamicObject.of(value.floatValue() * n.floatValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() * n.longValue());
    } else {
      return DynamicObject.of(value.intValue() * n.intValue());
    }
  }

  @Override
  public DynamicObject div(Object object) {
    Object o = getRealValue(object);
    if (!(o instanceof Number)) {
      throw new MissingMethodException(getValue().getClass(), "div", new Object[]{object});
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() / n.doubleValue());
    } else if (value instanceof Float || n instanceof Float) {
      return DynamicObject.of(value.floatValue() / n.floatValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() / n.longValue());
    } else {
      return DynamicObject.of(value.intValue() / n.intValue());
    }
  }

  @Override
  public DynamicObject invokeMethod(String name, Map<String, Object> namedArgs, Object... positionalArgs) {
    return invokeMethod(Number.class, name, namedArgs, positionalArgs);
  }
}
