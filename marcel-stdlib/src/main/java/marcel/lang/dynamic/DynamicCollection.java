package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.lang.methods.DefaultMarcelMethods;
import marcel.lang.methods.MarcelTruth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

  @Override
  public DynamicObject leftShift(Object object) {
    return DynamicObject.of(value.add(object));
  }

  abstract T copy();

  @Override
  public DynamicObject find(DynamicObjectLambda1 lambda1) {
    return DynamicObject.of(
        DefaultMarcelMethods.find(value, (e) -> MarcelTruth.truthy(lambda1.apply(DynamicObject.of(e))))
    );
  }

  @Override
  public DynamicObject map(DynamicObjectLambda1 lambda1) {
    List<DynamicObject> list = new ArrayList<>();
    for (Object o : value) {
      list.add(lambda1.apply(DynamicObject.of(o)));
    }
    return DynamicObject.of(list);
  }

  @Override
  public int size() {
    return value.size();
  }
}
