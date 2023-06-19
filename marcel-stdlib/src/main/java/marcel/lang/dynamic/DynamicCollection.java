package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

import java.util.Collection;

@AllArgsConstructor
abstract class DynamicCollection<T extends Collection> extends AbstractDynamicObject {
  @Getter
  T value;

  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (!(o instanceof Collection)) {
      throw new MissingMethodException(getValue().getClass(), "plus", new Object[]{object});
    }
    T c = copy();
    c.addAll((Collection) o);
    return DynamicObject.of(c);
  }

  abstract T copy();

}
