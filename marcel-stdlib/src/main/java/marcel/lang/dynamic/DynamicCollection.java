package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

import java.util.Collection;

@AllArgsConstructor
abstract class DynamicCollection extends AbstractDynamicObject {
  @Getter
  Collection value;


  @Override
  public DynamicObject plus(Object object) {
    Object o = getRealValue(object);
    if (!(o instanceof Collection)) {
      throw new MissingMethodException(getValue().getClass(), "plus", new Object[]{object});
    }
    Collection c = copy();
    c.addAll((Collection) o);
    return DynamicObject.of(c);
  }

  abstract Collection copy();

}
