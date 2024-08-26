package marcel.lang.dynamic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marcel.lang.DynamicObject;
import marcel.lang.LongRange;
import marcel.lang.MarcelTruth;
import marcel.lang.lambda.DynamicObjectLambda1;
import marcel.util.primitives.collections.lists.LongArrayList;
import marcel.util.primitives.collections.lists.LongList;
import marcel.util.primitives.collections.sets.LongSet;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DynamicLongRange extends AbstractDynamicObject {

  @Getter
  final LongRange value;

  @Override
  public List asList() {
    return value.toList();
  }

  @Override
  public DynamicObject find(DynamicObjectLambda1 lambda1) {
    for (long i : value) {
      DynamicObject o = DynamicObject.of(i);
      if (MarcelTruth.isTruthy(lambda1.invoke(o))) {
        return o;
      }
    }
    return null;
  }

  @Override
  public DynamicObject findAll(DynamicObjectLambda1 lambda1) {
    LongList list = new LongArrayList();
    for (long i : value) {
      if (MarcelTruth.isTruthy(lambda1.invoke(DynamicObject.of(i)))) {
        list.add(i);
      }
    }
    return DynamicObject.of(list);
  }

  @Override
  public DynamicObject map(DynamicObjectLambda1 lambda1) {
    return new DynamicList(
        value.toList().stream().map(i -> lambda1.invoke(DynamicObject.of(i)))
            .collect(Collectors.toList())
    );
  }

  @Override
  public LongList asLongList() {
    return value.toList();
  }

  @Override
  public LongSet asLongSet() {
    return value.toList().toSet();
  }
}
