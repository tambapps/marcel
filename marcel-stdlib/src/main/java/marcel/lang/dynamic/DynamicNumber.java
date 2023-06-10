package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

@AllArgsConstructor
public class DynamicNumber implements DynamicObject {

  private final Number value;


  @Override
  public Number getValue() {
    return value;
  }

  @Override
  public DynamicObject plus(DynamicObject object) {
    Object o = object.getValue();
    if (!(o instanceof Number)) {
      throw new IllegalArgumentException("Cannot sum a number with " + object.getClass().getSimpleName());
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() + n.doubleValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() + n.longValue());
    } else {
      return DynamicObject.of(value.intValue() + n.intValue());
    }
  }

  @Override
  public DynamicObject minus(DynamicObject object) {
    Object o = object.getValue();
    if (!(o instanceof Number)) {
      throw new IllegalArgumentException("Cannot substract a number with " + object.getClass().getSimpleName());
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() - n.doubleValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() - n.longValue());
    } else {
      return DynamicObject.of(value.intValue() - n.intValue());
    }
  }

  @Override
  public DynamicObject multiply(DynamicObject object) {
    Object o = object.getValue();
    if (!(o instanceof Number)) {
      throw new IllegalArgumentException("Cannot multiply a number with " + object.getClass().getSimpleName());
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() * n.doubleValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() * n.longValue());
    } else {
      return DynamicObject.of(value.intValue() * n.intValue());
    }
  }

  @Override
  public DynamicObject div(DynamicObject object) {
    Object o = object.getValue();
    if (!(o instanceof Number)) {
      throw new IllegalArgumentException("Cannot divide a number with " + object.getClass().getSimpleName());
    }
    Number n = (Number) o;
    if (value instanceof Double || n instanceof Double) {
      return DynamicObject.of(value.doubleValue() / n.doubleValue());
    } else if (value instanceof Long || n instanceof Long) {
      return DynamicObject.of(value.longValue() / n.longValue());
    } else {
      return DynamicObject.of(value.intValue() / n.intValue());
    }
  }

  // TODO some other operators are missing
}
