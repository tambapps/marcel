package marcel.lang.dynamic;

import marcel.lang.DynamicObject;

import java.util.Objects;

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

}
