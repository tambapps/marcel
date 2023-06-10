package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;

import java.util.Collection;

@AllArgsConstructor
abstract class DynamicCollection  implements DynamicObject {
  @Getter
  Collection value;


  @Override
  public DynamicObject plus(DynamicObject object) {
    Object o = object.getValue();
    if (!(o instanceof Collection)) {
      throw new IllegalArgumentException("Cannot add to a collection a value of type " + o.getClass().getSimpleName());
    }
    Collection c = copy();
    c.addAll((Collection) o);
    return DynamicObject.of(c);
  }

  abstract Collection copy();

}
